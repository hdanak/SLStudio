package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.BooleanParameter;

import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.Strip;

public class RubberBandsPattern extends SLPattern<StripsModel<?>> {
    public static final String GROUP_NAME = "strips";

    public final ColorParameter colorParam;
    public final BooleanParameter usePaletteParam;
    public final CompoundParameter dampenParam, freqParam, speedParam;
    public final BooleanParameter pluckParam, dampExpParam;

    private static final int CLOCK_MAX = 1000;

    private float[] strandDisplacements;
    private double t = 0;

    public RubberBandsPattern(LX lx) {
        super(lx);

        strandDisplacements = new float[model.getStrips().size()];

        addParameter(colorParam = new ColorParameter("Color", LXColor.WHITE));
        addParameter(usePaletteParam = new BooleanParameter("UsePalette"));
        colorParam.addListener(p -> {
            if (colorParam.getColor() != lx.palette.color.getColor()) {
                usePaletteParam.setValue(false);
            }
        });
        usePaletteParam.addListener(p -> {
            colorParam.setColor(lx.palette.color.getColor());
        });
        lx.palette.color.addListener(p -> {
            if (usePaletteParam.isOn()) {
                colorParam.setColor(lx.palette.color.getColor());
            }
        });

        addParameter(dampenParam = new CompoundParameter("Dampen", 1.5, 0, 8));
        addParameter(freqParam = new CompoundParameter("Freq", 1, 0, 4));
        addParameter(speedParam = new CompoundParameter("Speed", 1.5, 0.1, 4));
        addParameter(dampExpParam = new BooleanParameter("DampExp", true));
        addParameter(pluckParam = new BooleanParameter("Pluck").setMode(BooleanParameter.Mode.MOMENTARY));
        pluckParam.addListener((p) -> pluck());
    }

    private void pluck() {
        t = 0;
    }

    @Override
    protected void run(double deltaMs) {
        t += speedParam.getValue() * deltaMs / 1000;

        double dampener = dampExpParam.isOn() ? Math.exp(dampenParam.getValue() * t) : Math.pow(t, dampenParam.getValue()) + 1;
        float disp = (float)(Math.cos(freqParam.getValue() * Math.pow(t + 5 * Math.pow(Math.PI, 1./3) / 3, 3)) / dampener);

        clear();

        for (int i = 0; i < model.getStrips().size(); ++i) {

            Strip strip = model.getStrips().get(i);
            int strandLength = strip.getPoints().size();
            float strandCenter = (strandLength - 1) / 2f;
            for (int j = 0; j < strandLength; ++j) {
                float s = disp * (float)(Math.cos(Math.PI * (j - strandCenter) / (strandLength - 1)) - 0.5) + 0.5f;
                LXPoint p = strip.getPoints().get(j);
                colors[p.index] = LXColor.scaleBrightness(colorParam.getColor(), s);
            }
        }
    }
}
