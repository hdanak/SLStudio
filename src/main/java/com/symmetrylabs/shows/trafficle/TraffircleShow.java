package com.symmetrylabs.shows.traffircle;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
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

    private static final int PIXLITE_OUTPUT_COUNT = 4;
    private static final String[] PIXLITE_IPS = {
        "10.200.1.130",
        "10.200.1.128",
        "10.200.1.129",
    };

    private Workspace workspace;

    // list of fixtures per output, where output starts from 1
    private Map<Integer, List<LXFixture>> fixturesPerOutput = new HashMap<>();

    @Override
    public TraffircleModel buildModel() {

        List<Strip> groundRays = new ArrayList<>();
        List<Strip> groundStrings = new ArrayList<>();
        List<Strip> wallStrings = new ArrayList<>();

        ObjParser objParser = new ObjParser();
        objParser.parse(OBJ_FILENAME);

        for (ObjParser.ParsedFixture f : objParser.fixtures) {
            System.out.println(LOG_TAG + "Adding " + f.verts.size() + " points for fixture " + f.num + " with output " + f.output);

            // make all the model points in output order first
            List<LXPoint> fixturePoints = new ArrayList<>();
            for (LXVector v : f.verts) {
                fixturePoints.add(new LXPoint(v.x, v.y, v.z));
            }

            List<LXFixture> outputFixtures = null;
            if (f.output > 0) {
                fixturesPerOutput.putIfAbsent(f.output, new ArrayList<LXFixture>());
                outputFixtures = fixturesPerOutput.get(f.output);
            }

            Strip strip = new Strip(null, new Strip.Metrics(fixturePoints.size()), fixturePoints);

            if ("GroundRay".equals(f.type)) {
                groundRays.add(strip);
            }
            else if ("GroundString".equals(f.type)) {
                groundStrings.add(strip);
            }
            else if ("WallString".equals(f.type)) {
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

        return new TraffircleModel(groundRays, groundStrings, wallStrings);
    }

    @Override
    public void setupLx(LX lx) {
        int baseOutput = 1;
        for (String ip : PIXLITE_IPS) {
            SimplePixlite pixlite = new SimplePixlite(lx, ip);

            for (int i = 0; i < PIXLITE_OUTPUT_COUNT; ++i) {
                if (fixturesPerOutput.containsKey(baseOutput + i)) {
                    List<LXFixture> fixtures = fixturesPerOutput.get(baseOutput + i);
                    List<LXPoint> points = new ArrayList<>();
                    for (LXFixture fixture : fixtures) {
                        points.addAll(fixture.getPoints());
                    }
                    points.sort((LXPoint p1, LXPoint p2) -> { return p1.index - p2.index; });
                    pixlite.addPixliteOutput(new PointsGrouping(i+1+"").addPoints(points));
                }
            }

            lx.addOutput(pixlite);
            baseOutput += PIXLITE_OUTPUT_COUNT;
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
