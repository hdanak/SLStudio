package com.symmetrylabs.shows.mindboggle;

import java.util.List;
import java.util.ArrayList;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.EnumParameter;

import com.symmetrylabs.slstudio.effect.SLEffect;


public class NeuronMaskEffect extends SLEffect<MindBoggleModel> {
    public static enum MaskMode { PASS, OFF, ON };

    public final EnumParameter<MaskMode> dendriteMaskParam = new EnumParameter<>("DendriteMask", MaskMode.PASS);
    public final EnumParameter<MaskMode> axonMaskParam = new EnumParameter<>("AxonMask", MaskMode.PASS);

    public NeuronMaskEffect(LX lx) {
        super(lx);

        addParameter(dendriteMaskParam);
        addParameter(axonMaskParam);
    }

    @Override
    public void run(double deltaMs, double amount) {
        for (MindBoggleModel.Neuron neuron : model.getNeurons()) {
            if (dendriteMaskParam.getEnum() != MaskMode.PASS) {
                int c = LXColor.BLACK;
                if (dendriteMaskParam.getEnum() == MaskMode.ON) {
                    c = LXColor.WHITE;
                }

                for (MindBoggleModel.Dendrite fixture : neuron.getDendrites()) {
                    setColor(fixture, c);
                }
            }
            if (axonMaskParam.getEnum() != MaskMode.PASS) {
                int c = LXColor.BLACK;
                if (axonMaskParam.getEnum() == MaskMode.ON) {
                    c = LXColor.WHITE;
                }

                for (MindBoggleModel.AxonSegment fixture : neuron.getAxonSegments()) {
                    setColor(fixture, c);
                }
            }
        }
    }
}
