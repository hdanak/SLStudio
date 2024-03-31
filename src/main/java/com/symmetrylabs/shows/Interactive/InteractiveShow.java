package com.symmetrylabs.shows.interactive;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXFixture;
import heronarts.lx.transform.LXVector;
import heronarts.lx.transform.LXTransform;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.HasWorkspace;
import com.symmetrylabs.shows.mindboggle.ObjParser;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.CandyBar;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import com.symmetrylabs.slstudio.model.DoubleStrip;
import com.symmetrylabs.slstudio.workspaces.Workspace;

public class InteractiveShow implements Show, HasWorkspace {
    public static final String SHOW_NAME = "interactive";
    public static final String LOG_TAG = "[InteractiveShow] ";

    private static final String[] PIXLITE_IPS = {
        "192.168.1.42",
    };
    private static final int[] PIXLITE_UNIVERSES_PER_OUTPUT = {
        10,
    };

    private Workspace workspace;

    // list of output fixture lists per controller,
    //   e.g. fixturesList = fixturesByControllerOutput.get(controllerIndex).get(outputIndex)
    protected static List<Map<Integer, List<LXFixture>>> fixturesByControllerOutput;
    static {
        fixturesByControllerOutput = new ArrayList<Map<Integer, List<LXFixture>>>(PIXLITE_IPS.length);
        for (int i = 0; i < PIXLITE_IPS.length; ++i) {
            // TreeMap stays sorted by key
            fixturesByControllerOutput.add(new TreeMap<>());
        }
    }

    private List<LXFixture> getOutputFixtures(int controllerIdx, int outputIdx) {
        List<LXFixture> outputFixtures = null;
        if (controllerIdx < fixturesByControllerOutput.size()) {
            fixturesByControllerOutput.get(controllerIdx).putIfAbsent(outputIdx, new ArrayList<>());
            outputFixtures = fixturesByControllerOutput.get(controllerIdx).get(outputIdx);
        }
        return outputFixtures;
    }

