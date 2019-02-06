package com.symmetrylabs.slstudio.output;

import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXModel;
import heronarts.lx.output.LXOutput;
import heronarts.lx.parameter.*;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

public class ContinuousOfflineRenderOutput extends OfflineRenderOutput {
    public static final String HEADER = "SLOutput";
    public static final int VERSION = 3;

    private File output = null;
    private LXModel model;
    private long startFrameNanos;
    private int lastFrameWritten;
    private BufferedImage img = null;
    private int framesToCapture;
    private double frameRate;

    public final BooleanParameter pStart = new BooleanParameter("pStart", false).setMode(BooleanParameter.Mode.MOMENTARY);
    public final DiscreteParameter pFramesToCapture = new DiscreteParameter("frames", 60, 0, 30000);
    public final CompoundParameter pFrameRate = new CompoundParameter("rate", 30, 1, 60);
    public final StringParameter pOutputFile = new StringParameter("output", "");
    public final StringParameter pStatus = new StringParameter("status", "IDLE");
    public final MutableParameter hunkSize = new MutableParameter("hunkSizeFrames", 30);

    WriterThread writerThread;
    private int currentFrameLimit;
    private int hunkIndex;

    public ContinuousOfflineRenderOutput(LX lx) {
        super(lx);
        this.model = lx.model;
        pOutputDir.addListener(p -> {
            dispose();
            if (!pOutputDir.getString().equals("")) {
//                    output = new File(pOutputFile.getString());
                output = new File(getCurrentOutputPath());
            }
        });
        pStart.addListener(p -> {
            if (pStart.getValueb()) {
                createImage();
            }
        });

        currentFrameLimit = hunkSize.getValuei(); // initialize our first write trigger
        hunkIndex = 0;

        writerThread = new WriterThread("writer");
    }

    class WriterThread extends Thread {

        boolean renderPending = false;
        boolean workDone = false;

        WriterThread(String name) {
            super(name);
        }

        public void run() {
            while (!isInterrupted()) {
                // Wait until we have work to do...
                synchronized (this) {
                    try {
                        while (!this.renderPending) {
                            wait();
                        }
                    } catch (InterruptedException ix) {
                        // Channel is finished
                        break;
                    }
                    this.renderPending = false;
                }

                // Do our work
//                runThread(this.branches, deltaMs);
                writeHunk();

                // Signal to the main thread that we are done
                synchronized (this) {
                    this.workDone = true;
                    notify();
                }
            }
        }

        public void writeHunk() {
            final BufferedImage imgToWrite = img;
//            final BufferedImage imgToWrite = getCurrentBufferedImage();
            final File outputToWrite = output;
            EventQueue.invokeLater(() -> {
                try {
                    ImageIO.write(imgToWrite, "png", outputToWrite);
                } catch (IOException e) {
                    System.err.println("couldn't save output image:");
                    e.printStackTrace();
                }
            });
            updateOutputTargetToNextHunk();
        }
    }

    private void updateOutputTargetToNextHunk() {
        output = new File(getCurrentOutputPath());
        hunkIndex++;
        System.out.println(output.getAbsoluteFile());
    }

    private BufferedImage getCurrentBufferedImage() {
        return img;
    }


    @NotNull
    private String getCurrentOutputPath() {
        return pOutputDir.getString() + "/" + hunkIndex + ".png";
    }

    public void dispose() {
        img = null;
        pStatus.setValue("IDLE");
    }

    private void createImage() {
        createImage(-1);
    }

    private void createImage(int lastFrame) {
        startFrameNanos = System.nanoTime();
        lastFrameWritten = lastFrame;
        framesToCapture = pFramesToCapture.getValuei();
        frameRate = pFrameRate.getValue();
        img = new BufferedImage(model.points.length, pFramesToCapture.getValuei(), BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    protected void onSend(PolyBuffer colors) {
        if (img == null) {
            return;
        }
        pStatus.setValue("REC");

        double elapsedSec = 1e-9 * (double) (System.nanoTime() - startFrameNanos);
        int inFrame = (int) Math.floor(frameRate * elapsedSec);
        if (inFrame <= lastFrameWritten) {
            return;
        }
        lastFrameWritten = inFrame;
        if (lastFrameWritten >= currentFrameLimit) {

            writerThread.writeHunk();
            currentFrameLimit += hunkSize.getValuei();
//            dispose();
        } else {
            int[] carr = (int[]) colors.getArray(PolyBuffer.Space.RGB8);
            img.setRGB(0, lastFrameWritten%hunkSize.getValuei(), model.points.length, 1, carr, 0, model.points.length);
        }
    }
}