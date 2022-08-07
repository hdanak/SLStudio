package com.symmetrylabs.slstudio.pattern;

import java.util.List;
import java.util.Arrays;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsDevice;
import java.awt.Robot;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXModel;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.InterpolatingPattern;
import com.symmetrylabs.slstudio.component.ModelImageProjector;

public class ScreenSampler extends InterpolatingPattern<SLModel> {

    private DiscreteParameter screenNumberParam;
    private DiscreteParameter boundXParam, boundYParam, boundWidthParam, boundHeightParam;

    private final ModelImageProjector modelImageProjector;

    private Rectangle bounds = new Rectangle(0, 0, 0, 0);
    private GraphicsDevice[] screens;
    private Robot robot;
    private BufferedImage image;

    public ScreenSampler(LX lx) {
        super(lx);

        modelImageProjector = new ModelImageProjector(lx);
        modelImageProjector.addToPattern(this);

        // TODO: add trigger param to refresh screens
        screens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        System.out.println("Number of displays: " + screens.length);

        Rectangle screenBounds = new Rectangle(0, 0, 0, 0);
        if (screens.length > 0) {
            GraphicsDevice screen = screens[0];
            screenBounds = screen.getDefaultConfiguration().getBounds();
            try {
                robot = new Robot(screen);
            }
            catch (Exception e) {
                System.err.println(e);
            }
        }

        addParameter(screenNumberParam = new DiscreteParameter("screen", screens.length));
        addParameter(boundXParam = new DiscreteParameter("x", screenBounds.width));
        addParameter(boundYParam = new DiscreteParameter("y", screenBounds.height));
        addParameter(boundWidthParam = new DiscreteParameter("width", screenBounds.width));
        addParameter(boundHeightParam = new DiscreteParameter("height", screenBounds.height));
    }

    @Override
    public void onVectorsChanged() {
        super.onVectorsChanged();

        if (modelImageProjector != null) {
            modelImageProjector.clearCache();
        }
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == boundXParam) {
            bounds.x = boundXParam.getValuei();
        }
        if (p == boundYParam) {
            bounds.y = boundYParam.getValuei();
        }
        if (p == boundWidthParam) {
            bounds.width = boundWidthParam.getValuei();
        }
        if (p == boundHeightParam) {
            bounds.height = boundHeightParam.getValuei();
        }
        if (p == screenNumberParam) {
            GraphicsDevice screen = screens[screenNumberParam.getValuei()];
            Rectangle screenBounds = screen.getDefaultConfiguration().getBounds();

            boundWidthParam.setRange(screenBounds.width);
            boundHeightParam.setRange(screenBounds.height);
            boundXParam.setRange(screenBounds.width);
            boundYParam.setRange(screenBounds.height);

            try {
                robot = new Robot(screen);
            }
            catch (Exception e) {
                System.err.println(e);
            }
        }
    }

    @Override
    public void render(double deltaMs, List<LXVector> vecs, PolyBuffer polyBuffer) {

        if (robot == null) {
            clear();
            return;
        }

        try {
            image = robot.createScreenCapture(bounds);
        }
        catch (Exception e) {
            System.err.println(e);
        }

        int[] ccs = (int[]) polyBuffer.getArray(PolyBuffer.Space.SRGB8);
        modelImageProjector.projectImageToPoints(image, vecs, ccs);
        polyBuffer.markModified(PolyBuffer.Space.SRGB8);
    }
}