    @Override
    public InteractiveModel buildModel() {
        LXTransform t = new LXTransform();

        List<Strip> crossStrips = new ArrayList<>();
        List<Strip> haloStrips = new ArrayList<>();
        List<Strip> flowerStrips = new ArrayList<>();
        List<Strip> aisleStrips = new ArrayList<>();
        List<Strip> stringStrips = new ArrayList<>();

        // cross
        float crossHeight = 0;
        {
            t.push();

            int controllerIdx = 0;
            int outputIdx = 0;

            // LEDs per segment on the cross, starting from the bottom and
            // moving up clockwise
            int[] segments = {32,  15,  3,  2,  3, 15,   9,  3,  2,  3,  9,  15,  3,  3,  3, 15, 32};
            // angles of segments in degrees
            float[] angles = {90, -90, 45, 45, 45, 45, -90, 45, 45, 45, 45, -90, 45, 45, 45, 45, -90};
            float pitch = 1.312f; // 30 LED/m => 1.312" pitch

            List<LXPoint> points = new ArrayList<>();

            for (int i = 0; i < segments.length; ++i) {
                t.rotateZ(angles[i] * Math.PI / 180.f);

                for (int j = 0; j < segments[i]; ++j) {
                    // 13th segment should be 2-LEDs long instead of 3
                    t.translate(i != 13 ? pitch : 2 * pitch / 3, 0, 0);
                    points.add(new LXPoint(t.x(), t.y(), t.z()));

                    if (t.y() > crossHeight) {
                        crossHeight = t.y();
                    }
                }

                t.translate(pitch, 0, 0);
            }

            // shift by half the width of the middle of cross
            float centerWidth = points.get(0).x - points.get(points.size() - 1).x;
            float halfCenterWidth = centerWidth / 2;
            for (LXPoint p : points) {
                p.x += halfCenterWidth;
            }

            Strip.Metrics stripMetrics = new Strip.Metrics(points.size(), pitch);
            Strip strip = new Strip(stripMetrics, points);
            crossStrips.add(strip);

            List<LXFixture> outputFixtures = getOutputFixtures(controllerIdx, outputIdx);
            if (outputFixtures != null) {
                outputFixtures.add(strip);
            }

            t.pop();
        }

        // halo, above the cross
        {
            t.push();

            int controllerIdx = 0;
            int outputIdx = 1;

            int count = 288;
            float pitch = 0.273f; // 144 LED/m => 0.273" pitch
            float heightAboveCross = 12; // inches above cross
            int direction = 1; // 1 => cw, -1 => ccw (looking down)

            List<LXPoint> points = new ArrayList<>();

            float xMax = Float.MIN_VALUE;
            float xMin = Float.MAX_VALUE;
            t.translate(0, crossHeight + heightAboveCross, 0);
            for (int i = 0; i < count; ++i) {
                t.rotateY(2 * Math.PI / count * direction);
                t.translate(pitch, 0, 0);
                points.add(new LXPoint(t.x(), t.y(), t.z()));

                if (t.x() > xMax) {
                    xMax = t.x();
                }
                if (t.x() < xMin) {
                    xMin = t.x();
                }
            }

            // shift back by radius
            float radius = (xMax - xMin) / 2;
            for (LXPoint p : points) {
                p.z += radius * direction;
            }

            Strip.Metrics stripMetrics = new Strip.Metrics(points.size(), pitch);
            Strip strip = new Strip(stripMetrics, points);
            haloStrips.add(strip);

            List<LXFixture> outputFixtures = getOutputFixtures(controllerIdx, outputIdx);
            if (outputFixtures != null) {
                outputFixtures.add(strip);
            }

            t.pop();
        }

        // flowers
        {
            t.push();

            int controllerIdx = 0;
            int outputIdx = 2;

            float heightAboveGround = 12; // inches above ground
            float distanceFromCross = 60; // inches from cross

            // coordinates of each flower on XY-plane, in inches
            // not including heightAboveGround
            int[] flowerX = {-8,  0,  8,
                             -8,  0,  8,
                             -8,  0,  8};
            int[] flowerY = { 8,  8,  8,
                              0,  0,  0,
                             -8, -8, -8};
            int pointsPerFlower = 10;
            float radius = 2; // radius of circle representing flower, in inches

            float pitch = 2 * (float)Math.PI * radius / pointsPerFlower;

            t.translate(0, heightAboveGround, -distanceFromCross);
            for (int i = 0; i < flowerX.length; ++i) {
                float x = flowerX[i];
                float y = flowerY[i];

                List<LXPoint> points = new ArrayList<>();

                t.push();
                t.translate(x, y, 0);

                for (int j = 0; j < pointsPerFlower; ++j) {
                    t.rotateZ(2 * Math.PI / pointsPerFlower);
                    t.push();
                    t.translate(radius, 0, 0);
                    points.add(new LXPoint(t.x(), t.y(), t.z()));
                    t.pop();
                }

                t.pop();

                Strip.Metrics stripMetrics = new Strip.Metrics(points.size(), pitch);
                Strip strip = new Strip(stripMetrics, points);
                flowerStrips.add(strip);

                List<LXFixture> outputFixtures = getOutputFixtures(controllerIdx, outputIdx);
                if (outputFixtures != null) {
                    outputFixtures.add(strip);
                }
            }

            t.pop();
        }

        // aisles
        {
            t.push();

            int leftControllerIdx = 0;
            int leftOutputIdx = 3;
            int rightControllerIdx = 0;
            int rightOutputIdx = 4;

            int count = 91; // LED count per strip
            float pitch = 1.312f; // 30 LED/m => 1.312" pitch
            float distanceFromCross = 120; // inches from cross
            float aisleWidth = 48; // distance between strips in inches

            t.translate(0, 0, -distanceFromCross);

            // left side
            List<LXPoint> points = new ArrayList<>();
            t.push();
            t.translate(-aisleWidth / 2, 0, 0);
            for (int i = 0; i < count; ++i) {
                points.add(new LXPoint(t.x(), t.y(), t.z()));
                t.translate(0, 0, -pitch);
            }
            t.pop();

            Strip.Metrics stripMetrics = new Strip.Metrics(points.size(), pitch);
            Strip strip = new Strip(stripMetrics, points);
            aisleStrips.add(strip);

            List<LXFixture> outputFixtures = getOutputFixtures(leftControllerIdx, leftOutputIdx);
            if (outputFixtures != null) {
                outputFixtures.add(strip);
            }

            // right side
            points.clear();
            t.push();
            t.translate(aisleWidth / 2, 0, 0);
            for (int i = 0; i < count; ++i) {
                points.add(new LXPoint(t.x(), t.y(), t.z()));
                t.translate(0, 0, -pitch);
            }
            t.pop();

            stripMetrics = new Strip.Metrics(points.size(), pitch);
            strip = new Strip(stripMetrics, points);
            aisleStrips.add(strip);

            outputFixtures = getOutputFixtures(rightControllerIdx, rightOutputIdx);
            if (outputFixtures != null) {
                outputFixtures.add(strip);
            }

            t.pop();
        }

        // strings
        {
            t.push();

            // controller and output index for each string
            int[] stringControllerIdx = {0, 0};
            int[] stringOutputIdx = {4, 5};

            float distanceFromCross = 60; // inches from cross

            // number of points in each string
            int[] stringPoints = {20, 20};
            // coordinates for start of each string on XZ-plane, in inches
            // not including distanceFromCross
            int[] stringX = {-36, 36};
            int[] stringZ = {0, 0};
            // angle of each string along vertical axis, in degrees
            int[] stringAngle = {45, -45};
            float pitch = 3; // distance between LEDs in inches

            t.translate(0, 0, -distanceFromCross);
            for (int i = 0; i < stringPoints.length; ++i) {
                int count = stringPoints[i];
                float x = stringX[i];
                float z = stringZ[i];
                float angle = stringAngle[i];

                int controllerIdx = stringControllerIdx[i];
                int outputIdx = stringOutputIdx[i];

                List<LXPoint> points = new ArrayList<>();

                t.push();
                t.translate(x, 0, z);
                t.rotateY(angle * Math.PI / 180);

                for (int j = 0; j < count; ++j) {
                    points.add(new LXPoint(t.x(), t.y(), t.z()));
                    t.translate(0, 0, -pitch);
                }

                t.pop();

                Strip.Metrics stripMetrics = new Strip.Metrics(points.size(), pitch);
                Strip strip = new Strip(stripMetrics, points);
                stringStrips.add(strip);

                List<LXFixture> outputFixtures = getOutputFixtures(controllerIdx, outputIdx);
                if (outputFixtures != null) {
                    outputFixtures.add(strip);
                }
            }

            t.pop();
        }

        return new InteractiveModel("interactive", crossStrips, haloStrips, flowerStrips, aisleStrips, stringStrips);
    }

