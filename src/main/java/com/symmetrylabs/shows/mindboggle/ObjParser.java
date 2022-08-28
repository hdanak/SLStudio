package com.symmetrylabs.shows.mindboggle;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.math3.util.FastMath;

import heronarts.lx.transform.LXVector;

import com.symmetrylabs.util.FileUtils;

public class ObjParser {
    public static final String LOG_TAG = "[ObjParser] ";

    private static final double CURVE_OVERLAP_DIST_THRESH = 0.0001;
    private static final double MIN_OVERLAP_LENGTH_THRESH = 0.1;

    public static abstract class ParsedObject {
        public final String name;
        public final int num;
        public final List<LXVector> verts;

        public ParsedObject(String name, int num) {
            this.name = name;
            this.num = num;

            verts = new ArrayList<>();
        }
    }

    public static class ParsedShape extends ParsedObject {
        public ParsedShape(String name, int num) {
            super(name, num);
        }
    }

    public static class ParsedFixture extends ParsedObject {
        public final int output;

        public ParsedFixture(String name, int num, int output) {
            super(name, num);

            this.output = output;
        }
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

    public List<ParsedShape> shapes;
    public List<ParsedFixture> fixtures;

    public Map<Integer, ParsedShape> shapeByNum;
    public Map<Integer, ParsedFixture> fixtureByNum;

    // fixture separated into parts bases on where shape overlaps itself
    // each "part" is a list of fixture vertex indices
    public Map<Integer, List<ShapeSection>> shapeSectionsByNum;
    public Map<Integer, int[]> fixtureVertIdxToShapeSegByNum;

    private ParsedObject curObject;

    public ObjParser() {
        reset();
    }

    public void reset() {
        shapes = new ArrayList<>();
        fixtures = new ArrayList<>();

        shapeByNum = new HashMap<>();
        fixtureByNum = new HashMap<>();
        shapeSectionsByNum = new HashMap<>();
        fixtureVertIdxToShapeSegByNum = new HashMap<>();

        curObject = null;
    }

    private void closeObject() {
        if (curObject instanceof ParsedShape) {
            shapes.add((ParsedShape)curObject);
        }
        if (curObject instanceof ParsedFixture) {
            fixtures.add((ParsedFixture)curObject);
        }

        curObject = null;
    }

    public void parse(String showFilename) {
        reset();

        List<String> lines = FileUtils.readShowLines(showFilename);
        if (lines == null) {
            throw new RuntimeException("Could not read model OBJ file '" + showFilename + "'");
        }

        for (String line : lines) {
            line = line.trim();

            if (line.startsWith("#"))
                continue;

            if ("".equals(line))
                continue;

            String[] parts = line.split(" ");

            if ("g".equals(parts[0])) {
                closeObject();
            }
            else if ("o".equals(parts[0]) && parts.length > 1) {
                closeObject();

                int num = 0;
                String[] nameParts = parts[1].split("_");
                if (nameParts[0].matches("^(Fixture|Shape)\\.\\d+$")) {
                    num = Integer.parseInt(nameParts[0].split("\\.")[1], 10);
                }

                // object named like Fixture.001_Output.001
                if (parts[1].startsWith("Fixture")) {
                    int output = 0;
                    if (nameParts.length > 1 && nameParts[1].matches("^Output\\.\\d+$")) {
                        output = Integer.parseInt(nameParts[1].split("\\.")[1], 10);
                    }

                    ParsedFixture fixture = new ParsedFixture(parts[1], num, output);
                    curObject = fixture;
                    fixtureByNum.put(num, fixture);
                }
                // object named like Shape.001
                if (parts[1].startsWith("Shape")) {
                    ParsedShape shape = new ParsedShape(parts[1], num);
                    curObject = shape;
                    shapeByNum.put(num, shape);
                }
            }
            else if ("v".equals(parts[0]) && parts.length > 3) {
                curObject.verts.add(new LXVector(Float.parseFloat(parts[1]),
                            Float.parseFloat(parts[2]), Float.parseFloat(parts[3])));
            }
        }

        closeObject();

        shapes.sort((ParsedObject o1, ParsedObject o2) -> { return o1.num - o2.num; });
        fixtures.sort((ParsedObject o1, ParsedObject o2) -> { return o1.num - o2.num; });

        calcFixtureShapeMappings();
        //calcShapeParts();
    }

