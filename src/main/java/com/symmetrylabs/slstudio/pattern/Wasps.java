package com.symmetrylabs.slstudio.pattern;

import org.apache.commons.math3.util.FastMath;

import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.BooleanParameter;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.ParticlePattern;

public class Wasps extends ParticlePattern<SLModel> {
    private final double SQRT_2PI = FastMath.sqrt(2 * FastMath.PI);

    public CompoundParameter speed;
    public CompoundParameter accel;
    public CompoundParameter dampen;
    public CompoundParameter focusX;
    public CompoundParameter focusY;
    public CompoundParameter focusZ;
    public CompoundParameter pullX;
    public CompoundParameter pullY;
    public CompoundParameter pullZ;
    public CompoundParameter twistX;
    public CompoundParameter twistY;
    public CompoundParameter twistZ;
    public BooleanParameter edgeRepel;

    public Wasps(LX lx) {
        super(lx);
    }

    public Wasps(LX lx, int particleGroupCount) {
        super(lx, particleGroupCount);
    }

    @Override
    protected void createParameters() {
        super.createParameters();

        addParameter(speed = new CompoundParameter("Speed", 1, 0, 3));
        addParameter(accel = new CompoundParameter("Accel", 1.5, 0, 3));
        addParameter(dampen = new CompoundParameter("Dampen", 0.75, 0, 1));
        addParameter(focusX = new CompoundParameter("FocusX", 0, -1, 1));
        addParameter(focusY = new CompoundParameter("FocusY", 0, -1, 1));
        addParameter(focusZ = new CompoundParameter("FocusZ", 0, -1, 1));
        addParameter(pullX = new CompoundParameter("PullX", 0.5, 0, 1));
        addParameter(pullY = new CompoundParameter("PullY", 0.5, 0, 1));
        addParameter(pullZ = new CompoundParameter("PullZ", 0.5, 0, 1));
        addParameter(twistX = new CompoundParameter("TwistX", 0, -1, 1));
        addParameter(twistY = new CompoundParameter("TwistY", 0, -1, 1));
        addParameter(twistZ = new CompoundParameter("TwistZ", 0, -1, 1));
        addParameter(edgeRepel = new BooleanParameter("EdgeRepel", false));
    }

    @Override
    protected void initParticle(Particle p) {
        p.pos[0] = (float)(2 * FastMath.random() - 1);
        p.pos[1] = (float)(2 * FastMath.random() - 1);
        p.pos[2] = (float)(2 * FastMath.random() - 1);
        //System.out.println("[" + p.pos[0] + ", " + p.pos[1] + ", " + p.pos[2] + "]");
    }

    private static final float TIME_BOOST = 10f;
    private static final float ACCEL_SCALE = 0.03f;
    private static final float DAMPEN_SCALE = 0.05f;
    private static final float PULL_SCALE = 0.01f;
    private static final float TWIST_SCALE = 0.005f;
    private static final float BLOB_PULL_SCALE = 0.001f;

