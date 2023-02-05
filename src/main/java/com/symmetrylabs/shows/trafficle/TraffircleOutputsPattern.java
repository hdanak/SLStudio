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

import com.symmetrylabs.util.TurboColorScheme;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;


public class TraffircleOutputsPattern extends SLPattern<TraffircleModel> {
	public enum ColorMode {
        SOLID, RAMP, DISTINCT, SPECTRUM
    }

	public enum ColorScheme {
        TURBO, SINEBOW, HSV
    }

    private DiscreteParameter outputParam;
    private DiscreteParameter pointParam;
    private ColorParameter colorParam;

    private EnumParameter<ColorMode> colorModeParam;
    private EnumParameter<ColorScheme> colorSchemeParam;
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
        addParameter(colorSchemeParam = new EnumParameter<ColorScheme>("colorScheme", ColorScheme.TURBO));
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
        else if (p == iterateParam) {
            iterTime = 0;
        }
    }

    // x is from 0 to 1
    private int rainbowFunc(double x) {
        switch (colorSchemeParam.getEnum()) {
        case HSV:
            double saturation = colorParam.saturation.getValue();
            double brightness = colorParam.brightness.getValue();
            return LXColor.hsb((x * 360) % 360, saturation, brightness);
        case SINEBOW:
            double t = 0.5 - x;
            double r = Math.sin(Math.PI * t);
            double g = Math.sin(Math.PI * (t + 1./3));
            double b = Math.sin(Math.PI * (t + 2./3));
            return LXColor.rgb((int)(255 * r * r), (int)(255 * g * g), (int)(255 * b * b));
        case TURBO:
        default:
            return TurboColorScheme.interpolate(x);
        }
    }

    private void runForOutput(double deltaMs, int output) {
        if (!TraffircleShow.fixturesPerOutput.containsKey(output)) {
            return;
        }
        double startHue = colorParam.hue.getValue();
        List<LXFixture> fixtures = TraffircleShow.fixturesPerOutput.get(output);
        if (fixtures != null) {
            int selectedPoint = pointParam.getValuei();
            if (iterateParam.isOn()) {
                selectedPoint = ((int)(iterTime / iterPeriodParam.getValue())) % pointParam.getMaxValue();
                iterTime += deltaMs;
            }
            int i = 0;
            int outputPointCount = getOutputPointCount(output);
            for (LXFixture fixture : fixtures) {
                for (LXPoint point : fixture.getPoints()) {
                    if (selectedPoint == -1 || selectedPoint == i) {
                        switch (colorModeParam.getEnum()) {
                        case SOLID:
                            colors[point.index] = colorParam.getColor();
                            break;
                        case RAMP:
                            colors[point.index] = rainbowFunc((startHue + i * rampRateParam.getValue()) % 360 / 360);
                            break;
                        case DISTINCT:
                            colors[point.index] = rainbowFunc((startHue + 360 * (output - 1) / outputParam.getMaxValue()) % 360 / 360);
                            break;
                        case SPECTRUM:
                            colors[point.index] = rainbowFunc((startHue + 360 * i / (double)outputPointCount) % 360 / 360);
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
