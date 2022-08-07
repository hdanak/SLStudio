package com.symmetrylabs.slstudio.render;

import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;

import java.util.ArrayList;
import java.util.List;

public abstract class Renderer {
    protected final LXFixture fixture;
    protected final int[] colors;
    protected final Renderable renderable;
    protected List<LXVector> vectors;

    public Renderer(LXFixture fixture, int[] colors, Renderable renderable) {
        this.fixture = fixture;
        this.colors = colors;
        this.renderable = renderable;

        vectors = new ArrayList<>();
    }

    // TODO: filter on points in fixture for per-fixture rendering
    public void setVectorList(List<LXVector> vectors) {
        this.vectors = vectors;
    }

    public void start() { }
    public void stop() { }
    public void run(double deltaMs) { }
}
