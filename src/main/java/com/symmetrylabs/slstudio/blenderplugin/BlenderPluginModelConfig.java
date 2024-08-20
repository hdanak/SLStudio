package com.symmetrylabs.slstudio.blenderplugin;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Collections;
import java.util.Arrays;

import com.symmetrylabs.util.FileUtils;

import static com.symmetrylabs.util.MathUtils.dist;
import static com.symmetrylabs.util.MathUtils.dot;

public class BlenderPluginModelConfig {
    public static final String LOG_TAG = "[BlenderPluginModelConfig] ";

    // 3.28084 ?
    private static final double CURVE_OVERLAP_DIST_THRESH = 0.003 * 3;
    private static final double MIN_OVERLAP_LENGTH_THRESH = 0.1 * 3;

    public static class ModelJson {
        public String date;
        public String notes;
        public ConfigFixture[] fixtures;
        public ConfigController[] controllers;
    }
    public static class ConfigFixture {
        public String name;
        public Integer idx;
        public String type;
        public String colorType = "RGB";
        public Integer controllerIdx;
        public Integer outputIdx;
        public float[][] verts;
        public float[][] shapeVerts;
        public boolean detectFolds;
    }
    public static class ConfigController {
        public Integer idx;
        public ConfigOutput[] outputs;
    }
    public static class ConfigOutput {
        public Integer idx;
        public String type;
    }

    public static class ShapeSection {
        public enum Type {
            BRANCH, SPINE;
        }

        public final Type type;
        public final int startShapeVertexIndex;
        public final int endShapeVertexIndex;
        public final int length;

        public int startFixtureVertexIndex = -1;
        public int endFixtureVertexIndex = -1;

        public ShapeSection(Type type, int startShapeVertexIndex, int length) {
            this.type = type;
            this.startShapeVertexIndex = startShapeVertexIndex;
            this.endShapeVertexIndex = startShapeVertexIndex + length;
            this.length = length;
        }
    }

    public final List<ConfigFixture> fixtures;
    public final Map<Integer, ConfigFixture> fixtureByIdx;
    public final Map<String, List<ConfigFixture>> fixturesByType;

    // fixture separated into sections bases on where shape overlaps itself
    // each shape section has a range of fixture vertex indices
    public Map<Integer, List<ShapeSection>> shapeSectionsByFixtureIdx;
    public Map<Integer, int[]> vertIdxToShapeSegByFixtureIdx; // map of fixture vertex to shape segment index

    // list of output fixture lists per controller,
    //   e.g. fixturesList = fixturesByControllerOutput.get(controllerIdx).get(outputIdx)
    public final List<Map<Integer, List<ConfigFixture>>> fixturesByControllerOutput;
    public final List<Map<Integer, ConfigOutput>> controllerOutputConfigs;

    public BlenderPluginModelConfig(ModelJson json) {
        fixtures = new ArrayList<>();
        fixtureByIdx = new HashMap<>();
        fixturesByType = new HashMap<>();
        fixturesByControllerOutput = new ArrayList<>();
        controllerOutputConfigs = new ArrayList<>();

        shapeSectionsByFixtureIdx = new HashMap<>();
        vertIdxToShapeSegByFixtureIdx = new HashMap<>();

        Arrays.sort(json.fixtures, (f1, f2) -> f1.idx - f2.idx);

        for (ConfigFixture cf : json.fixtures) {
            fixtures.add(cf);

            if (cf.idx != null) {
                fixtureByIdx.put(cf.idx, cf);
            }

            if (cf.type != null) {
                fixturesByType.putIfAbsent(cf.type, new ArrayList<ConfigFixture>());
                fixturesByType.get(cf.type).add(cf);
            }

            if (cf.controllerIdx != null && cf.outputIdx != null) {
                while (fixturesByControllerOutput.size() < (cf.controllerIdx + 1)) {
                    // TreeMap stays sorted by key
                    fixturesByControllerOutput.add(new TreeMap<>());
                }

                fixturesByControllerOutput.get(cf.controllerIdx).putIfAbsent(cf.outputIdx, new ArrayList<>());
                fixturesByControllerOutput.get(cf.controllerIdx).get(cf.outputIdx).add(cf);
            }

            calcFixtureShapeMappings(cf);
        }

        for (ConfigController cc : json.controllers) {
            while (controllerOutputConfigs.size() < (cc.idx + 1)) {
                // TreeMap stays sorted by key
                controllerOutputConfigs.add(new TreeMap<>());
            }

            for (ConfigOutput co : cc.outputs) {
                controllerOutputConfigs.get(cc.idx).put(co.idx, co);
            }
        }
    }

    public static BlenderPluginModelConfig fromShowFile(String filename) {
        ModelJson json = FileUtils.readShowJson(filename, ModelJson.class);
        return new BlenderPluginModelConfig(json);
    }