    private void calcFixtureShapeMappings() {
        for (ParsedShape shape : shapeByNum.values()) {
            ParsedFixture fixture = fixtureByNum.get(shape.num);

            if (fixture == null) {
                System.err.println(LOG_TAG + "Shape " + shape.num + " missing fixture, skipping.");
                continue;
            }

            // precalculate segment lengths and vector subtractions
            float[] segLengths = new float[shape.verts.size() - 1];
            float[] segDeltaXs = new float[shape.verts.size() - 1];
            float[] segDeltaYs = new float[shape.verts.size() - 1];
            float[] segDeltaZs = new float[shape.verts.size() - 1];
            for (int svi = 0; svi < shape.verts.size() - 1; ++svi) {
                LXVector sv1 = shape.verts.get(svi);
                LXVector sv2 = shape.verts.get(svi + 1);

                segLengths[svi] = sv1.dist(sv2);
                segDeltaXs[svi] = sv2.x - sv1.x;
                segDeltaYs[svi] = sv2.y - sv1.y;
                segDeltaZs[svi] = sv2.z - sv1.z;
            }

            int[] fixtureVertIdxToShapeSeg = new int[fixture.verts.size()];

            // find closest shape segment to fixture vert
            for (int fvi = 0; fvi < fixture.verts.size(); ++fvi) {
                LXVector fv = fixture.verts.get(fvi);

                int closestSegIndex = -1;
                int closestValidSegIndex = -1;
                float closestSegDist = Float.MAX_VALUE;
                float closestValidSegDist = Float.MAX_VALUE;

                for (int svi = 0; svi < shape.verts.size() - 1; ++svi) {
                    LXVector sv1 = shape.verts.get(svi);
                    LXVector sv2 = shape.verts.get(svi + 1);
                    float segLength = segLengths[svi];
                    float pointDist = -1;

                    if (segLength < Float.MIN_VALUE) { // length is zero
                        pointDist = sv1.dist(fv);
                    }
                    else {
                        // find projection of fixture point onto shape segment
                        LXVector tmp = new LXVector(fv.x - sv1.x, fv.y - sv1.y, fv.z - sv1.z);
                        float t = tmp.dot(segDeltaXs[svi], segDeltaYs[svi], segDeltaZs[svi])
                                        / (segLength * segLength);

                        // TODO: fix last point being off curve
                        if (t < 0 || t > 1) { // point is not along segment
                            continue;
                        }

                        tmp.x = sv1.x + t * segDeltaXs[svi];
                        tmp.y = sv1.y + t * segDeltaYs[svi];
                        tmp.z = sv1.z + t * segDeltaZs[svi];

                        pointDist = fv.dist(tmp);
                    }

                    if (pointDist != -1 && pointDist < closestSegDist) {
                        closestSegIndex = svi;
                        closestSegDist = pointDist;
                    }
                }

                if (closestSegIndex == -1) {
                    System.err.println(LOG_TAG + "Could not find shape segment for point " + fvi + " in fixture " + fixture.num);
                    fixtureVertIdxToShapeSeg[fvi] = -1;
                    continue;
                }

                fixtureVertIdxToShapeSeg[fvi] = closestSegIndex;
            }

            fixtureVertIdxToShapeSegByNum.put(shape.num, fixtureVertIdxToShapeSeg);

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
            for (int svi = 0; svi < shape.verts.size(); ++svi) {
                LXVector v = shape.verts.get(svi);

                boolean overlapFound = false;
                for (int i = 0; i < vertQueue.size() - 1; ++i) {
                    float pointDist = -1;
                    int vqi = vertQueue.get(i);

                    LXVector sv1 = shape.verts.get(vqi);
                    LXVector sv2 = shape.verts.get(vqi + 1);
                    float segLength = segLengths[vqi];

                    if (segLength < Float.MIN_VALUE) { // length is zero
                        pointDist = sv1.dist(v);
                    }
                    else {
                        // find projection of fixture point onto shape segment
                        LXVector tmp = new LXVector(v.x - sv1.x, v.y - sv1.y, v.z - sv1.z);
                        float t = tmp.dot(segDeltaXs[vqi], segDeltaYs[vqi], segDeltaZs[vqi])
                                        / (segLength * segLength);

                        // move projection to be along segment
                        if (t < 0) t = 0;
                        if (t > 1) t = 1;

                        // check if point is along segment
                        if (t >= 0 && t <= 1) {
                            // projection of point onto segment
                            tmp.x = sv1.x + t * segDeltaXs[vqi];
                            tmp.y = sv1.y + t * segDeltaYs[vqi];
                            tmp.z = sv1.z + t * segDeltaZs[vqi];

                            pointDist = v.dist(tmp);
                        }
                    }

                    if (pointDist != -1 && pointDist < CURVE_OVERLAP_DIST_THRESH) {
                        overlapFound = true;
                        lastOverlapIndex = i;
                        break;
                    }
                }

                if (!overlapMode && overlapFound) {
                    overlapMode = true;
                }
                else if (overlapMode && !overlapFound) {
                    // check if overlapping section is long enough to qualify
                    double overlapLength = calcShapeSectionLength(shape, vertQueue.get(lastOverlapIndex), vertQueue.get(vertQueue.size() - 1));
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
                        System.out.println("Did not meet overlap threshold, " + shape.num + " " + vertQueue.get(lastOverlapIndex) + " " + vertQueue.get(vertQueue.size() - 1));
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

            shapeSectionsByNum.put(shape.num, shapeSections);
        }
    }

    private double calcShapeSectionLength(ParsedShape shape, int i0, int i1) {
        double length = 0;
        for (int i = i0; i < i1; ++i) {
            length += shape.verts.get(i).dist(shape.verts.get(i + 1));
        }
        return length;
    }
}
