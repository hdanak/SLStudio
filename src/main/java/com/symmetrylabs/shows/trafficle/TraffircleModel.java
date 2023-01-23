package com.symmetrylabs.shows.traffircle;

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

public class TraffircleModel extends StripsModel {

    private List<Strip> groundRays = new ArrayList<>();
    private List<Strip> groundStrings = new ArrayList<>();
    private List<Strip> wallStrings = new ArrayList<>();
    private List<Strip> groundRaysUnmodifiable = Collections.unmodifiableList(groundRays);
    private List<Strip> groundStringsUnmodifiable = Collections.unmodifiableList(groundStrings);
    private List<Strip> wallStringsUnmodifiable = Collections.unmodifiableList(wallStrings);

    public TraffircleModel(List<Strip> groundRays, List<Strip> groundStrings, List<Strip> wallStrings) {
        super(TraffircleShow.SHOW_NAME, concatStrips(groundRays, groundStrings, wallStrings).toArray(new LXFixture[0]));
        //super(TraffircleShow.SHOW_NAME);

        this.groundRays.addAll(groundRays);
        this.groundStrings.addAll(groundStrings);
        this.wallStrings.addAll(wallStrings);

        strips.addAll(groundRays);
        strips.addAll(groundStrings);
        strips.addAll(wallStrings);
    }

    public List<Strip> getGroundRays() {
        return groundRaysUnmodifiable;
    }

    public List<Strip> getGroundStrings() {
        return groundStringsUnmodifiable;
    }

    public List<Strip> getWallStrings() {
        return wallStringsUnmodifiable;
    }

    private static List<Strip> concatStrips(List<Strip> as, List<Strip> bs, List<Strip> cs) {
        List<Strip> strips = new ArrayList<>();
        strips.addAll(as);
        strips.addAll(bs);
        strips.addAll(cs);
        strips.sort((Strip s1, Strip s2) -> { return s1.getPoints().get(0).index - s2.getPoints().get(0).index; });
        return strips;
    }
}
