package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.color.LXColor;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;

public class PointSelector extends LXPattern {
    private DiscreteParameter selectedParam;
    private ColorParameter colorParam;

    public PointSelector(LX lx) {
        super(lx);

        addParameter(selectedParam = new DiscreteParameter("point", model.size));
        addParameter(colorParam = new ColorParameter("color", LXColor.RED));
    }

    public void run(double deltaMs) {
        clear();
        colors[selectedParam.getValuei()] = colorParam.getColor();
    }
}
