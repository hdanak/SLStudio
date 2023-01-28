package com.symmetrylabs.shows.traffircle;

import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXFixture;
import heronarts.lx.color.LXColor;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.DiscreteParameter;

import com.symmetrylabs.slstudio.pattern.base.SLPattern;


public class TraffircleOutputsPattern extends SLPattern<TraffircleModel> {
    private DiscreteParameter outputParam;
    private DiscreteParameter pointParam;
    private ColorParameter colorParam;

    public TraffircleOutputsPattern(LX lx) {
        super(lx);

        int maxOutput = 1;
        for (int output : TraffircleShow.fixturesPerOutput.keySet()) {
            if (output > maxOutput) {
                maxOutput = output;
            }
        }

        addParameter(outputParam = new DiscreteParameter("output", 1, 1, maxOutput + 1));
        addParameter(pointParam = new DiscreteParameter("point", -1, -1, 0));
        addParameter(colorParam = new ColorParameter("color", LXColor.RED));
    }

    public void onParameterChanged(LXParameter p) {
        if (p == outputParam) {
            int output = outputParam.getValuei();
            int pointCount = 0;
            List<LXFixture> fixtures = TraffircleShow.fixturesPerOutput.get(output);
            if (fixtures != null) {
                for (LXFixture fixture : fixtures) {
                    pointCount += fixture.getPoints().size();
                }
            }
            pointParam.setValue(-1);
            pointParam.setRange(-1, pointCount);
        }
    }

    public void run(double deltaMs) {
        clear();
        int output = outputParam.getValuei();
        if (!TraffircleShow.fixturesPerOutput.containsKey(output)) {
            return;
        }
        List<LXFixture> fixtures = TraffircleShow.fixturesPerOutput.get(output);
        if (fixtures != null) {
            int selectedPoint = pointParam.getValuei();
            int i = 0;
            for (LXFixture fixture : fixtures) {
                for (LXPoint point : fixture.getPoints()) {
                    if (selectedPoint == -1 || selectedPoint == i) {
                        colors[point.index] = colorParam.getColor();
                    }

                    ++i;
                }
            }
        }
    }
}
