package com.symmetrylabs.shows.release;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Collections;
import java.util.Arrays;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;

import com.symmetrylabs.shows.WorkspaceShow;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.output.ManualPixlite;
import com.symmetrylabs.slstudio.output.ArtNetCustomDmxDatagram;
import com.symmetrylabs.slstudio.blenderplugin.BlenderPluginModel;
import com.symmetrylabs.slstudio.blenderplugin.BlenderPluginModelConfig;

public class ReleaseShow extends WorkspaceShow {
    public static final String SHOW_NAME = "release";
    public static final String LOG_TAG = "[ReleaseShow] ";

    private static final String MODEL_CONFIG_FILENAME = "model.json";

    // PIXLITE_IPS and PIXLITE_UNIVERSES_PER_OUTPUT should be changed together
    private static final String[] PIXLITE_IPS = {
        "192.168.1.43",
    };
    private static final int[] PIXLITE_UNIVERSES_PER_OUTPUT = { 6 };

    private static final String FIXTURE_COLOR_TYPE_RGB = "RGB";
    private static final String FIXTURE_COLOR_TYPE_RGBWYP = "RGBWYP";
    private static final String FIXTURE_COLOR_TYPE_RGBW = "RGBW";
    private static final String OUTPUT_TYPE_PIXELS = "PIXELS";
    private static final String OUTPUT_TYPE_DMX = "DMX";

    private BlenderPluginModelConfig modelConfig = null;
    private BlenderPluginModel model = null;
    private List<ManualPixlite> pixlites = new ArrayList<>();

    public ReleaseShow() {
        super(SHOW_NAME);
    }

    @Override
    public BlenderPluginModel buildModel() {
        modelConfig = BlenderPluginModelConfig.fromShowFile(MODEL_CONFIG_FILENAME);
        model = BlenderPluginModel.fromModelConfig(SHOW_NAME, modelConfig);
        return model;
    }

    @Override
    public void setupLx(LX lx) {
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

            BlenderPluginModel.Fixture[] fixtureByPointIndex = new BlenderPluginModel.Fixture[model.getPoints().size()];
            for (BlenderPluginModel.Fixture fixture : model.fixtures) {
                for (LXPoint p : fixture.getPoints()) {
                    fixtureByPointIndex[p.index] = fixture;
                }
            }


            Map<Integer, List<BlenderPluginModel.Fixture>> fixturesByOutput
                    = model.fixturesByControllerOutput.get(controllerIdx);
            for (int outputIdx : fixturesByOutput.keySet()) {
                List<BlenderPluginModel.Fixture> fixtures = fixturesByOutput.get(outputIdx);
                List<LXPoint> points = new ArrayList<>();
                for (BlenderPluginModel.Fixture fixture : fixtures) {
                    points.addAll(fixture.getPoints());
                }
                points.sort((LXPoint p1, LXPoint p2) -> { return p1.index - p2.index; });

                final BlenderPluginModelConfig.ConfigOutput outputConfig
                    = modelConfig.controllerOutputConfigs.get(controllerIdx).get(outputIdx);

                pixlite.addPoints(outputIdx * universesPerOutput, points,
                    (int[] pointIndices, Integer universeIndex) -> {
                        String outputType = "GENERIC";
                        if (outputConfig != null) {
                            outputType = outputConfig.type;
                        }

                        switch (outputType) {
                        //case OUTPUT_TYPE_DMX:
                        default:
                            ArtNetCustomDmxDatagram.Builder builder = new ArtNetCustomDmxDatagram.Builder();

                            String lastColorType = null;
                            int start = 0;
                            int count = 0;
                            for (int i = 0; i < pointIndices.length; ++i) {
                                int pointIndex = pointIndices[i];
                                BlenderPluginModel.Fixture fixture = fixtureByPointIndex[pointIndex];
                                if (fixture.colorType != lastColorType) {
                                    if (count > 0) {
                                        int[] indicesSlice = start > 0 || count < pointIndices.length
                                                ? Arrays.copyOfRange(pointIndices, start, start + count)
                                                : pointIndices;
                                        builder.addSegment(indicesSlice, ArtNetCustomDmxDatagram.ColorType.fromString(lastColorType));
                                    }

                                    lastColorType = fixture.colorType;
                                    start = i;
                                    count = 1;
                                }
                                else {
                                    ++count;
                                }
                            }
                            if (count > 0) {
                                int[] indicesSlice = start > 0 || count < pointIndices.length
                                        ? Arrays.copyOfRange(pointIndices, start, start + count)
                                        : pointIndices;
                                builder.addSegment(indicesSlice, ArtNetCustomDmxDatagram.ColorType.fromString(lastColorType));
                            }

                            return builder.createDatagram(lx, ip, universeIndex);
                            //return new ArtNetDmxDatagram(lx, ip, pointIndices, universeIndex);
                        }
                    });
            }

            lx.addOutput(pixlite);
        }
    }
}
