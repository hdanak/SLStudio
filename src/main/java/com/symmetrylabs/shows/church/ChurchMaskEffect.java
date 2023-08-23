package com.symmetrylabs.shows.church;

import java.util.List;
import java.util.ArrayList;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.EnumParameter;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.effect.SLEffect;


public class ChurchMaskEffect extends SLEffect<ChurchShow.ChurchModel> {
    public static enum MaskMode { PASS, OFF, ON };

    public final EnumParameter<MaskMode> crossMaskParam = new EnumParameter<>("CrossMask", MaskMode.PASS);
    public final EnumParameter<MaskMode> haloMaskParam = new EnumParameter<>("HaloMask", MaskMode.PASS);
    public final EnumParameter<MaskMode> flowerMaskParam = new EnumParameter<>("FlowerMask", MaskMode.PASS);
    public final EnumParameter<MaskMode> aisleMaskParam = new EnumParameter<>("AisleMask", MaskMode.PASS);
    public final EnumParameter<MaskMode> stringMaskParam = new EnumParameter<>("StringMask", MaskMode.PASS);

    public ChurchMaskEffect(LX lx) {
        super(lx);

        addParameter(crossMaskParam);
        addParameter(haloMaskParam);
        addParameter(flowerMaskParam);
        addParameter(aisleMaskParam);
        addParameter(stringMaskParam);
    }

    private void applyMask(MaskMode mask, List<Strip> strips) {
        if (mask != MaskMode.PASS) {
            int c = LXColor.BLACK;
            if (mask == MaskMode.ON) {
                c = LXColor.WHITE;
            }

            for (Strip strip : strips) {
                setColor(strip, c);
            }
        }
    }

    @Override
    public void run(double deltaMs, double amount) {
        applyMask(crossMaskParam.getEnum(), model.crossStrips);
        applyMask(haloMaskParam.getEnum(), model.haloStrips);
        applyMask(flowerMaskParam.getEnum(), model.flowerStrips);
        applyMask(aisleMaskParam.getEnum(), model.aisleStrips);
        applyMask(stringMaskParam.getEnum(), model.stringStrips);
    }
}
