package com.symmetrylabs.slstudio.ui.gdx;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new SLStudioGDX(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.useOpenGL3(true, 3, 2);
        configuration.setTitle("SLStudio");
        configuration.setWindowedMode(640, 480);
        configuration.setWindowIcon("application.png");
        /* 8 bits per color, 0 bits for depth/stencil, 3 samples per pixel */
        configuration.setBackBufferConfig(8, 8, 8, 8, 0, 0, 3);
        configuration.enableGLDebugOutput(true, System.out);
        return configuration;
    }
}
