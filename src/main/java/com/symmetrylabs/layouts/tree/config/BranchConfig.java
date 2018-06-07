package com.symmetrylabs.layouts.tree.config;

import java.util.Collections;
import java.util.List;
import java.util.Arrays;

public class BranchConfig {
    public String ipAddress;
    public float x;
    public float y;
    public float z;
    public float azimuth;
    public float elevation;
    public float tilt;
    private TwigConfig[] twigs;

    public BranchConfig(String ipAddress, float x, float y, float z, float azimuth, float elevation, float tilt, TwigConfig[] twigs) {
        this.ipAddress = ipAddress;
        this.x = x;
        this.y = y;
        this.z = z;
        this.azimuth = azimuth;
        this.elevation = elevation;
        this.tilt = tilt;
        this.twigs = twigs;
    }

    public List<TwigConfig> getTwigs() {
        return Collections.unmodifiableList(Arrays.asList(twigs));
    }

    public TwigConfig getTwigAtIndex(int i) {
        return twigs[i];
    }
}