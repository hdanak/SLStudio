package com.symmetrylabs.shows;

import heronarts.lx.LX;

import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.workspaces.Workspace;

public abstract class WorkspaceShow implements Show, HasWorkspace {
    private String showName;
    private Workspace workspace;

    public WorkspaceShow(String showName) {
        this.showName = showName;
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        workspace = new Workspace(lx, ui, "shows/" + showName);
        workspace.setRequestsBeforeSwitch(2); // needed for Vezer
    }

    @Override
    public void setupUi(LX lx) {
        workspace = new Workspace(lx, (SLStudioLX.UI) null, "shows/" + showName);
        workspace.setRequestsBeforeSwitch(2); // needed for Vezer
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }
}
