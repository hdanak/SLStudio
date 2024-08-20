package com.symmetrylabs.shows.mindboggle;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXFixture;

import com.symmetrylabs.shows.WorkspaceShow;
import com.symmetrylabs.slstudio.component.GammaExpander;
import com.symmetrylabs.slstudio.output.ManualPixlite;
import com.symmetrylabs.slstudio.blenderplugin.BlenderPluginModelConfig;

public class MindBoggleShow extends WorkspaceShow {
    public static final String SHOW_NAME = "mindboggle";
    public static final String LOG_TAG = "[MindBoggleShow] ";

    private static final String MODEL_CONFIG_FILENAME = "model.json";

    // PIXLITE_IPS and PIXLITE_UNIVERSES_PER_OUTPUT should be changed together
    private static final String[] PIXLITE_IPS = {
        "10.200.1.130",
        "10.200.1.128",
        "10.200.1.129",
    };
    // last one repeats for all remaining Pixlites
    private static final int[] PIXLITE_UNIVERSES_PER_OUTPUT = { 5 };

    private BlenderPluginModelConfig modelConfig = null;
    private MindBoggleModel model = null;
    private List<ManualPixlite> pixlites = new ArrayList<>();

    public MindBoggleShow() {
        super(SHOW_NAME);
    }

    @Override
    public MindBoggleModel buildModel() {
        modelConfig = BlenderPluginModelConfig.fromShowFile(MODEL_CONFIG_FILENAME);
        model = MindBoggleModel.fromModelConfig(SHOW_NAME, modelConfig);
        return model;
    }

    @Override
    public void setupLx(LX lx) {
        GammaExpander.getInstance(lx).enabled.setValue(false);

        pixlites.clear();

        for (int controllerIdx = 0; controllerIdx < model.fixturesByControllerOutput.size(); ++controllerIdx) {
            if (controllerIdx >= PIXLITE_IPS.length) {
                continue;
            }

            String ip = PIXLITE_IPS[controllerIdx];
            ManualPixlite pixlite = new ManualPixlite(lx, ip);
            pixlite.setLogConnections(false);
            pixlites.add(pixlite);

            int universesPerOutput = controllerIdx < PIXLITE_UNIVERSES_PER_OUTPUT.length
                    ? PIXLITE_UNIVERSES_PER_OUTPUT[controllerIdx]
                    : PIXLITE_UNIVERSES_PER_OUTPUT[PIXLITE_UNIVERSES_PER_OUTPUT.length - 1];

            Map<Integer, List<LXFixture>> fixturesByOutput
                    = model.fixturesByControllerOutput.get(controllerIdx);
            for (int outputIdx : fixturesByOutput.keySet()) {
                List<LXFixture> outputFixtures = fixturesByOutput.get(outputIdx);
                List<LXPoint> points = new ArrayList<>();
                for (LXFixture fixture : outputFixtures) {
                    points.addAll(fixture.getPoints());
                }
                points.sort((p1, p2) -> p1.index - p2.index);

                pixlite.addPoints((outputIdx + 1) * universesPerOutput, points);
            }

            lx.addOutput(pixlite);
        }
    }
}
