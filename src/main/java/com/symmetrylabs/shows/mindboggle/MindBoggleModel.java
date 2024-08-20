package com.symmetrylabs.shows.mindboggle;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;

import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXPoint;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.blenderplugin.BlenderPluginModelConfig;

import static com.symmetrylabs.util.MathConstants.*;

public class MindBoggleModel extends StripsModel {
    private static final String LOG_TAG = "[MindBoggleModel] ";

    public static final String FIXTURE_TYPE_NEURON = "NEURON";
    public static final String FIXTURE_TYPE_ROOT = "ROOT";
    public static final String FIXTURE_TYPE_PERIM = "PERIM";

    public final List<Map<Integer, List<LXFixture>>> fixturesByControllerOutput;
    private List<Neuron> neurons = new ArrayList<>();
    private List<Neuron> neuronsUnmodifiable = Collections.unmodifiableList(neurons);
    private List<Root> roots = new ArrayList<>();
    private List<Root> rootsUnmodifiable = Collections.unmodifiableList(roots);
    private List<Perim> perims = new ArrayList<>();
    private List<Perim> perimsUnmodifiable = Collections.unmodifiableList(perims);

    public MindBoggleModel(String name, List<Neuron> neurons, List<Root> roots, List<Perim> perims) {
        this(name, neurons, roots, perims, new ArrayList<>());
    }
    public MindBoggleModel(String name, List<Neuron> neurons, List<Root> roots, List<Perim> perims,
            List<Map<Integer, List<LXFixture>>> fixturesByControllerOutput) {
        super(name, concatFixtures(neurons, roots, perims));

        this.neurons.addAll(neurons);
        this.roots.addAll(roots);
        this.perims.addAll(perims);
        this.fixturesByControllerOutput = fixturesByControllerOutput;

        for (Neuron neuron : neurons) {
            strips.addAll(neuron.getStrips());
        }

        strips.addAll(roots);
        strips.addAll(perims);
    }

    private static LXFixture[] concatFixtures(List<Neuron> neurons, List<Root> roots, List<Perim> perims) {
        List<LXFixture> fixtures = new ArrayList<>();
        fixtures.addAll(neurons);
        fixtures.addAll(roots);
        fixtures.addAll(perims);

        fixtures.sort((f1, f2) -> {
            if (f1.getPoints().isEmpty())
                return 1;
            if (f2.getPoints().isEmpty())
                return -1;
            return f1.getPoints().get(0).index - f2.getPoints().get(0).index;
        });
        return fixtures.toArray(new LXFixture[0]);
    }

    public List<Neuron> getNeurons() {
        return neuronsUnmodifiable;
    }

    public List<Root> getRoots() {
        return rootsUnmodifiable;
    }

    public List<Perim> getPerims() {
        return perimsUnmodifiable;
    }

    public static class Neuron extends StripsModel {
        private List<AxonSegment> axonSegments = new ArrayList<>();
        private List<AxonSegment> axonSegmentsUnmodifiable = Collections.unmodifiableList(axonSegments);
        private List<Dendrite> dendrites = new ArrayList<>();
        private List<Dendrite> dendritesUnmodifiable = Collections.unmodifiableList(dendrites);

        public Neuron(String name, List<AxonSegment> axonSegments, List<Dendrite> dendrites) {
            super(name, concatStrips(axonSegments, dendrites));

            this.axonSegments.addAll(axonSegments);
            this.dendrites.addAll(dendrites);
        }

        private static List<Strip> concatStrips(List<AxonSegment> axonSegments, List<Dendrite> dendrites) {
            List<Strip> strips = new ArrayList<>();
            strips.addAll(axonSegments);
            strips.addAll(dendrites);
            strips.sort((s1, s2) -> s1.getPoints().get(0).index - s2.getPoints().get(0).index);
            return strips;
        }

        public List<AxonSegment> getAxonSegments() {
            return axonSegmentsUnmodifiable;
        }

        public List<Dendrite> getDendrites() {
            return dendritesUnmodifiable;
        }
    }

    public static class AxonSegment extends Strip {
        public AxonSegment(List<LXPoint> points) {
            super(null, new Strip.Metrics(points.size()), points);
        }
    }

    public static class Dendrite extends Strip {
        public Dendrite(List<LXPoint> points) {
            super(null, new Strip.Metrics(points.size()), points);
        }
    }

