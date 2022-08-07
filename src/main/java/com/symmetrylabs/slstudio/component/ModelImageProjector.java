package com.symmetrylabs.slstudio.component;

/**
 * Refactored from ImageProject pattern.
 */

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.StreamSupport;
import java.awt.image.BufferedImage;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.LXComponent;
import heronarts.lx.model.LXModel;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXListenableParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
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

    public final BoundedParameter thetaParam = new BoundedParameter("theta", 0, -180, 180);
    public final BoundedParameter phiParam = new BoundedParameter("phi", 0, -180, 180);
    public final EnumParameter<Projection> projectionParam = new EnumParameter<>("proj", Projection.PLANAR);
    public final BooleanParameter flipIParam = new BooleanParameter("flipi", false);
    public final BooleanParameter flipJParam = new BooleanParameter("flipj", false);
    public final DiscreteParameter blurParam = new DiscreteParameter("blur", 0, 0, 100);

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
        addParameter(blurParam);
    }

    private static LXParameter cloneParameter(LXParameter p) {
        LXParameter pp = null;

        // parameters must be in reverse order of generality
        if (p instanceof BoundedParameter) {
            BoundedParameter ourParam = (BoundedParameter)p;
            pp = new BoundedParameter(ourParam.getLabel(), ourParam.getValue(),
                        ourParam.range.min, ourParam.range.max);
        }
        else if (p instanceof EnumParameter) {
            pp = new EnumParameter(p.getLabel(), ((EnumParameter)p).getEnum());
        }
        else if (p instanceof DiscreteParameter) {
            DiscreteParameter ourParam = (DiscreteParameter)p;
            pp = new DiscreteParameter(ourParam.getLabel(), ourParam.getValuei(),
                        ourParam.getMinValue(), ourParam.getMaxValue() + 1);
            String[] options = ourParam.getOptions();
            if (options != null) {
                ((DiscreteParameter)pp).setOptions(options);
            }
        }
        else if (p instanceof BooleanParameter) {
            BooleanParameter ourParam = (BooleanParameter)p;
            pp = new BooleanParameter(ourParam.getLabel(), ourParam.getValueb());
            ((BooleanParameter)pp).setMode(ourParam.getMode());
        }

        if (pp == null)
            return null;

        if (pp instanceof LXListenableParameter) {
            LXListenableParameter lp = (LXListenableParameter)pp;
            lp.setFormatter(p.getFormatter());
            lp.setUnits(p.getUnits());
            lp.setPolarity(p.getPolarity());
            lp.setDescription(p.getDescription());
            lp.setShouldSerialize(p.getShouldSerialize());
            lp.setVisible(p.isVisible());
            lp.setPriority(p.getPriority());
            lp.setSupportsOscTransmit(p.supportsOscTransmit());
        }

        return pp;
    }

    public void addToPattern(LXPattern pattern) {
        List<LXParameter> patternParams = new ArrayList<>();
        for (LXParameter p : getParameters()) {
            if (!(p instanceof LXListenableParameter))
                continue;

            LXListenableParameter patternParam = (LXListenableParameter)cloneParameter(p);
            if (patternParam == null)
                continue;

            if (p instanceof EnumParameter) {
                patternParam.addListener(pp -> ((EnumParameter)p).setValue(((EnumParameter)pp).getEnum()));
            }
            else {
                patternParam.addListener(pp -> p.setValue(pp.getValue()));
            }

            patternParams.add(patternParam);
        }

        pattern.addParameters(patternParams);
        setParent(pattern);
    }

    public void clearCache() {
        cacheValid = false;
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p != blurParam) {
            clearCache();
        }
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

        int blur = blurParam.getValuei();
        int width = image.getWidth();
        int height = image.getHeight();
        int wlo = flipIParam.getValueb() ? width - 1 : 0;
        int whi = flipIParam.getValueb() ? 0 : width - 1;
        int hlo = flipJParam.getValueb() ? 0 : height - 1;
        int hhi = flipJParam.getValueb() ? height - 1 : 0;

        StreamSupport.stream(vecs.spliterator(), true).forEach((LXVector vec) -> {
            int i = Math.round(mapPeriodic(pointu[vec.index], 0, 1, wlo, whi));
            int j = Math.round(mapPeriodic(pointv[vec.index], 0, 1, hlo, hhi));

            if (blur != 0) { // apply box blur
                int iStart = i - blur / 2; // inclusive
                if (iStart < 0) iStart = 0;
                int iEnd = i + blur - blur / 2 + 1; // exclusive
                if (iEnd > width) iEnd = width;
                int jStart = j - blur / 2; // inclusive
                if (jStart < 0) jStart = 0;
                int jEnd = j + blur - blur / 2 + 1; // exclusive
                if (jEnd > height) jEnd = height;

                double rSum = 0;
                double gSum = 0;
                double bSum = 0;

                for (int k = iStart; k < iEnd; ++k) {
                    for (int l = jStart; l < jEnd; ++l) {
                        int c = image.getRGB(k, l);
                        rSum += LXColor.red(c) & 0xff;
                        gSum += LXColor.green(c) & 0xff;
                        bSum += LXColor.blue(c) & 0xff;
                    }
                }

                int count = (iEnd - iStart) * (jEnd - jStart);
                colors[vec.index] = LXColor.rgb((int)(rSum / count), (int)(gSum / count), (int)(bSum / count));
            }
            else {
                colors[vec.index] = image.getRGB(i, j);
            }
        });
    }
}
