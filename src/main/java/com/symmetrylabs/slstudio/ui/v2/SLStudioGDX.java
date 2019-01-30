package com.symmetrylabs.slstudio.ui.v2;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.symmetrylabs.LXClassLoader;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.ShowRegistry;
import com.symmetrylabs.slstudio.SLStudio;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import com.symmetrylabs.util.Utils;
import com.symmetrylabs.slstudio.ApplicationState;
import com.symmetrylabs.slstudio.network.NetworkMonitor;
import heronarts.lx.model.LXPoint;
import com.symmetrylabs.slstudio.output.OutputControl;

public class SLStudioGDX extends ApplicationAdapter implements ApplicationState.Provider {
    private static final String DEFAULT_SHOW = "demo";
    private String showName;
    private Show show;
    private ModelRenderer renderer;
    private LX lx;
    private OutputControl outputControl;

    /* visible so that InternalsWindow can mutate it. */
    int clearRGB;

    CameraInputController camController;

    int lastBufWidth = 0, lastBufHeight;

    @Override
    public void create() {
        String sn;
        try {
            sn = Files.readAllLines(Paths.get(SLStudio.SHOW_FILE_NAME)).get(0);
        } catch (IOException e) {
            System.err.println(
                "couldn't read " + SLStudio.SHOW_FILE_NAME + ": " + e.getMessage());
            sn = DEFAULT_SHOW;
        }
        UI.init(((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle());

        /* clearR/clearG/clearB will be set on first frame */
        clearRGB = 0x222222;

        /* TODO: we should remove any need to know what the "sketch path" is, because
             it means we can only run SLStudio from source, but for now we assume that
             the process is started from the root of the SLStudio repository and set
             the sketch path to that. */
        Utils.setSketchPath(Paths.get(System.getProperty("user.dir")).toString());

        ApplicationState.setProvider(this);

        loadShow(sn);
    }

    void loadShow(String showName) {
        System.out.println("opening show " + showName);
        if (lx != null) {
            disposeLX();
        }

        WindowManager.reset();
        ConsoleWindow.reset();
        WindowManager.addPersistent(ConsoleWindow.WINDOW_NAME, () -> new ConsoleWindow(), false);

        this.showName = showName;
        show = ShowRegistry.getShow(showName);

        LXModel model = show.buildModel();
        lx = new LX(model);

        outputControl = new OutputControl(lx);

        // make sure that ApplicationState is fully filled out before setupLx is called
        show.setupLx(lx);

        renderer = new ModelRenderer(lx, model);
        renderer.setDisplayDensity(Gdx.graphics.getDensity());

        camController = new CameraInputController(renderer.cam);
        camController.target.set(model.cx, model.cy, model.cz);
        camController.translateUnits = model.xRange;
        camController.scrollFactor *= -0.2f;

        Gdx.input.setInputProcessor(new DelegatingInputProcessor(camController));

        loadLxComponents();

        /* The main menu isn't really transient but we don't want it to appear in
             the Window menu and it doesn't have a close button, so there's no risk of
             it disappearing. */
        WindowManager.addTransient(new MainMenu(lx, this));
        WindowManager.addPersistent("Audio", () -> new AudioWindow(lx), true);
        WindowManager.addPersistent("Internals", () -> new InternalsWindow(lx, this), false);
        WindowManager.addPersistent("Project", () -> new ProjectWindow(lx), true);
        WindowManager.addPersistent("Warps / effects / patterns", () -> new WEPWindow(lx), true);
        WindowManager.addPersistent("Widget demo", () -> new SlimguiDemoWindow(), false);

        WindowManager.addPersistent("Channel 1", () -> new ChannelWindow(lx, lx.engine.getChannel(0)), false);

        lx.engine.isMultithreaded.setValue(true);
        lx.engine.isChannelMultithreaded.setValue(true);
        lx.engine.isNetworkMultithreaded.setValue(true);
        lx.engine.start();

        show.setupUi(lx);
    }

    @Override
    public void render() {
        int w = Gdx.graphics.getBackBufferWidth();
        int h = Gdx.graphics.getBackBufferHeight();
        Gdx.gl20.glViewport(0, 0, w, h);
        if (w != lastBufWidth || h != lastBufHeight) {
            renderer.setBackBufferSize(w, h);
        }

        float clearR = ((clearRGB >> 16) & 0xFF) / 255.f;
        float clearG = ((clearRGB >>  8) & 0xFF) / 255.f;
        float clearB = ((clearRGB      ) & 0xFF) / 255.f;
        Gdx.gl20.glClearColor(clearR, clearG, clearB, 1);

        Gdx.gl20.glClear(
            GL20.GL_COLOR_BUFFER_BIT
            | (Gdx.graphics.getBufferFormat().coverageSampling
                 ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
        renderer.cam.viewportHeight = Gdx.graphics.getHeight();
        renderer.cam.viewportWidth = Gdx.graphics.getWidth();
        renderer.cam.update();

        lx.engine.onDraw();

        camController.update();
        renderer.draw();

        UI.newFrame();
        WindowManager.get().draw();
        UI.render();
    }

    private void disposeLX() {
        NetworkMonitor.shutdownInstance(lx);
        renderer.dispose();
        lx.engine.stop();
        /* we have to call onDraw here because onDraw is the only thing that pokes the
             engine to actually kill the engine thread. This is a byproduct of P3LX calling
             onDraw on every frame, and P3LX needing to kill the engine thread from the
             thread that calls onDraw. We don't have the same requirements, so we mark
             the thread as needing shutdown (using stop() above) then immediately call
             onDraw to get it to actually shut down the thread. */
        lx.engine.onDraw();
        lx.dispose();
        LXPoint.resetIdCounter();
    }

    @Override
    public void dispose() {
        UI.shutdown();
    }

    private void loadLxComponents() {
        LXClassLoader.findWarps().stream().forEach(lx::registerWarp);
        LXClassLoader.findEffects().stream().forEach(lx::registerEffect);
        LXClassLoader.findPatterns().stream().forEach(lx::registerPattern);

        lx.registerPattern(heronarts.p3lx.pattern.SolidColorPattern.class);
    }

    @Override // ApplicationState.Provider
    public String showName() {
        return showName;
    }

    @Override // ApplicationState.Provider
    public OutputControl outputControl() {
        return outputControl;
    }

    @Override
    public void setWarning(String key, String message) {
        ConsoleWindow.setWarning(key, message);
        if (message != null) {
            WindowManager.showPersistent(ConsoleWindow.WINDOW_NAME);
        }
    }

}