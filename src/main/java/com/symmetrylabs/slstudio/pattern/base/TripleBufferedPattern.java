package com.symmetrylabs.slstudio.pattern.base;

import java.util.Arrays;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.transform.LXVector;

import com.symmetrylabs.util.TripleBuffer;
import com.symmetrylabs.slstudio.model.SLModel;

public abstract class TripleBufferedPattern<M extends SLModel> extends SLPattern<M> {
    protected final TripleBuffer<RenderFrame> tripleBuffer;

    private RenderThread renderThread;
    private int fps = 60;

    // TODO: why is this even necessary?
    protected volatile boolean runLoopStarted = false;

    public TripleBufferedPattern(LX lx) {
        super(lx);

        tripleBuffer = new TripleBuffer<>(() -> new RenderFrame(new PolyBuffer(lx)));
    }

    public void render(double deltaMs, List<LXVector> vectors, int[] colors) { }

    public void render(double deltaMs, List<LXVector> vectors, PolyBuffer polyBuffer) {
        int[] layer = (int[])polyBuffer.getArray(PolyBuffer.Space.SRGB8);
        render(deltaMs, vectors, layer);
        polyBuffer.markModified(PolyBuffer.Space.SRGB8);
    }

    protected synchronized void start() {
        renderThread = new RenderThread();
        renderThread.start();
    }

    protected synchronized void stop() {
        if (renderThread != null) {
            renderThread.shutdown();
            renderThread = null;
            runLoopStarted = false;
        }
    }

    @Override
    public void onActive() {
        super.onActive();

        start();
    }

    @Override
    public void onInactive() {
        super.onInactive();

        stop();
    }

    public void setFPS(int fps) {
        this.fps = fps;
    }

    public int getFPS() {
        return fps;
    }

    @Override
    protected void run(double deltaMs, PolyBuffer.Space preferredSpace) {
        synchronized (this) {
            runLoopStarted = true;
        }

        if (!tripleBuffer.hasSnapshotChanged())
            return;

        RenderFrame frame = tripleBuffer.takeSnapshot();
        PolyBuffer.Space renderSpace = frame.polyBuffer.getBestFreshSpace();
        if (renderSpace != null) {
            polyBuffer.copyFrom(frame.polyBuffer, renderSpace);
        }
    }

    protected class RenderFrame {
        public PolyBuffer polyBuffer;
        public volatile long renderStartNanos;
        public volatile long renderEndNanos;

        public RenderFrame(PolyBuffer polyBuffer) {
            this.polyBuffer = polyBuffer;
        }

        public RenderFrame copy() {
            RenderFrame f = new RenderFrame(polyBuffer.clone());
            f.renderStartNanos = renderStartNanos;
            f.renderEndNanos = renderEndNanos;
            return f;
        }

        public String toString() {
            return "RenderFrame[ polyBuffer=" + polyBuffer
                + " renderStartNanos=" + renderStartNanos
                + " renderEndNanos=" + renderEndNanos + "]";
        }
    }

    private class RenderThread extends Thread {
        private boolean running = true;

        public void shutdown() {
            running = false;
            interrupt();
        }

        private void sleepNanosFromElapsed(long elapsedNanos) {
            long periodNanos = 1_000_000_000l / fps;
            if (elapsedNanos >= periodNanos)
                return;

            try {
                long nanos = periodNanos - elapsedNanos;
                long millisPart = nanos / 1_000_000l;
                sleep(millisPart, (int)(nanos - millisPart * 1_000_000l));
            }
            catch (InterruptedException e) { /* pass */ }
        }

        @Override
        public void run() {
            long lastTimeNanos = System.nanoTime();
            while (running) {
                if (!runLoopStarted) {
                    sleepNanosFromElapsed(0);
                    lastTimeNanos = System.nanoTime();
                    continue;
                }

                long timeNanos = System.nanoTime();
                double deltaMs = (timeNanos - lastTimeNanos) / 1_000_000d;

                RenderFrame active = tripleBuffer.getWriteBuffer();
                active.renderStartNanos = timeNanos;

                active.polyBuffer.setZero();
                render(deltaMs, getVectorList(), active.polyBuffer);

                long renderEndNanos = System.nanoTime();
                long elapsedNanos = renderEndNanos - lastTimeNanos;
                active.renderEndNanos = renderEndNanos;
                lastTimeNanos = timeNanos;

                tripleBuffer.flipWriteBuffer();

                sleepNanosFromElapsed(elapsedNanos);
            }
        }
    }
}
