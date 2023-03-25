package com.symmetrylabs.shows.traffircle;

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
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import com.symmetrylabs.slstudio.workspaces.Workspace;

public class TraffircleShow implements Show, HasWorkspace {
    public static final String SHOW_NAME = "traffircle";
    public static final String LOG_TAG = "[TraffircleShow] ";

    private static final String OBJ_FILENAME = "model.obj";
    private static final String OBJ_FIXTURE_TYPE_GROUNDRAY = "GroundRay";
    private static final String OBJ_FIXTURE_TYPE_GROUNDSTRING = "GroundString";
    private static final String OBJ_FIXTURE_TYPE_WALLSTRING = "WallString";

    private static final int PIXLITE_OUTPUT_COUNT = 16;
    private static final String[] PIXLITE_IPS = {
        "192.168.1.43",
        "192.168.1.42",
    };

    private static final int[] PIXLITE_UNIVERSES_PER_OUTPUT = {
        5,
        10,
    };

    private Workspace workspace;

    // list of fixtures per output, in order of (controller, output), not minding gaps
    protected static List<List<LXFixture>> fixturesPerOutput = new ArrayList<>();

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

    @Override
    public TraffircleModel buildModel() {

        List<Strip> groundRays = new ArrayList<>();
        List<Strip> groundStrings = new ArrayList<>();
        List<Strip> wallStrings = new ArrayList<>();

        ObjParser objParser = new ObjParser();
        objParser.parse(OBJ_FILENAME);

        for (ObjParser.ParsedFixture f : objParser.fixtures) {
            System.out.println(LOG_TAG + "Adding " + f.verts.size() + " points for fixture " + f.num + " with controller " + f.controller + " and output " + f.output);

            // make all the model points in output order first
            List<LXPoint> fixturePoints = new ArrayList<>();
            for (LXVector v : f.verts) {
                fixturePoints.add(new LXPoint(v.x, v.y, v.z));
            }

            List<LXFixture> outputFixtures = null;
            int controllerIdx = -1;
            int outputIdx = -1;
            if (f.output > 0) {
                if (f.controller == 0) {
                    controllerIdx = (f.output - 1) / PIXLITE_OUTPUT_COUNT;
                    outputIdx = (f.output - 1) % PIXLITE_OUTPUT_COUNT;
                }
                else {
                    controllerIdx = f.controller - 1;
                    outputIdx = f.output - 1;
                }
            }

            if (controllerIdx >= 0 && controllerIdx < fixturesByControllerOutput.size()) {
                fixturesByControllerOutput.get(controllerIdx).putIfAbsent(outputIdx, new ArrayList<>());
                outputFixtures = fixturesByControllerOutput.get(controllerIdx).get(outputIdx);
            }

            Strip strip = new Strip(null, new Strip.Metrics(fixturePoints.size()), fixturePoints);

            if (OBJ_FIXTURE_TYPE_GROUNDRAY.equals(f.type)) {
                groundRays.add(strip);
            }
            else if (OBJ_FIXTURE_TYPE_GROUNDSTRING.equals(f.type)) {
                groundStrings.add(strip);
            }
            else if (OBJ_FIXTURE_TYPE_WALLSTRING.equals(f.type)) {
                wallStrings.add(strip);
            }
            else {
                System.out.println(LOG_TAG + "Error: Unrecognized fixture type in model OBJ: " + f.type);
                outputFixtures = null;
            }

            if (outputFixtures != null) {
                outputFixtures.add(strip);
            }
        }

        for (Map<Integer, List<LXFixture>> fixturesByOutput : fixturesByControllerOutput) {
            for (List<LXFixture> outputFixtures : fixturesByOutput.values()) {
                fixturesPerOutput.add(outputFixtures);
            }
        }

        return new TraffircleModel(groundRays, groundStrings, wallStrings);
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
