package com.symmetrylabs.shows.mindboggle;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXPoint;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.SLModel;

import static com.symmetrylabs.util.MathConstants.*;

public class MindBoggleModel extends StripsModel {

    private List<Neuron> neurons = new ArrayList<>();
    private List<Neuron> neuronsUnmodifiable = Collections.unmodifiableList(neurons);

    public MindBoggleModel(List<Neuron> neurons) {
        super(MindBoggleShow.SHOW_NAME, neurons.toArray(new LXFixture[0]));

        this.neurons.addAll(neurons);

        for (Neuron neuron : neurons) {
            strips.addAll(neuron.getStrips());
        }
    }

    public List<Neuron> getNeurons() {
        return neuronsUnmodifiable;
    }

    public static class Neuron extends StripsModel {
        private List<AxonSegment> axonSegments = new ArrayList<>();
        private List<AxonSegment> axonSegmentsUnmodifiable = Collections.unmodifiableList(axonSegments);
        private List<Dendrite> dendrites = new ArrayList<>();
        private List<Dendrite> dendritesUnmodifiable = Collections.unmodifiableList(dendrites);

        public Neuron(List<AxonSegment> axonSegments, List<Dendrite> dendrites) {
            super(null, concatStrips(axonSegments, dendrites));

            this.axonSegments.addAll(axonSegments);
            this.dendrites.addAll(dendrites);
        }

        private static List<Strip> concatStrips(List<AxonSegment> axonSegments, List<Dendrite> dendrites) {
            List<Strip> strips = new ArrayList<>();
            strips.addAll(axonSegments);
            strips.addAll(dendrites);
            strips.sort((Strip s1, Strip s2) -> { return s1.getPoints().get(0).index - s2.getPoints().get(0).index; });
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
}