    private void calcFixtureShapeMappings(ConfigFixture cf) {
        float[][] shapeVerts = cf.shapeVerts;
        if (shapeVerts == null) {
            shapeSectionsByFixtureIdx.put(cf.idx, new ArrayList<ShapeSection>());
            vertIdxToShapeSegByFixtureIdx.put(cf.idx, new int[0]);
            return;
        }

        // precalculate segment lengths and vector subtractions
        float[] segLengths = new float[shapeVerts.length - 1];
        float[] segDeltaXs = new float[shapeVerts.length - 1];
        float[] segDeltaYs = new float[shapeVerts.length - 1];
        float[] segDeltaZs = new float[shapeVerts.length - 1];
        for (int svi = 0; svi < shapeVerts.length - 1; ++svi) {
            float[] sv1 = shapeVerts[svi];
            float[] sv2 = shapeVerts[svi + 1];

            segLengths[svi] = dist(sv1, sv2);
            segDeltaXs[svi] = sv2[0] - sv1[0];
            segDeltaYs[svi] = sv2[1] - sv1[1];
            segDeltaZs[svi] = sv2[2] - sv1[2];
        }

        int[] fixtureVertIdxToShapeSeg = new int[cf.verts.length];

        // find closest shape segment to fixture vert
        int lastShapeVertIndex = 0;
        for (int fvi = 0; fvi < cf.verts.length; ++fvi) {
            float[] fv = cf.verts[fvi];

            int closestSegIndex = -1;
            float closestSegDist = Float.MAX_VALUE;

            for (int svi = lastShapeVertIndex; svi < shapeVerts.length - 1; ++svi) {
                float[] sv1 = shapeVerts[svi];
                float[] sv2 = shapeVerts[svi + 1];
                float segLength = segLengths[svi];
                float pointDist = -1;

                if (segLength < Float.MIN_VALUE) { // length is zero
                    pointDist = dist(sv1, fv);
                }
                else {
                    // find projection of fixture point onto shape segment
                    float x = fv[0] - sv1[0];
                    float y = fv[1] - sv1[1];
                    float z = fv[2] - sv1[2];
                    float t = dot(x, y, z, segDeltaXs[svi], segDeltaYs[svi], segDeltaZs[svi])
                                    / (segLength * segLength);

                    // closest point along segment
                    //if (t < 0) t = 0;
                    if (t > 1) t = 1;

                    if (t >= 0 && t <= 1) {
                        x = sv1[0] + t * segDeltaXs[svi];
                        y = sv1[1] + t * segDeltaYs[svi];
                        z = sv1[2] + t * segDeltaZs[svi];

                        pointDist = dist(fv[0], fv[1], fv[2], x, y, z);
                    }
                }

                if (pointDist != -1 && pointDist < closestSegDist) {
                    closestSegIndex = svi;
                    closestSegDist = pointDist;
                    //System.out.println("Check: idx=" + svi + " dist=" + pointDist);
                }
                else {
                    //System.out.println("Break: idx=" + svi + " dist=" + pointDist);
                    //break;
                }
            }


            if (closestSegIndex != -1) {
                //System.out.println(LOG_TAG + "Closest segment index for fixture point " + fvi + " in fixture " + (cf.idx + 1) + " is " + closestSegIndex + " at dist " + closestSegDist);
                //lastShapeVertIndex = closestSegIndex;
                fixtureVertIdxToShapeSeg[fvi] = closestSegIndex;
            }
            else {
                System.err.println(LOG_TAG + "Could not find shape segment for point " + fvi + " in fixture " + (cf.idx + 1));
                fixtureVertIdxToShapeSeg[fvi] = -1;
            }

        }

        // create inverse lookup
        Map<Integer, List<Integer>> fixtureVertIdxsByShapeSeg = new HashMap<>();
        for (int fvi = 0; fvi < fixtureVertIdxToShapeSeg.length; ++fvi) {
            int svi = fixtureVertIdxToShapeSeg[fvi];
            fixtureVertIdxsByShapeSeg.putIfAbsent(svi, new ArrayList<Integer>());
            fixtureVertIdxsByShapeSeg.get(svi).add(fvi);
        }

        // array of shape vert indices
        List<ShapeSection> shapeSections = new ArrayList<>();

        // detect parts of shape that overlap and parts between
        // very hacky right now, checks whether shape vert is near
        // previously seen segment rather than actually intersecting.
        // may not handle intersecting segments well
        boolean overlapMode = false;
        int lastOverlapIndex = -1;
        List<Integer> vertQueue = new ArrayList<>();
        for (int svi = 0; svi < shapeVerts.length; ++svi) {
            float[] v = shapeVerts[svi];

            int closestSegIndex = 0;
            float closestSegDist = Float.MAX_VALUE;

            boolean overlapFound = false;
            for (int i = 0; i < vertQueue.size() - 1; ++i) {
                float pointDist = -1;
                int vqi = vertQueue.get(i);

                float[] sv1 = shapeVerts[vqi];
                float[] sv2 = shapeVerts[vqi + 1];
                float segLength = segLengths[vqi];

                if (segLength < Float.MIN_VALUE) { // length is zero
                    pointDist = dist(sv1, v);
                }
                else {
                    // find projection of fixture point onto shape segment
                    float x = v[0] - sv1[0];
                    float y = v[1] - sv1[1];
                    float z = v[2] - sv1[2];
                    float t = dot(x, y, z, segDeltaXs[vqi], segDeltaYs[vqi], segDeltaZs[vqi])
                                    / (segLength * segLength);

                    // move projection to be along segment
                    if (t < 0) t = 0;
                    if (t > 1) t = 1;

                    // check if point is along segment
                    if (t >= 0 && t <= 1) {
                        // projection of point onto segment
                        x = sv1[0] + t * segDeltaXs[vqi];
                        y = sv1[1] + t * segDeltaYs[vqi];
                        z = sv1[2] + t * segDeltaZs[vqi];

                        pointDist = dist(v[0], v[1], v[2], x, y, z);
                    }
                }

                if (pointDist != -1 && pointDist < closestSegDist) {
                    closestSegIndex = i;
                    closestSegDist = pointDist;
                }

                if (pointDist != -1 && pointDist < CURVE_OVERLAP_DIST_THRESH) {
                    overlapFound = true;
                    lastOverlapIndex = i;
                    break;
                }
            }

            if (!vertQueue.isEmpty()) {
                //System.out.println(LOG_TAG + "Closest: svi=" + svi + " distance=" + closestSegDist + " (" + vertQueue.get(closestSegIndex) + ")");
            }

            if (!overlapMode && overlapFound) {
                //System.out.println(LOG_TAG + "Shape " + (cf.idx + 1) + " Overlap start: " + svi + " (" + vertQueue.get(lastOverlapIndex) + ", " + (vertQueue.get(lastOverlapIndex) + 1) + ")");
                overlapMode = true;
            }
            else if (overlapMode && !overlapFound) {
                //System.out.println(LOG_TAG + "Shape " + (cf.idx + 1) + " Overlap end: " + svi + " (" + vertQueue.get(lastOverlapIndex) + ", " + (vertQueue.get(lastOverlapIndex) + 1) + ")");

                // check if overlapping section is long enough to qualify
                double overlapLength = calcShapeSectionLength(shapeVerts, vertQueue.get(lastOverlapIndex), vertQueue.get(vertQueue.size() - 1));
                if (overlapLength > MIN_OVERLAP_LENGTH_THRESH) {
                    // points before lastOverlapIndex are a "SPINE" section
                    if (lastOverlapIndex > 0) {
                        shapeSections.add(new ShapeSection(ShapeSection.Type.SPINE,
                                vertQueue.get(0), lastOverlapIndex));
                    }

                    // points between lastOverlapIndex and the final point in
                    // vertQueue are a "BRANCH" section and contain all the
                    // overlapping points
                    shapeSections.add(new ShapeSection(ShapeSection.Type.BRANCH,
                            vertQueue.get(lastOverlapIndex),
                            vertQueue.size() - lastOverlapIndex));

                    vertQueue.clear();
                }
                else {
                    System.out.println("Did not meet overlap length threshold, shape " + (cf.idx + 1) + ": " + vertQueue.get(lastOverlapIndex) + " " + vertQueue.get(vertQueue.size() - 1));
                }

                overlapMode = false;
                lastOverlapIndex = -1;
            }

            vertQueue.add(svi);
        }

        if (!vertQueue.isEmpty()) {
            // remaining points are a "SPINE" section
            shapeSections.add(new ShapeSection(ShapeSection.Type.SPINE,
                    vertQueue.get(0), vertQueue.size()));
        }

        // set fixture vert range for each shape section
        if (fixtureVertIdxsByShapeSeg != null) {
            for (ShapeSection shapeSection : shapeSections) {
                shapeSection.startFixtureVertexIndex = -1;
                shapeSection.endFixtureVertexIndex = -1;

                for (int svi = shapeSection.startShapeVertexIndex; svi < shapeSection.endShapeVertexIndex; ++svi) {
                    List<Integer> vertIdxs = fixtureVertIdxsByShapeSeg.get(svi);
                    if (vertIdxs != null && !vertIdxs.isEmpty()) {
                        for (int vertIdx : vertIdxs) {
                            if (shapeSection.startFixtureVertexIndex == -1 || vertIdx < shapeSection.startFixtureVertexIndex) {
                                shapeSection.startFixtureVertexIndex = vertIdx;
                            }
                            if (shapeSection.endFixtureVertexIndex == -1 || vertIdx > shapeSection.endFixtureVertexIndex) {
                                shapeSection.endFixtureVertexIndex = vertIdx;
                            }
                        }
                    }
                }

                if (shapeSection.endFixtureVertexIndex != -1) {
                    ++shapeSection.endFixtureVertexIndex;
                }
            }
        }

        shapeSectionsByFixtureIdx.put(cf.idx, shapeSections);
        vertIdxToShapeSegByFixtureIdx.put(cf.idx, fixtureVertIdxToShapeSeg);
    }

    private double calcShapeSectionLength(float[][] shapeVerts, int i0, int i1) {
        double length = 0;
        for (int i = i0; i < i1; ++i) {
            length += dist(shapeVerts[i], shapeVerts[i + 1]);
        }
        return length;
    }
}
