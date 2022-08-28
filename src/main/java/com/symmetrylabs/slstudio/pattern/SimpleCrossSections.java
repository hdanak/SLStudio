package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.color.Ops8;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;

import static com.symmetrylabs.util.MathUtils.*;
import static heronarts.lx.PolyBuffer.Space.SRGB8;

public class SimpleCrossSections extends LXPattern {

    public enum Axis {
        X, Y, Z;
    }

    public final EnumParameter<Axis> axisParam = new EnumParameter<Axis>("Axis", Axis.X);
    public final CompoundParameter posParam = new CompoundParameter("Pos");
    public final CompoundParameter widthParam = new CompoundParameter("Width", 0.3);


    public SimpleCrossSections(LX lx) {
        super(lx);

        addParameter(axisParam);
        addParameter(posParam);
        addParameter(widthParam);
    }

    public void run(double deltaMs) {
        clear();

        for (LXVector p : getVectorList()) {
            int c = 0;
            switch (axisParam.getEnum()) {
            case X: {
                double size = widthParam.getValue() * model.xRange;
                double pos = model.xMin + posParam.getValue() * model.xRange;
                double posMin = pos - size / 2;
                double posMax = pos + size / 2;
                if (p.x > posMin && p.x < posMax) {
                    c = LXColor.WHITE;
                }
            } break;
            case Y: {
                double size = widthParam.getValue() * model.yRange;
                double pos = model.yMin + posParam.getValue() * model.yRange;
                double posMin = pos - size / 2;
                double posMax = pos + size / 2;
                if (p.y > posMin && p.y < posMax) {
                    c = LXColor.WHITE;
                }
            } break;
            case Z: {
                double size = widthParam.getValue() * model.zRange;
                double pos = model.zMin + posParam.getValue() * model.zRange;
                double posMin = pos - size / 2;
                double posMax = pos + size / 2;
                if (p.z > posMin && p.z < posMax) {
                    c = LXColor.WHITE;
                }
            } break;
            }
            colors[p.index] = c;
        }
    }
}
