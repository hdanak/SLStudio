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
import heronarts.lx.model.LXModel;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.slstudio.component.ModelImageProjector;
import com.symmetrylabs.slstudio.render.Renderer;
import com.symmetrylabs.slstudio.render.InterpolatingRenderer;
import com.symmetrylabs.slstudio.render.Renderable;

import static com.symmetrylabs.util.MathUtils.*;

public class ScreenSampler extends SLPattern<SLModel> {

    private DiscreteParameter screenNumber;
    private DiscreteParameter boundX, boundY, boundWidth, boundHeight;

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

        Rectangle screenBounds = null;
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

        addParameter(screenNumber = new DiscreteParameter("screen", screens.length));
        addParameter(boundX = new DiscreteParameter("x", screenBounds == null ? 0 : screenBounds.width));
        addParameter(boundY = new DiscreteParameter("y", screenBounds == null ? 0 : screenBounds.height));
        addParameter(boundWidth = new DiscreteParameter("width", screenBounds == null ? 0 : screenBounds.width));
        addParameter(boundHeight = new DiscreteParameter("height", screenBounds == null ? 0 : screenBounds.height));
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
        if (p == boundX) {
            bounds.x = boundX.getValuei();
        }
        if (p == boundY) {
            bounds.y = boundY.getValuei();
        }
        if (p == boundWidth) {
            bounds.width = boundWidth.getValuei();
        }
        if (p == boundHeight) {
            bounds.height = boundHeight.getValuei();
        }
        if (p == screenNumber) {
            GraphicsDevice screen = screens[screenNumber.getValuei()];
            Rectangle screenBounds = screen.getDefaultConfiguration().getBounds();

            boundWidth.setRange(screenBounds.width);
            boundHeight.setRange(screenBounds.height);
            boundX.setRange(screenBounds.width);
            boundY.setRange(screenBounds.height);

            try {
                robot = new Robot(screen);
            }
            catch (Exception e) {
                System.err.println(e);
            }
        }
    }

    @Override
    //public void run(double deltaMs) {
    //    List<LXVector> vecs = getVectorList();
    public void render(double deltaMs, List<LXVector> vecs, int[] colors) {

        if (robot != null) {
            try {
                image = robot.createScreenCapture(bounds);
            }
            catch (Exception e) {
                System.err.println(e);
            }
        }

        modelImageProjector.projectImageToPoints(image, vecs, colors);
    }

    protected Renderer createRenderer(LXModel model, int[] colors, Renderable renderable) {
        return new InterpolatingRenderer(model, colors, renderable);
    }
}
