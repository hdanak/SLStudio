package com.symmetrylabs.shows.mindboggle;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;
import heronarts.lx.transform.LXTransform;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.HasWorkspace;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import com.symmetrylabs.slstudio.workspaces.Workspace;

public class MindBoggleShow implements Show, HasWorkspace {
    public static final String SHOW_NAME = "mindboggle";
    public static final String LOG_TAG = "[MindBoggleShow] ";

    private static final String OBJ_FILENAME = "model.obj";

    private static final int PIXLITE_OUTPUT_COUNT = 4;
    private static final String[] PIXLITE_IPS = {
        "10.200.1.128",
        "10.200.1.129",
        "10.200.1.130",
    };

    private Workspace workspace;

    // map from output => points, where output starts from 1
    private Map<Integer, List<LXPoint>> pointsPerOutput = new HashMap<>();

    public SLModel buildModel() {

        ObjParser objParser = new ObjParser();
        objParser.parse(OBJ_FILENAME);

        List<LXPoint> points = new ArrayList<>();
        for (ObjParser.FixtureObject f : objParser.fixtures) {

            List<LXPoint> outputPoints = null;
            if (f.output > 0) {
                pointsPerOutput.putIfAbsent(f.output, new ArrayList<LXPoint>());
                outputPoints = pointsPerOutput.get(f.output);
            }

            System.out.println(LOG_TAG + "Adding points for fixture " + f.num + " with output " + f.output);
            for (LXVector v : f.verts) {
                LXPoint p = new LXPoint(v.x, v.y, v.z);

                points.add(p);

                if (outputPoints != null) {
                    outputPoints.add(p);
                }
            }
        }

        /*
        for (ObjParser.ShapeObject s : objParser.shapes) {
            for (LXVector v : f.verts) {
                points.add(new LXPoint(v.x, v.y, v.z));
            }
        }
        */


        return new SLModel("mindboggle", points);
    }

    @Override
    public void setupLx(LX lx) {
        int baseOutput = 1;
        for (String ip : PIXLITE_IPS) {
            SimplePixlite pixlite = new SimplePixlite(lx, ip);

            for (int i = 0; i < PIXLITE_OUTPUT_COUNT; ++i) {
                if (pointsPerOutput.containsKey(baseOutput + i)) {
                    List<LXPoint> points = pointsPerOutput.get(baseOutput + i);
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
