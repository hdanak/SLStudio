package com.symmetrylabs.shows.flowers;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;
import java.util.Arrays;
import java.util.List;

public class FlowerInspector extends FlowerPattern implements UIFlowerTool.SelectionListener {
    public static final String GROUP_NAME = FlowersShow.SHOW_NAME;

    private final DiscreteParameter flowerParam;

    private static final int[] PANEL_COLORS = new int[] {
        0xFFFF0000,
        0xFFFFFF00,
        0xFF00FF00,
        0xFF00FFFF,
        0xFF0000FF,
        0xFFFF00FF,
    };

    public FlowerInspector(LX lx) {
        super(lx);
        flowerParam = new DiscreteParameter("flower", 0, 0, model.getFlowers().size());
        addParameter(flowerParam);
    }

    private FlowerModel get() {
        return model.getFlowers().get(flowerParam.getValuei());
    }

    @Override
    public void run(double elapsedMs) {
        Arrays.fill(colors, 0);
        for (FlowerModel fm : model.getFlowers()) {
            FlowerData fd = fm.getFlowerData();
            if (fd.record.panelId != null) {
                for (LXPoint p : fm.points) {
                    colors[p.index] = PANEL_COLORS[fd.record.panelId.hashCode() % PANEL_COLORS.length];
                }
            }
        }
        for (LXPoint p : get().points) {
            colors[p.index] = 0xFFFFFFFF;
        }
    }

    @Override
    public String getCaption() {
        return get().getFlowerData().toString();
    }

    @Override
    public void onActive() {
        super.onActive();
        UIFlowerTool.get().addListener(this);
    }

    @Override
    public void onInactive() {
        super.onInactive();
        UIFlowerTool.get().removeListener(this);
    }

    @Override
    public void onFlowerSelected(FlowerData data) {
        List<FlowerModel> flowers = model.getFlowers();
        for (int i = 0; i < flowers.size(); i++) {
            if (flowers.get(i).getFlowerData().record.id == data.record.id) {
                flowerParam.setValue(i);
                return;
            }
        }
    }
}
