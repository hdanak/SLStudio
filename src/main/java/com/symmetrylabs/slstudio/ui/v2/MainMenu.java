package com.symmetrylabs.slstudio.ui.v2;

import com.symmetrylabs.shows.ShowRegistry;
import com.symmetrylabs.slstudio.SLStudio;
import heronarts.lx.LX;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class MainMenu implements Window {
    private final LX lx;
    private final SLStudioGDX parent;

    public MainMenu(LX lx, SLStudioGDX parent) {
        this.lx = lx;
        this.parent = parent;
    }

    public void draw() {
        if (!UI.beginMainMenuBar()) {
            return;
        }
        if (UI.beginMenu("Project")) {
            if (UI.menuItem("New")) {
                lx.engine.addTask(() -> lx.newProject());
            }
            if (UI.menuItem("Open...")) {
                EventQueue.invokeLater(() -> {
                        FileDialog dialog = new FileDialog(
                            (Frame) null, "Open project", FileDialog.LOAD);
                        dialog.setVisible(true);
                        String fname = dialog.getFile();
                        if (fname == null) {
                            return;
                        }

                        final File project = new File(dialog.getDirectory(), fname);
                        WindowManager.get().disableUI();
                        lx.engine.addTask(() -> {
                                lx.openProject(project);
                                WindowManager.get().enableUI();
                            });
                    });
            }
            if (UI.menuItem("Save")) {
                if (lx.getProject() == null) {
                    runSaveAs();
                } else {
                    lx.saveProject();
                }
            }
            if (UI.menuItem("Save As...")) {
                runSaveAs();
            }
            UI.endMenu();
        }
        if (UI.beginMenu("Show")) {
            for (String showName : ShowRegistry.getNames()) {
                if (UI.menuItem(showName)) {
                    try {
                        Files.write(Paths.get(SLStudio.SHOW_FILE_NAME), showName.getBytes());
                        parent.loadShow(showName);
                        break;
                    } catch (IOException e) {
                        System.err.println("couldn't write new show: " + e.getMessage());
                    }
                }
            }
            UI.endMenu();
        }
        if (UI.beginMenu("Window")) {
            WindowManager wm = WindowManager.get();
            /* Iterate over entries to preserve order */
            for (WindowManager.PersistentWindow ws : wm.getSpecs()) {
                if (UI.checkbox(ws.name, ws.current != null)) {
                    wm.showPersistent(ws);
                } else {
                    wm.hidePersistent(ws);
                }
            }
            UI.endMenu();
        }
        UI.endMainMenuBar();
    }

    private void runSaveAs() {
        EventQueue.invokeLater(() -> {
                FileDialog dialog = new FileDialog(
                    (Frame) null, "Save project", FileDialog.SAVE);
                dialog.setVisible(true);
                String fname = dialog.getFile();
                if (fname == null) {
                    return;
                }

                final File project = new File(dialog.getDirectory(), fname);
                lx.engine.addTask(() -> lx.saveProject(project));
            });
    }
}