package com.symmetrylabs.shows.flowers;

import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UI2dScrollContext;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIDoubleBox;
import heronarts.p3lx.ui.component.UIIntegerBox;
import heronarts.p3lx.ui.component.UIItemList;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.component.UITextBox;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import processing.core.PConstants;
import java.util.ArrayList;
import java.util.List;

public class UIFlowerTool extends UI2dContainer {
    private static UIFlowerTool INSTANCE;

    public static UIFlowerTool attach(SLStudioLX lx, SLStudioLX.UI ui) {
        INSTANCE = new UIFlowerTool(lx, ui);
        INSTANCE.addToContainer(ui.rightPane.model);
        return INSTANCE;
    }

    static UIFlowerTool get() {
        if (INSTANCE == null) {
            throw new RuntimeException("UIFlowerTool needs to be attached first");
        }
        return INSTANCE;
    }

    public interface Listener {
        void onFlowerSelected(FlowerData data);
        void onUpdate();
    }

    private final SLStudioLX lx;
    private final SLStudioLX.UI ui;
    private final FlowersModel model;
    private final FlowerEditor flowerEditor;
    private final UIItemList.ScrollList flowerList;
    private final UICollapsibleSection flowers;
    private final List<Listener> listeners = new ArrayList<>();
    private final BooleanParameter saveParam =
        new BooleanParameter("save").setMode(BooleanParameter.Mode.MOMENTARY);
    private final BooleanParameter runPanelizerParam =
        new BooleanParameter("panelizer").setMode(BooleanParameter.Mode.MOMENTARY);

    public UIFlowerTool(SLStudioLX lx, SLStudioLX.UI ui) {
        super(0, 0, ui.rightPane.model.getContentWidth(), 1000);
        this.lx = lx;
        this.ui = ui;
        model = (FlowersModel) lx.model;

        flowers = new UICollapsibleSection(ui, 0, 0, getContentWidth(), 350);
        flowers.setTitle("FLOWERS");

        new UIButton(0, 2, flowers.getContentWidth(), 20)
            .setParameter(saveParam)
            .setLabel("SAVE ALL")
            .addToContainer(flowers);
        saveParam.addListener(p -> model.storeRecords());

        new UIButton(0, 24, flowers.getContentWidth(), 20)
            .setParameter(runPanelizerParam)
            .setLabel("RUN PANELIZER")
            .addToContainer(flowers);
        runPanelizerParam.addListener(p -> model.panelize());

        flowerList =
            new UIItemList.ScrollList(
                ui, 0, 52, flowers.getContentWidth(), 172);
        for (FlowerModel fm : model.getFlowers()) {
            flowerList.addItem(new FlowerItem(fm));
        }
        flowerList.addToContainer(flowers);

        flowerEditor = new FlowerEditor();
        flowerEditor.addToContainer(flowers);

        addTopLevelComponent(flowers);
    }

    private void onUpdate() {
        flowerList.redraw();
        for (Listener l : listeners) {
            l.onUpdate();
        }
    }

    private void setCurrent(FlowerModel fm) {
        flowerEditor.setCurrent(fm);
        for (Listener l : listeners) {
            l.onFlowerSelected(fm.getFlowerData());
        }
    }

    public void addListener(Listener l) {
        listeners.add(l);
    }

    public void removeListener(Listener l) {
        listeners.remove(l);
    }

    class FlowerEditor extends UI2dContainer {
        FlowerModel currentModel;
        FlowerData current;
        UILabel id;
        UITextBox panel;
        UIIntegerBox pixliteId;
        UIIntegerBox harnessIndex;
        UIDoubleBox yOverride;
        UIButton overrideHeight;
        UIIntegerBox harness;

        StringParameter pP = new StringParameter("P");
        DiscreteParameter pPX = new DiscreteParameter("PX", 0, 0, 256);
        DiscreteParameter pH = new DiscreteParameter("H", FlowerRecord.UNKNOWN_HARNESS, FlowerRecord.UNKNOWN_HARNESS, 5);
        BooleanParameter pHA = new BooleanParameter("HA");
        BooleanParameter pHB = new BooleanParameter("HB");
        DiscreteParameter pHI = new DiscreteParameter("HI", 0, -1, 10);
        BooleanParameter pOH = new BooleanParameter("OH");
        CompoundParameter pYO = new CompoundParameter("YO", 0, -1000, 1000);

        boolean loading = false;

