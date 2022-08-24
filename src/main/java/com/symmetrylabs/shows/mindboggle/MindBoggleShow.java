package com.symmetrylabs.shows.mindboggle;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;
import heronarts.lx.transform.LXTransform;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.HasWorkspace;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
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

    // list of fixtures per output, where output starts from 1
    private Map<Integer, List<MindBoggleModel.Neuron>> fixturesPerOutput = new HashMap<>();

    public MindBoggleModel buildModel() {
    //public SLModel buildModel() {

        ObjParser objParser = new ObjParser();
        objParser.parse(OBJ_FILENAME);

        List<MindBoggleModel.Neuron> fixtures = new ArrayList<>();
        for (ObjParser.ParsedFixture f : objParser.fixtures) {

            // make all the model points in output order first
            LXPoint[] fixturePoints = new LXPoint[f.verts.size()];
            for (int i = 0; i < f.verts.size(); ++i) {
                int[] fixtureVertIdxToShapeSeg = objParser.fixtureVertIdxToShapeSegByNum.get(f.num);
                if (fixtureVertIdxToShapeSeg == null || fixtureVertIdxToShapeSeg[i] == -1) {
                    continue;
                }

                LXVector v = f.verts.get(i);
                fixturePoints[i] = new LXPoint(v.x, v.y, v.z);
            }

            List<MindBoggleModel.Neuron> outputFixtures = null;
            if (f.output > 0) {
                fixturesPerOutput.putIfAbsent(f.output, new ArrayList<MindBoggleModel.Neuron>());
                outputFixtures = fixturesPerOutput.get(f.output);
            }

            System.out.println(LOG_TAG + "Adding points for fixture " + f.num + " with output " + f.output);

            List<MindBoggleModel.AxonSegment> axonSegments = new ArrayList<>();
            List<MindBoggleModel.Dendrite> dendrites = new ArrayList<>();

            List<ObjParser.ShapeSection> shapeSections = objParser.shapeSectionsByNum.get(f.num);
            for (ObjParser.ShapeSection shapeSection : shapeSections) {
                List<LXPoint> partPoints = new ArrayList<>();
                System.out.println("ShapeSection type=" + shapeSection.type
                        + " shapeStart=" + shapeSection.startShapeVertexIndex + " shapeEnd=" + shapeSection.endShapeVertexIndex
                        + " fixtureStart=" + shapeSection.startFixtureVertexIndex + " fixtureEnd=" + shapeSection.endFixtureVertexIndex);
                for (int i = shapeSection.startFixtureVertexIndex; i < shapeSection.endFixtureVertexIndex; ++i) {
                    partPoints.add(fixturePoints[i]);
                }

                System.out.println(partPoints.size() + " points");

                if (partPoints.isEmpty())
                    continue;

                if (shapeSection.type == ObjParser.ShapeSection.Type.SPINE) {
                    System.out.println(LOG_TAG + "Adding axon for fixture " + f.num);
                    axonSegments.add(new MindBoggleModel.AxonSegment(partPoints));
                }
                else if (shapeSection.type == ObjParser.ShapeSection.Type.BRANCH) {
                    System.out.println(LOG_TAG + "Adding dendrite for fixture " + f.num);
                    dendrites.add(new MindBoggleModel.Dendrite(partPoints));
                }
            }

            MindBoggleModel.Neuron neuron = new MindBoggleModel.Neuron(axonSegments, dendrites);
            fixtures.add(neuron);

            if (outputFixtures != null) {
                outputFixtures.add(neuron);
            }
        }

        return new MindBoggleModel(fixtures);
    }

    @Override
    public void setupLx(LX lx) {
        int baseOutput = 1;
        for (String ip : PIXLITE_IPS) {
            SimplePixlite pixlite = new SimplePixlite(lx, ip);

            for (int i = 0; i < PIXLITE_OUTPUT_COUNT; ++i) {
                if (fixturesPerOutput.containsKey(baseOutput + i)) {
                    List<MindBoggleModel.Neuron> fixtures = fixturesPerOutput.get(baseOutput + i);
                    List<LXPoint> points = new ArrayList<>();
                    for (MindBoggleModel.Neuron fixture : fixtures) {
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
