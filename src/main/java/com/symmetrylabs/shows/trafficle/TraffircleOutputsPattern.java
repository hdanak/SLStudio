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
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.BooleanParameter;

import com.symmetrylabs.slstudio.pattern.base.SLPattern;


public class TraffircleOutputsPattern extends SLPattern<TraffircleModel> {
	public enum ColorMode {
        SOLID, RAMP, DISTINCT, SPECTRUM
    }

    private DiscreteParameter outputParam;
    private DiscreteParameter pointParam;
    private ColorParameter colorParam;

    private EnumParameter<ColorMode> colorModeParam;
    private CompoundParameter rampRateParam;
    private BooleanParameter iterateParam;
    private CompoundParameter iterPeriodParam;

    private double iterTime = 0;

    public TraffircleOutputsPattern(LX lx) {
        super(lx);

        int maxOutput = 1;
        for (int output : TraffircleShow.fixturesPerOutput.keySet()) {
            if (output > maxOutput) {
                maxOutput = output;
            }
        }

        addParameter(outputParam = new DiscreteParameter("output", 1, 0, maxOutput + 1));
        addParameter(pointParam = new DiscreteParameter("point", -1, -1, 0));
        addParameter(colorParam = new ColorParameter("color", LXColor.RED));
        addParameter(colorModeParam = new EnumParameter<ColorMode>("colorMode", ColorMode.SOLID));
        addParameter(rampRateParam = new CompoundParameter("rampRate", 10, 1, 50));
        addParameter(iterateParam = new BooleanParameter("iterate", false));
        addParameter(iterPeriodParam = new CompoundParameter("iterPeriod", 500, 100, 10000));
    }

    private int getOutputPointCount(int output) {
        int pointCount = 0;
        List<LXFixture> fixtures = TraffircleShow.fixturesPerOutput.get(output);
        if (fixtures != null) {
            for (LXFixture fixture : fixtures) {
                pointCount += fixture.getPoints().size();
            }
        }
        return pointCount;
    }

    public void onParameterChanged(LXParameter p) {
        if (p == outputParam) {
            int output = outputParam.getValuei();
            int pointCount = 0;
            if (output != 0) {
                pointCount = getOutputPointCount(output);
            }
            else {
                for (int i = 1; i < outputParam.getMaxValue() + 1; ++i) {
                    int x = getOutputPointCount(i);
                    if (x > pointCount) {
                        pointCount = x;
                    }
                }
            }

            pointParam.setValue(-1);
            pointParam.setRange(-1, pointCount);
        }
    }

    private void runForOutput(double deltaMs, int output) {
        if (!TraffircleShow.fixturesPerOutput.containsKey(output)) {
            return;
        }
        double startHue = colorParam.hue.getValue();
        double saturation = colorParam.saturation.getValue();
        double brightness = colorParam.brightness.getValue();
        List<LXFixture> fixtures = TraffircleShow.fixturesPerOutput.get(output);
        if (fixtures != null) {
            int selectedPoint = pointParam.getValuei();
            if (iterateParam.isOn()) {
                selectedPoint = ((int)(iterTime / iterPeriodParam.getValue())) % pointParam.getMaxValue();
                iterTime += deltaMs;
            }
            int i = 0;
            for (LXFixture fixture : fixtures) {
                for (LXPoint point : fixture.getPoints()) {
                    if (selectedPoint == -1 || selectedPoint == i) {
                        switch (colorModeParam.getEnum()) {
                        case SOLID:
                            colors[point.index] = colorParam.getColor();
                            break;
                        case RAMP:
                            colors[point.index] = LXColor.hsb((startHue + i * rampRateParam.getValue()) % 360, saturation, brightness);
                            break;
                        case DISTINCT:
                            colors[point.index] = LXColor.hsb((startHue + 360 * (output - 1) / outputParam.getMaxValue()) % 360, saturation, brightness);
                            break;
                        case SPECTRUM:
                            colors[point.index] = LXColor.hsb((startHue + 360 * i / (double)pointParam.getMaxValue()) % 360, saturation, brightness);
                            break;
                        }
                    }

                    ++i;
                }
            }
        }
    }

    public void run(double deltaMs) {
        clear();
        int output = outputParam.getValuei();
        if (output != 0) {
            runForOutput(deltaMs, output);
        }
        else {
            for (int i = 1; i < outputParam.getMaxValue() + 1; ++i) {
                runForOutput(deltaMs, i);
            }
        }
    }
}
