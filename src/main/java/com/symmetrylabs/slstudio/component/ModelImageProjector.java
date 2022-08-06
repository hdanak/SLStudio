package com.symmetrylabs.slstudio.component;

/**
 * Refactored from ImageProject pattern.
 */

import java.util.Arrays;
import java.awt.image.BufferedImage;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.LXComponent;
import heronarts.lx.model.LXModel;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.transform.LXMatrix;
import heronarts.lx.transform.LXVector;

import static com.symmetrylabs.util.MathUtils.*;
import static com.symmetrylabs.util.MathConstants.*;

public class ModelImageProjector extends LXComponent {

    public enum Projection {
        PLANAR,
        MERCATOR,
        S4,
        S8,
    };

    public final CompoundParameter thetaParam = new CompoundParameter("theta", 0, -180, 180);
    public final CompoundParameter phiParam = new CompoundParameter("phi", 0, -180, 180);
    public final EnumParameter<Projection> projectionParam = new EnumParameter<>("proj", Projection.PLANAR);
    public final BooleanParameter flipIParam = new BooleanParameter("flipi", false);
    public final BooleanParameter flipJParam = new BooleanParameter("flipj", false);

    private LXModel model;
    private final float pointu[];
    private final float pointv[];
    private boolean cacheValid = false;

    public ModelImageProjector(LX lx) {
        super(lx);

        model = lx.model;
        pointu = new float[model.size];
        pointv = new float[model.size];

        addParameter(thetaParam);
        addParameter(phiParam);
        addParameter(projectionParam);
        addParameter(flipIParam);
        addParameter(flipJParam);
    }

    // there's gotta be a better way
    public void addToPattern(LXPattern pattern) {
        CompoundParameter thetaParam = new CompoundParameter("theta", 0, -180, 180);
        CompoundParameter phiParam = new CompoundParameter("phi", 0, -180, 180);
        EnumParameter<Projection> projectionParam = new EnumParameter<>("proj", Projection.PLANAR);
        BooleanParameter flipIParam = new BooleanParameter("flipi", false);
        BooleanParameter flipJParam = new BooleanParameter("flipj", false);

        thetaParam.addListener(p -> this.thetaParam.setValue(p.getValue()));
        phiParam.addListener(p -> this.phiParam.setValue(p.getValue()));
        projectionParam.addListener(p -> this.projectionParam.setValue(((EnumParameter)p).getEnum()));
        flipIParam.addListener(p -> this.flipIParam.setValue(((BooleanParameter)p).getValueb()));
        flipJParam.addListener(p -> this.flipJParam.setValue(((BooleanParameter)p).getValueb()));

        pattern.addParameter(thetaParam);
        pattern.addParameter(phiParam);
        pattern.addParameter(projectionParam);
        pattern.addParameter(flipIParam);
        pattern.addParameter(flipJParam);

        setParent(pattern);
    }

    public void clearCache() {
        cacheValid = false;
    }

    private void project(Iterable<LXVector> vecs) {
        if (cacheValid) {
            return;
        }

        float theta = PI / 180 * thetaParam.getValuef();
        float phi = PI / 180 * phiParam.getValuef();
        Projection proj = projectionParam.getEnum();

        switch (proj) {
        case PLANAR: {
            LXMatrix xf = LXMatrix.identity().rotateX(phi).rotateY(theta);
            LXVector u = xf.apply(1, 0, 0);
            LXVector v = xf.apply(0, 1, 0);
            float umin = Float.POSITIVE_INFINITY;
            float umax = Float.NEGATIVE_INFINITY;
            float vmin = Float.POSITIVE_INFINITY;
            float vmax = Float.NEGATIVE_INFINITY;

            for (LXVector vec : vecs) {
                float uval = u.dot(vec);
                float vval = v.dot(vec);
                if (uval < umin) {
                    umin = uval;
                }
                if (uval > umax) {
                    umax = uval;
                }
                if (vval < vmin) {
                    vmin = vval;
                }
                if (vval > vmax) {
                    vmax = vval;
                }
            }
            for (LXVector vec : vecs) {
                pointu[vec.index] = (u.dot(vec) - umin) / (umax - umin);
                pointv[vec.index] = (v.dot(vec) - vmin) / (vmax - vmin);
            }
            break;
        }
        case MERCATOR: {
            LXVector center = new LXVector(model.cx, model.cy, model.cz);
            for (LXVector vec : vecs) {
                LXVector x = center.copy().mult(-1).add(vec);
                x.normalize();
                pointu[vec.index] = (float) ((Math.PI + Math.atan2(x.x, -x.z) + theta) / (2 * Math.PI));
                pointv[vec.index] = (float) ((Math.PI - Math.acos(x.y) + phi) / Math.PI);
            }
            break;
        }
        case S4: {
            LXVector center = new LXVector(model.cx, model.cy, model.cz);
            for (LXVector vec : vecs) {
                LXVector x = center.copy().mult(-1).add(vec);
                x.normalize();
                double uu = wrap((float) Math.atan2(x.x, -x.z) + theta, -PI, PI) / PI;
                if (uu < 0) {
                    uu = -uu;
                }
                double vv = wrap((float) Math.acos(x.y) + phi - HALF_PI, -HALF_PI, HALF_PI) / HALF_PI;
                if (vv < 0) {
                    vv = -vv;
                }
                pointu[vec.index] = (float) uu;
                pointv[vec.index] = (float) vv;
            }
            break;
        }
        case S8: {
            LXVector center = new LXVector(model.cx, model.cy, model.cz);
            for (LXVector vec : vecs) {
                LXVector x = center.copy().mult(-1).add(vec);
                x.normalize();
                double uu = wrap((float) Math.atan2(x.x, -x.z) + theta + PI, 0, TWO_PI) / HALF_PI;
                double vv = wrap((float) Math.acos(x.y) + phi, 0, PI) / HALF_PI;
                uu = ((int) uu) % 2 == 0 ? uu - Math.floor(uu) : Math.ceil(uu) - uu;
                vv = ((int) vv) % 2 == 0 ? vv - Math.floor(vv) : Math.ceil(vv) - vv;
                pointu[vec.index] = (float) uu;
                pointv[vec.index] = (float) vv;
            }
            break;
        }
        }

        cacheValid = true;
    }

    public void projectImageToPoints(BufferedImage image, Iterable<LXVector> vecs, int[] colors) {
        if (image == null) {
            Arrays.fill(colors, 0);
            return;
        }

        project(vecs);

        int width = image.getWidth();
        int height = image.getHeight();
        int wlo = flipIParam.getValueb() ? width - 1 : 0;
        int whi = flipIParam.getValueb() ? 0 : width - 1;
        int hlo = flipJParam.getValueb() ? 0 : height - 1;
        int hhi = flipJParam.getValueb() ? height - 1 : 0;

        for (LXVector vec : vecs) {
            int i = Math.round(mapPeriodic(pointu[vec.index], 0, 1, wlo, whi));
            int j = Math.round(mapPeriodic(pointv[vec.index], 0, 1, hlo, hhi));
            colors[vec.index] = image.getRGB(i, j);
        }
    }
}
