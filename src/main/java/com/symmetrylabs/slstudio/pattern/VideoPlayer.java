package com.symmetrylabs.slstudio.pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.jna.Memory;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.StripsTopology;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;
import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

import javax.swing.*;
import java.nio.IntBuffer;
import java.util.Deque;
import java.util.LinkedList;

public class VideoPlayer<T extends Strip> extends SLPattern<StripsModel<T>> {
    CompoundParameter shrinkParam = new CompoundParameter("shrink", 1, 1.4, 3);
    CompoundParameter yOffsetParam = new CompoundParameter("yoff", 0, 0, 1);
    BooleanParameter fitParam = new BooleanParameter("fit", false);
    BooleanParameter restartParam = new BooleanParameter("restart", false);
    BooleanParameter chooseFileParam = new BooleanParameter("file", false);
    BooleanParameter captureParam = new BooleanParameter("capture", false);

    BooleanParameter maskX = new BooleanParameter("maskx", false);
    BooleanParameter maskY = new BooleanParameter("masky", false);
    BooleanParameter maskZ = new BooleanParameter("maskz", false);

    /**
     * A guess at the amount of time it will take vlcj to start playing the
     * video. We skip the video forward by this amount to compensate for
     * the expected lag.
     */
    private static final long INITIAL_SKEW_GUESS_MS = 200;

    /**
     * Like INITIAL_SKEW_GUESS_MS, but for when we're restarting the video
     * when it's already playing instead of playing it from scratch. This
     * ends up taking a little longer that just starting the video from
     * the beginning.
     */
    private static final long RESTART_SKEW_GUESS_MS = 270;

    /**
     * Like INITIAL_SKEW_GUESS_MS, but for when the video loops
     * automatically.
     */
    private static final long LOOP_SKEW_GUESS_MS = 100;

    private int[] buf = null;
    private int width;
    private int height;
    private double time;
    private boolean streaming = false;
    private long skipOnNextFrame = 0;

    private Deque<Double> timeOffsets = new LinkedList<>();

    private DirectMediaPlayerComponent mediaPlayerComponent;
    private DirectMediaPlayer mediaPlayer;
    private String mediaUrl;
    private String[] mediaOptions;

