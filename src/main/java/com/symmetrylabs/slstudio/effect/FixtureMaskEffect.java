package com.symmetrylabs.slstudio.effect;

import java.util.List;
import java.util.ArrayList;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.model.LXFixture;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.EnumParameter;


public class FixtureMaskEffect extends LXEffect {
    public static enum MaskMode { PASS, OFF, ON };

    public final List<EnumParameter<MaskMode>> maskParams = new ArrayList<>();

    private final List<LXFixture> fixtures;

    public FixtureMaskEffect(LX lx) {
        super(lx);

        fixtures = getFixtures();
        for (int i = 0; i < fixtures.size(); ++i) {
            EnumParameter<MaskMode> param = new EnumParameter<>("fixture" + (i+1), MaskMode.PASS);
            maskParams.add(param);
            addParameter(param);
        }
    }

    public List<LXFixture> getFixtures() {
        return model.fixtures;
    }

    @Override
    public void run(double deltaMs, double amount) {
        int i = -1;
        for (EnumParameter<MaskMode> param : maskParams) {
            ++i;

            if (param.getEnum() == MaskMode.PASS)
                continue;

            int c = LXColor.BLACK;
            if (param.getEnum() == MaskMode.ON) {
                c = LXColor.WHITE;
            }

            setColor(fixtures.get(i), c);
        }
    }
}