    public static class InteractiveModel extends StripsModel {
        public final List<Strip> crossStrips, haloStrips, flowerStrips, aisleStrips, stringStrips;

        public InteractiveModel(String name, List<Strip> crossStrips,
                List<Strip> haloStrips, List<Strip> flowerStrips,
                List<Strip> aisleStrips, List<Strip> stringStrips) {

            super(name, concatStrips(crossStrips, haloStrips, flowerStrips,
                            aisleStrips, stringStrips).toArray(new LXFixture[0]));

            this.crossStrips = crossStrips;
            this.haloStrips = haloStrips;
            this.flowerStrips = flowerStrips;
            this.aisleStrips = aisleStrips;
            this.stringStrips = stringStrips;

            strips.addAll(crossStrips);
            strips.addAll(haloStrips);
            strips.addAll(flowerStrips);
            strips.addAll(aisleStrips);
            strips.addAll(stringStrips);
        }

        private static List<Strip> concatStrips(List<Strip> as, List<Strip> bs, List<Strip> cs, List<Strip> ds, List<Strip> es) {
            List<Strip> strips = new ArrayList<>();
            strips.addAll(as);
            strips.addAll(bs);
            strips.addAll(cs);
            strips.addAll(ds);
            strips.addAll(es);
            strips.sort((Strip s1, Strip s2) -> { return s1.getPoints().get(0).index - s2.getPoints().get(0).index; });
            return strips;
        }
    }

    @Override
    public void setupLx(LX lx) {
        for (int controllerIdx = 0; controllerIdx < PIXLITE_IPS.length; ++controllerIdx) {
            String ip = PIXLITE_IPS[controllerIdx];
            int universesPerOutput = PIXLITE_UNIVERSES_PER_OUTPUT[controllerIdx];
            SimplePixlite pixlite = new SimplePixlite(lx, ip, universesPerOutput);

            System.out.println("controllerIdx=" + controllerIdx);
            for (int outputIdx : fixturesByControllerOutput.get(controllerIdx).keySet()) {
                List<LXFixture> fixtures = fixturesByControllerOutput.get(controllerIdx).get(outputIdx);
                List<LXPoint> points = new ArrayList<>();
                for (LXFixture fixture : fixtures) {
                    points.addAll(fixture.getPoints());
                }
                points.sort((LXPoint p1, LXPoint p2) -> { return p1.index - p2.index; });
                pixlite.addPixliteOutput(new PointsGrouping(outputIdx+1+"").addPoints(points));
            }

            lx.addOutput(pixlite);
        }
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        workspace = new Workspace(lx, ui, "shows/" + SHOW_NAME);
        workspace.setRequestsBeforeSwitch(2); // needed for Vezer
    }

    @Override
    public void setupUi(LX lx) {
        workspace = new Workspace(lx, (SLStudioLX.UI) null, "shows/" + SHOW_NAME);
        workspace.setRequestsBeforeSwitch(2); // needed for Vezer
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }
}