    public VideoPlayer(LX lx) {
        super(lx);

        addParameter(shrinkParam);
        addParameter(yOffsetParam);
        addParameter(fitParam);
        addParameter(restartParam);
        addParameter(chooseFileParam);
        addParameter(captureParam);

        addParameter(maskX);
        addParameter(maskY);
        addParameter(maskZ);

        fitParam.setMode(BooleanParameter.Mode.MOMENTARY);
        restartParam.setMode(BooleanParameter.Mode.MOMENTARY);
        chooseFileParam.setMode(BooleanParameter.Mode.MOMENTARY);
        captureParam.setMode(BooleanParameter.Mode.MOMENTARY);

        mediaPlayer = null;
        mediaPlayerComponent = null;
        mediaUrl = null;

        if (!new NativeDiscovery().discover()) {
            SLStudio.setWarning("VideoPlayer", "VLC not installed or not found");
            return;
        }

        mediaPlayerComponent = new DirectMediaPlayerComponent((w, h) -> {
            System.out.println(String.format("DMPC w=%d h=%d", w, h));
            buf = null;
            width = w;
            height = h;
            return new RV32BufferFormat(w, h);
        }) {
            @Override
            public void display(DirectMediaPlayer mediaPlayer, Memory[] nativeBuffers, BufferFormat bufferFormat) {
                Memory byteBuf = nativeBuffers[0];
                IntBuffer intBuf = byteBuf.getByteBuffer(0, byteBuf.size()).asIntBuffer();

                width = bufferFormat.getWidth();
                height = bufferFormat.getHeight();

                if (buf == null || buf.length != intBuf.limit()) {
                    System.out.println("VideoPlayer: realloc image buffer");
                    buf = new int[intBuf.limit()];
                }
                intBuf.get(buf);
            }
        };
        mediaPlayer = mediaPlayerComponent.getMediaPlayer();
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == fitParam && fitParam.getValueb()) {
            setShrinkToFit();
        }
        if (p == restartParam && restartParam.getValueb()) {
            restartVideo();
        }
        if (p == captureParam && captureParam.getValueb()) {
            useCaptureCard();
        }
        if (p == chooseFileParam && chooseFileParam.getValueb()) {
            showFileBrowser();
        }
    }

    @Override
    public void onActive() {
        super.onActive();
        restartVideo();
    }

    @Override
    public void onInactive() {
        super.onInactive();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        mediaPlayer.release();
    }

    private void showFileBrowser() {
        JFileChooser jfc = new JFileChooser();
        int res = jfc.showOpenDialog(null);
        if (res == JFileChooser.APPROVE_OPTION) {
            mediaPlayer.stop();
            mediaUrl = jfc.getSelectedFile().getAbsolutePath();
            mediaOptions = null;
        }
        streaming = false;
        restartVideo();
    }

    private void useCaptureCard() {
        mediaPlayer.stop();
        mediaUrl = "dshow://";
        mediaOptions = new String[] {
            ":dshow-size=720x480",
            ":dshow-aspect-ratio=16\\:9",
            ":dshow-fps=29.97",
            ":no-dshow-config",
            ":no-dshow-tuner",
            ":dshow-audio-bitspersample=0",
            ":dshow-vdev=Game Capture HD60 S (Video) (#01)",
            ":dshow-adev=none",
            ":dshow-caching=0",
            ":live-caching=0",
            };
        streaming = true;
        restartVideo();
    }

    private void setShrinkToFit() {
        float fitWidth = width / model.xRange;
        float fitHeight = height / model.yRange;
        float fit = Float.min(fitWidth, fitHeight);
        shrinkParam.setValue(fit);
    }

    private void restartVideo() {
        if (mediaPlayer == null)
            return;
        if (mediaUrl != null) {
            long skewGuess = 0;

            if (!mediaPlayer.isPlayable()) {
                mediaPlayer.prepareMedia(mediaUrl, mediaOptions);
                mediaPlayer.mute(true);
                mediaPlayer.setVolume(0);
                mediaPlayer.setRepeat(true);
                mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
                    @Override
                    public void finished(MediaPlayer player) {
                        skipOnNextFrame = LOOP_SKEW_GUESS_MS;
                    }
                });
                skewGuess = INITIAL_SKEW_GUESS_MS;
            } else if (mediaPlayer.isPlaying()) {
                mediaPlayer.setPosition(0);
                skewGuess = RESTART_SKEW_GUESS_MS;
            } else {
                mediaPlayer.setPosition(0);
                skewGuess = INITIAL_SKEW_GUESS_MS;
            }

            mediaPlayer.play();
            mediaPlayer.skip(skewGuess);
            time = 0;
        }
    }

    private static final String KEY_VIDEO_FILE = "videoFile";
    private static final String KEY_MEDIA_OPTIONS = "mediaOptions";
    private static final String KEY_STREAMING = "isStreaming";

    @Override
    public void save(LX lx, JsonObject json) {
        super.save(lx, json);
        if (mediaUrl != null) {
            json.addProperty(KEY_VIDEO_FILE, mediaUrl);
        }
        if (mediaOptions != null) {
            JsonArray arr = new JsonArray();
            for (String opt : mediaOptions) {
                arr.add(opt);
            }
            json.add(KEY_MEDIA_OPTIONS, arr);
        }
        json.addProperty(KEY_STREAMING, streaming);
    }

    @Override
    public void load(LX lx, JsonObject json) {
        super.load(lx, json);
        if (json.has(KEY_VIDEO_FILE)) {
            mediaUrl = json.get(KEY_VIDEO_FILE).getAsString();
        }
        if (json.has(KEY_MEDIA_OPTIONS)) {
            JsonArray arr = json.getAsJsonArray(KEY_MEDIA_OPTIONS);
            mediaOptions = new String[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                mediaOptions[i] = arr.get(i).getAsString();
            }
        }
        if (json.has(KEY_STREAMING)) {
            streaming = json.get(KEY_STREAMING).getAsBoolean();
        }
    }

    @Override
    public String getCaption() {
        if (mediaPlayer == null) {
            return "VLC not available, video playback disabled";
        }

        if (streaming) {
            return "streaming";
        }

        double avgOffset = 0;
        synchronized (timeOffsets) {
            for (Double t : timeOffsets) {
                avgOffset += t;
            }
            avgOffset /= timeOffsets.size();
        }

        long ms = mediaPlayer.getTime();
        int s = (int) Math.floor(ms / 1000f);
        ms -= 1000f * s;
        int m = (int) Math.floor(s / 60f);
        s -= m * 60f;
        int h = (int) Math.floor((float) m / 60f);
        m -= h * 60f;
        return String.format(
            "video time: %02d:%02d:%02d.%03d average skew: %.0fms",
            h, m, s, ms, avgOffset);
    }

    @Override
    public void run(double elapsedMs) {
        if (buf == null) {
            return;
        }
        time += elapsedMs;
        if (time > mediaPlayer.getLength()) {
            time = 0;
        }

        if (skipOnNextFrame != 0) {
            mediaPlayer.skip(skipOnNextFrame);
            skipOnNextFrame = 0;
        }

        double delta = (double) mediaPlayer.getTime() - time;
        synchronized (timeOffsets) {
            timeOffsets.addFirst(delta);
            if (timeOffsets.size() > 1000) {
                timeOffsets.removeLast();
            }
        }

        float shrink = shrinkParam.getValuef();
        boolean mx = maskX.getValueb();
        boolean my = maskY.getValueb();
        boolean mz = maskZ.getValueb();

        for (StripsTopology.Bundle b : model.getTopology().bundles) {
            for (int sidx : b.strips) {
                Strip s = model.getStripByIndex(sidx);
                for (LXVector v : getVectors(s.points)) {
                    int i = (int) ((shrink * (model.yMax - v.y)) + yOffsetParam.getValue() * height);
                    int j = (int) (shrink * (v.x - model.xMin));

                    int color;

                    if (mx && b.dir == StripsTopology.Dir.X) {
                        color = LXColor.gray(0);
                    } else if (my && b.dir == StripsTopology.Dir.Y) {
                        color = LXColor.gray(0);
                    } else if (mz && b.dir == StripsTopology.Dir.Z) {
                        color = LXColor.gray(0);
                    } else if (i >= height || j >= width || i < 0 || j < 0) {
                        color = LXColor.gray(0);
                    } else {
                        int vcolor = buf[width * i + j];
                        color = LXColor.rgb(
                            (vcolor >> 16) & 0xFF,
                            (vcolor >> 8) & 0xFF,
                            vcolor & 0xFF);
                    }
                    colors[v.index] = color;
                }
            }
        }
    }
}