        public FlowerEditor() {
            super(0, 236, flowers.getContentWidth(), 80);
            id = new UILabel(0, 0, getContentWidth(), 10);
            id.setLabel("Select a flower");
            id.setTextAlignment(PConstants.LEFT, PConstants.TOP).setTextOffset(0,  1);
            id.addToContainer(this);

            pP.addListener(p -> onUpdate());
            pHI.setFormatter(v -> Integer.toString((int) v)).addListener(p -> onUpdate());
            pH.setFormatter(v -> Integer.toString((int) v)).addListener(p -> onUpdate());
            pOH.addListener(p -> {
                    yOverride.setEnabled(pOH.getValueb());
                    onUpdate();
                });
            pYO.addListener(p -> onUpdate());
            pPX.setFormatter(v -> Integer.toString((int) v)).addListener(p -> onUpdate());

            float LH = 10;
            float H = 20;
            float PW = 40;
            float PP = 12;
            float RP = 4;
            float Y1 = id.getHeight() + RP;
            float Y2 = Y1 + LH + RP;
            float Y3 = Y2 + H + RP;
            float Y4 = Y3 + LH + RP;

            new UILabel(0, Y1, PW, LH).setLabel("PANEL").addToContainer(this);
            new UILabel(PW + PP, Y1, PW, LH).setLabel("PIXLITE").addToContainer(this);
            new UILabel(2 * (PW + PP), Y1, getContentWidth() - PW, LH)
                .setLabel("HARNESS").addToContainer(this);

            panel = new UITextBox(0, Y2, PW, H);
            panel.setParameter(pP);
            panel.addToContainer(this);
            panel.setEnabled(false);

            pixliteId = new UIIntegerBox(PW + PP, Y2, PW, H);
            pixliteId.setParameter(pPX);
            pixliteId.addToContainer(this);
            pixliteId.setEnabled(false);

            harness = new UIIntegerBox(2 * (PW + PP), Y2, PW, H);
            harness.setParameter(pH);
            harness.addToContainer(this);
            harness.setEnabled(false);

            harnessIndex = new UIIntegerBox(3 * (PW + PP), Y2, PW, H);
            harnessIndex.setParameter(pHI);
            harnessIndex.addToContainer(this);
            harnessIndex.setEnabled(false);

            new UILabel(0, Y3, getContentWidth(), LH)
                .setLabel("OVERRIDE HEIGHT").addToContainer(this);

            overrideHeight = new UIButton(0, Y4, H, H);
            overrideHeight.setLabel("").setParameter(pOH);
            overrideHeight.addToContainer(this);
            overrideHeight.setEnabled(false);

            yOverride = new UIDoubleBox(H + 4, Y4, PW, H);
            yOverride.setParameter(pYO);
            yOverride.addToContainer(this);
            yOverride.setEnabled(false);
        }

        void setCurrent(FlowerModel fm) {
            currentModel = fm;
            current = fm.getFlowerData();
            loading = true;

            id.setLabel(String.format("FLOWER %04d", current.record.id));
            pP.setValue(current.record.panelId == null ? "" : current.record.panelId);
            pPX.setValue(current.record.pixliteId);
            pH.setValue(current.record.harness);
            pHI.setValue(current.record.harnessIndex);
            pYO.setValue(current.record.yOverride);
            pOH.setValue(current.record.overrideHeight);

            loading = false;

            panel.setEnabled(true);
            pixliteId.setEnabled(true);
            harness.setEnabled(true);
            harnessIndex.setEnabled(true);
            yOverride.setEnabled(pOH.getValueb());
            overrideHeight.setEnabled(true);

            redraw();
        }

        void onUpdate() {
            if (loading) {
                return;
            }
            current.record.panelId = "".equals(pP.getString()) ? null : pP.getString();
            current.record.pixliteId = pPX.getValuei();
            current.record.harness = pH.getValuei();
            current.record.harnessIndex = pHI.getValuei();
            current.record.overrideHeight = pOH.getValueb();
            current.record.yOverride = pYO.getValuef();
            current.recalculateLocation();
            currentModel.onDataUpdated();
            UIFlowerTool.this.onUpdate();
            ui.preview.pointCloud.updateVertexPositions();
        }
    }

    class FlowerItem extends UIItemList.AbstractItem {
        private final FlowerModel model;

        FlowerItem(FlowerModel model) {
            this.model = model;
        }

        @Override
        public String getLabel() {
            return model.getFlowerData().record.toString();
        }

        @Override
        public void onFocus() {
            UIFlowerTool.this.setCurrent(model);
        }
    }
}
