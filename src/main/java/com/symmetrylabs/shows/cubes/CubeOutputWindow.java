package com.symmetrylabs.shows.cubes;

import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.slstudio.ui.v2.CloseableWindow;
import com.symmetrylabs.slstudio.ui.v2.ParameterUI;
import com.symmetrylabs.slstudio.ui.v2.UI;
import heronarts.lx.LX;
import com.symmetrylabs.slstudio.ApplicationState;
import java.util.Collection;
import com.symmetrylabs.slstudio.ui.v2.ComponentUI;

public class CubeOutputWindow extends CloseableWindow {
    private final LX lx;
    private final CubesModel model;
    private final CubesShow show;
    private final String[] featureIdBuffer = new String[32];

    public CubeOutputWindow(LX lx, CubesShow show) {
        super("Cube output");
        this.lx = lx;
        this.show = show;
        this.model = (CubesModel) lx.model;
    }

    @Override
    protected void drawContents() {
        ParameterUI.draw(lx, ApplicationState.outputControl().enabled);
        ParameterUI.draw(lx, ApplicationState.outputControl().testBroadcast);
        ParameterUI.draw(lx, ApplicationState.outputControl().controllerResetModule.enabled);

        UI.separator();

        Collection<CubesController> ccs = show.getSortedControllers();
        UI.text("%d controllers", ccs.size());
        boolean expand = UI.button("expand all");
        UI.sameLine();
        boolean collapse = UI.button("collapse all");

        for (CubesController cc : ccs) {
            if (expand) {
                UI.setNextTreeNodeOpen(true);
            } else if (collapse) {
                UI.setNextTreeNodeOpen(false);
            }

            boolean mapped = model.getCubeById(cc.id) != null;
            if (mapped) {
                UI.pushColor(UI.COLOR_HEADER, UI.BLUE);
                UI.pushColor(UI.COLOR_HEADER_ACTIVE, UI.BLUE);
                UI.pushColor(UI.COLOR_HEADER_HOVERED, UI.BLUE_HOVER);
            } else {
                UI.pushColor(UI.COLOR_HEADER, UI.RED);
                UI.pushColor(UI.COLOR_HEADER_ACTIVE, UI.RED);
                UI.pushColor(UI.COLOR_HEADER_HOVERED, UI.RED_HOVER);
            }
            UI.CollapseResult cr = UI.collapsibleSection(cc.id, false);
            UI.popColor(3);
            if (!cr.isOpen) {
                continue;
            }
            new ComponentUI(lx, cc, ParameterUI.WidgetType.SLIDER).draw();
            UI.labelText("Status", mapped ? "mapped" : "unmapped");
            NetworkDevice nd = cc.networkDevice;
            if (nd == null) {
                UI.text("(no network device)");
            } else {
                String version = nd.versionId;
                if (version.isEmpty()) {
                    version = String.format("%d*", nd.version.get());
                }
                UI.labelText("Version", version);
                UI.labelText("IP", nd.ipAddress.toString());
                UI.labelText("Product", nd.productId);
                UI.labelText("Device", nd.deviceId);
                UI.labelText("Features", String.join(",", nd.featureIds));
            }
        }
    }
}
