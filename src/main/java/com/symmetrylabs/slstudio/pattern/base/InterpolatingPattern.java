package com.symmetrylabs.slstudio.pattern.base;

import java.util.Arrays;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.transform.LXVector;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.slstudio.model.SLModel;

public abstract class InterpolatingPattern<M extends SLModel> extends TripleBufferedPattern<M> {
    private RenderFrame frameA, frameB;

    public InterpolatingPattern(LX lx) {
        super(lx);

        frameA = tripleBuffer.getSnapshotBuffer();
        frameB = frameA;
    }

    @Override
    protected void run(double deltaMs, PolyBuffer.Space preferredSpace) {
        // need this here for now, can't call super.run()
        synchronized (this) {
            runLoopStarted = true;
        }

        RenderFrame frame = tripleBuffer.takeSnapshot();

        if (frame.renderEndNanos != frameB.renderEndNanos) {
            frameA = frameB;
            frameB = frame.copy();
        }

        double f = (System.nanoTime() - frameB.renderEndNanos) / (double)(frameB.renderEndNanos - frameA.renderEndNanos);
        //System.out.println(f);
        //System.out.println(frameA);
        //System.out.println(frameB);

        final double fFinal = f > 1 ? 1 : f < 0 ? 0 : f;
        PolyBuffer.Space spaceA = frameA.polyBuffer.getBestFreshSpace();
        PolyBuffer.Space spaceB = frameB.polyBuffer.getBestFreshSpace();
        if (spaceA != null && spaceB != null && spaceA == spaceB) {
            if (spaceA == PolyBuffer.Space.RGB16) {
                long[] arrayA = (long[])frameA.polyBuffer.getArray(spaceA);
                long[] arrayB = (long[])frameB.polyBuffer.getArray(spaceB);
                Arrays.parallelSetAll((long[])polyBuffer.getArray(spaceA), i -> Ops16.dissolve(arrayA[i], arrayB[i], fFinal));
            }
            else {
                int[] arrayA = (int[])frameA.polyBuffer.getArray(spaceA);
                int[] arrayB = (int[])frameB.polyBuffer.getArray(spaceB);
                Arrays.parallelSetAll((int[])polyBuffer.getArray(spaceA), i -> Ops8.dissolve(arrayA[i], arrayB[i], fFinal));
            }

            polyBuffer.markModified(spaceA);
        }
        else if (spaceA != null) {
            polyBuffer.copyFrom(frameA.polyBuffer, spaceA);
        }
        else if (spaceB != null) {
            polyBuffer.copyFrom(frameB.polyBuffer, spaceB);
        }
    }
}