    @Override
    protected void simulate(double deltaMs) {
        double timeStep = TIME_BOOST * deltaMs / 1000f;

        double speedValue = speed.getValue();
        double accelValue = ACCEL_SCALE * accel.getValue();
        double dampenValue = DAMPEN_SCALE * dampen.getValue();

        double pullXValue = PULL_SCALE * pullX.getValue();
        double pullYValue = PULL_SCALE * pullY.getValue();
        double pullZValue = PULL_SCALE * pullZ.getValue();

        double twistXValue = TWIST_SCALE * twistX.getValue();
        double twistYValue = TWIST_SCALE * twistY.getValue();
        double twistZValue = TWIST_SCALE * twistZ.getValue();

        double focusPosX = focusX.getValue();
        double focusPosY = focusY.getValue();
        double focusPosZ = focusZ.getValue();

        double blobPosX = 0;
        double blobPosY = 0;
        double blobPosZ = 0;
        double blobScale = 0;

        if (enableBlobs.isOn()) {
            switch (blobTrackingMode.getEnum()) {
                case CLOSEST:
                    if (closestBlobDist != null) {
                        blobPosX = (closestBlobDist.blob.pos.x - model.cx) * 2f / model.xRange;
                        blobPosY = (closestBlobDist.blob.pos.y - model.cy) * 2f / model.yRange;
                        blobPosZ = (closestBlobDist.blob.pos.z - model.cz) * 2f / model.zRange;
                        blobScale = BLOB_PULL_SCALE * blobPull.getValue() / (closestBlobDist.dist + 1);
                    }
                    break;
                case AVERAGE:
                    if (visibleBlobCount > 0) {
                        blobPosX = (avgBlobPos[0] - model.cx) * 2f / model.xRange;
                        blobPosY = (avgBlobPos[1] - model.cy) * 2f / model.yRange;
                        blobPosZ = (avgBlobPos[2] - model.cz) * 2f / model.zRange;
                        blobScale = BLOB_PULL_SCALE * blobPull.getValue() / (avgBlobDist + 1);
                    }
                    break;
            }
        }

        final double blobPosXFinal = blobPosX;
        final double blobPosYFinal = blobPosY;
        final double blobPosZFinal = blobPosZ;
        final double blobScaleFinal = blobScale;

        final boolean edgeRepelEnabled = edgeRepel.isOn();

        particles.parallelStream().forEach(p -> {
            p.vel[0] -= dampenValue * p.vel[0];
            p.vel[1] -= dampenValue * p.vel[1];
            p.vel[2] -= dampenValue * p.vel[2];

            p.vel[0] += accelValue * (FastMath.random() - 0.5);
            p.vel[1] += accelValue * (FastMath.random() - 0.5);
            p.vel[2] += accelValue * (FastMath.random() - 0.5);

            double pullVecX = focusPosX - p.pos[0];
            double pullVecY = focusPosY - p.pos[1];
            double pullVecZ = focusPosZ - p.pos[2];

            p.vel[0] += pullXValue * pullVecX;
            p.vel[1] += pullYValue * pullVecY;
            p.vel[2] += pullZValue * pullVecZ;

            double blobVecX = blobPosXFinal - p.pos[0];
            double blobVecY = blobPosYFinal - p.pos[1];
            double blobVecZ = blobPosZFinal - p.pos[2];

            p.vel[0] += blobScaleFinal * blobVecX;
            p.vel[1] += blobScaleFinal * blobVecY;
            p.vel[2] += blobScaleFinal * blobVecZ;

            // NOTE: assuming left-handed Z-axis
            double pullNorm = FastMath.sqrt(pullVecX * pullVecX + pullVecY * pullVecY + pullVecZ * pullVecZ);

            double twistXVecX = 0;
            double twistXVecY = pullVecZ / pullNorm;
            double twistXVecZ = -pullVecY / pullNorm;

            double twistYVecX = -pullVecZ / pullNorm;
            double twistYVecY = 0;
            double twistYVecZ = pullVecX / pullNorm;

            double twistZVecX = -pullVecY / pullNorm;
            double twistZVecY = pullVecX / pullNorm;
            double twistZVecZ = 0;

            p.vel[0] += twistXValue * twistXVecX;
            p.vel[1] += twistXValue * twistXVecY;
            p.vel[2] += twistXValue * twistXVecZ;

            p.vel[0] += twistYValue * twistYVecX;
            p.vel[1] += twistYValue * twistYVecY;
            p.vel[2] += twistYValue * twistYVecZ;

            p.vel[0] += twistZValue * twistZVecX;
            p.vel[1] += twistZValue * twistZVecY;
            p.vel[2] += twistZValue * twistZVecZ;

            if (edgeRepelEnabled && p.contact > 0) {
                if (p.rebound[0] * p.vel[0] < 0) p.vel[0] /= (1 + p.contact);
                if (p.rebound[1] * p.vel[1] < 0) p.vel[1] /= (1 + p.contact);
                if (p.rebound[2] * p.vel[2] < 0) p.vel[2] /= (1 + p.contact);

                p.arrow[0] = p.rebound[0];
                p.arrow[1] = p.rebound[1];
                p.arrow[2] = p.rebound[2];
            }
            else {
                p.arrow[0] = 0;
                p.arrow[1] = 0;
                p.arrow[2] = 0;
            }

            p.pos[0] += p.vel[0] * speedValue * timeStep;
            p.pos[1] += p.vel[1] * speedValue * timeStep;
            p.pos[2] += p.vel[2] * speedValue * timeStep;

            if (p.pos[0] < -1) p.pos[0] = -1;
            if (p.pos[0] > 1) p.pos[0] = 1;
            if (p.pos[1] < -1) p.pos[1] = -1;
            if (p.pos[1] > 1) p.pos[1] = 1;
            if (p.pos[2] < -1) p.pos[2] = -1;
            if (p.pos[2] > 1) p.pos[2] = 1;
        });
    }
}
