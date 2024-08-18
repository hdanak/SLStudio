package com.symmetrylabs.slstudio.blenderplugin;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;

import heronarts.lx.model.LXPoint;

import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.Strip;

public class BlenderPluginModel extends StripsModel<BlenderPluginModel.Fixture> {
    public static final String LOG_TAG = "[BlenderPluginModel] ";

    public static class Fixture extends Strip {
        public final String type;
        public final String colorType;

        public Fixture(BlenderPluginModelConfig.ConfigFixture cf, List<LXPoint> fixturePoints) {
            super(cf.name, new Strip.Metrics(fixturePoints.size()), fixturePoints);

            this.type = cf.type;
            this.colorType = cf.colorType;
        }
    }

    public final List<Fixture> fixtures;
    public final Map<String, List<Fixture>> fixturesByType;
    public final List<Map<Integer, List<Fixture>>> fixturesByControllerOutput;

    private BlenderPluginModel(String name, List<Fixture> fixtures,
            Map<String, List<Fixture>> fixturesByType,
            List<Map<Integer, List<Fixture>>> fixturesByControllerOutput) {

        super(name, fixtures);

        this.fixtures = fixtures;
        this.fixturesByType = fixturesByType;
        this.fixturesByControllerOutput = fixturesByControllerOutput;
    }

    public static BlenderPluginModel fromModelConfig(String name, BlenderPluginModelConfig modelConfig) {
        Map<String, List<Fixture>> fixturesByType = new HashMap<>();
        List<Map<Integer, List<Fixture>>> fixturesByControllerOutput = new ArrayList<>();
        List<Fixture> fixtures = new ArrayList<>();

        for (BlenderPluginModelConfig.ConfigFixture cf : modelConfig.fixtures) {
            List<LXPoint> fixturePoints = new ArrayList<>();
            for (float[] v : cf.verts) {
                fixturePoints.add(new LXPoint(v[0], v[1], v[2]));
            }

            Fixture fixture = new Fixture(cf, fixturePoints);
            fixtures.add(fixture);

            if (cf.type != null) {
                fixturesByType.putIfAbsent(cf.type, new ArrayList<>());
                fixturesByType.get(cf.type).add(fixture);
            }

            if (cf.controllerIdx != null && cf.outputIdx != null) {
                while (fixturesByControllerOutput.size() < cf.controllerIdx + 1) {
                    // TreeMap stays sorted by key
                    fixturesByControllerOutput.add(new TreeMap<>());
                }

                fixturesByControllerOutput.get(cf.controllerIdx).putIfAbsent(cf.outputIdx, new ArrayList<>());
                fixturesByControllerOutput.get(cf.controllerIdx).get(cf.outputIdx).add(fixture);
            }
        }

        return new BlenderPluginModel(name, fixtures, fixturesByType, fixturesByControllerOutput);
    }
}