    public static class Root extends Strip {
        public Root(String name, List<LXPoint> points) {
            super(name, new Strip.Metrics(points.size()), points);
        }
    }

    public static class Perim extends Strip {
        public Perim(String name, List<LXPoint> points) {
            super(name, new Strip.Metrics(points.size()), points);
        }
    }

    public static MindBoggleModel fromModelConfig(String name, BlenderPluginModelConfig modelConfig) {
        List<Map<Integer, List<LXFixture>>> fixturesByControllerOutput = new ArrayList<>();
        List<Neuron> neurons = new ArrayList<>();
        List<Root> roots = new ArrayList<>();
        List<Perim> perims = new ArrayList<>();

        for (BlenderPluginModelConfig.ConfigFixture cf : modelConfig.fixtures) {
            LXFixture fixture = null;

            List<LXPoint> fixturePoints = new ArrayList<>();
            for (float[] v : cf.verts) {
                fixturePoints.add(new LXPoint(v[0], v[1], v[2]));
            }

            System.out.println("Creating fixture " + cf.name + " with " + fixturePoints.size() + " points");

            switch (cf.type) {
            case FIXTURE_TYPE_ROOT:
                Root root = new Root(cf.name, fixturePoints);
                roots.add(root);
                fixture = root;
                break;
            case FIXTURE_TYPE_PERIM:
                Perim perim = new Perim(cf.name, fixturePoints);
                perims.add(perim);
                fixture = perim;
                break;
            case FIXTURE_TYPE_NEURON:
                List<AxonSegment> axonSegments = new ArrayList<>();
                List<Dendrite> dendrites = new ArrayList<>();

                List<BlenderPluginModelConfig.ShapeSection> shapeSections
                        = modelConfig.shapeSectionsByFixtureIdx.get(cf.idx);
                for (BlenderPluginModelConfig.ShapeSection shapeSection : shapeSections) {
                    /*
                    System.out.println("ShapeSection type=" + shapeSection.type
                            + " shapeStart=" + shapeSection.startShapeVertexIndex
                            + " shapeEnd=" + shapeSection.endShapeVertexIndex
                            + " fixtureStart=" + shapeSection.startFixtureVertexIndex
                            + " fixtureEnd=" + shapeSection.endFixtureVertexIndex);
                    */

                    List<LXPoint> partPoints = new ArrayList<>();
                    for (int i = shapeSection.startFixtureVertexIndex;
                            i < shapeSection.endFixtureVertexIndex; ++i) {
                        partPoints.add(fixturePoints.get(i));
                    }

                    //System.out.println(partPoints.size() + " points");

                    if (partPoints.isEmpty())
                        continue;

                    if (shapeSection.type == BlenderPluginModelConfig.ShapeSection.Type.SPINE) {
                        System.out.println(LOG_TAG + "Adding axon for fixture " + (cf.idx + 1));
                        axonSegments.add(new AxonSegment(partPoints));
                    }
                    else if (shapeSection.type == BlenderPluginModelConfig.ShapeSection.Type.BRANCH) {
                        System.out.println(LOG_TAG + "Adding dendrite for fixture " + (cf.idx + 1));
                        dendrites.add(new Dendrite(partPoints));
                    }
                }

                System.out.println("Number of axons segments: " + axonSegments.size()
                                    + ", dendrites: " + dendrites.size());

                Neuron neuron = new Neuron(cf.name, axonSegments, dendrites);
                neurons.add(neuron);
                fixture = neuron;
                break;
            default:
                System.out.println("Unknown fixture type: " + cf.type);
            }

            if (fixture != null && cf.controllerIdx != null && cf.outputIdx != null) {
                while (fixturesByControllerOutput.size() < cf.controllerIdx + 1) {
                    // TreeMap stays sorted by key
                    fixturesByControllerOutput.add(new TreeMap<>());
                }

                fixturesByControllerOutput.get(cf.controllerIdx).putIfAbsent(cf.outputIdx, new ArrayList<>());
                fixturesByControllerOutput.get(cf.controllerIdx).get(cf.outputIdx).add(fixture);
            }
        }

        return new MindBoggleModel(name, neurons, roots, perims, fixturesByControllerOutput);
    }
}
