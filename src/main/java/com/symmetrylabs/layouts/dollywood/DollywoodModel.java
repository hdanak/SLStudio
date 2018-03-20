package com.symmetrylabs.layouts.dollywood;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.transform.LXTransform;

import com.symmetrylabs.layouts.oslo.TreeModel;
import processing.core.PApplet;

import static com.symmetrylabs.util.MathUtils.*;
import static com.symmetrylabs.util.DistanceConstants.*;
import static com.symmetrylabs.util.MathConstants.*;

/**
 * Top-level model of the entire sculpture. This contains a list of
 * every cube on the sculpture, which forms a hierarchy of faces, strips
 * and points.
 */
public class DollywoodModel extends StripsModel<DollywoodModel.Wing> {
    public final TreeModel treeModel;

    protected final List<Butterfly> butterflies = new ArrayList<>();
    protected final Map<String, Butterfly> butterflyTable = new HashMap<>();

    protected final List<Wing> wings = new ArrayList<>();
    protected final Map<String, Wing> wingTable = new HashMap<>();

    private final List<Butterfly> butterfliesUnmodifiable = Collections.unmodifiableList(butterflies);
    private final List<Wing> wingsUnmodifiable = Collections.unmodifiableList(wings);

    private final Butterfly[] _butterflies;

    // public DollywoodModel() {
    //   this(new ArrayList<>(), new Butterfly[0]);
    // }

    public DollywoodModel(PApplet applet, List<Butterfly> butterflies, Butterfly[] butterflyArr) {
        super(new Fixture(applet, butterflyArr));
        Fixture fixture = (Fixture) this.fixtures.get(0);
        this.treeModel = fixture.treeModel;

        _butterflies = butterflyArr;

        for (Butterfly butterfly : butterflies) {
            if (butterfly != null) {
                this.butterflyTable.put(butterfly.id, butterfly);
                this.butterflies.add(butterfly);
                this.wings.addAll(butterfly.getWings());
                this.strips.addAll(butterfly.getWings());

                for (Wing wing : butterfly.getWings()) {
                    this.wingTable.put(wing.id, wing);
                }
            }
        }
    }

    public List<Butterfly> getButterflies() {
        return butterfliesUnmodifiable;
    }

    public List<Wing> getWings() {
        return wingsUnmodifiable;
    }

    private static class Fixture extends LXAbstractFixture {
        private final TreeModel treeModel;

        private Fixture(PApplet applet, Butterfly[] butterflyArr) {
            this.treeModel = new TreeModel(applet, TreeModel.ModelMode.MAJOR_LIMBS);
            for (LXPoint p : treeModel.points) {
                this.points.add(p);
            }

            for (Butterfly butterfly : butterflyArr) {
                if (butterfly != null) {
                    for (LXPoint point : butterfly.points) {
                        this.points.add(point);
                    }
                }
            }
        }
    }

    public Butterfly getButterflyByRawIndex(int index) {
        return _butterflies[index];
    }

    public Butterfly getButterflyById(String id) {
        return this.butterflyTable.get(id);
    }

    public Wing getWingById(String id) {
        return this.wingTable.get(id);
    }

    public static class Butterfly extends StripsModel<Wing> {

        public static enum Type {
            SMALL, LARGE
        }

        public final String id;
        public final Type type;

        protected final List<Wing> wings = new ArrayList<>();
        private final List<Wing> wingsUnmodifiable = Collections.unmodifiableList(wings);

        public final float x;
        public final float y;
        public final float z;
        public final float rx;
        public final float ry;
        public final float rz;

        public Butterfly(String id, Type type, float x, float y, float z, float rx, float ry, float rz, LXTransform t) {
            super(new Fixture(id, type, x, y, z, rx, ry, rz, t));
            Fixture fixture = (Fixture)this.fixtures.get(0);
            this.id = id;
            this.type = type;

            while (rx < 0) rx += 360;
            while (ry < 0) ry += 360;
            while (rz < 0) rz += 360;
            rx = rx % 360;
            ry = ry % 360;
            rz = rz % 360;

            this.x = x;
            this.y = y;
            this.z = z;
            this.rx = rx;
            this.ry = ry;
            this.rz = rz;

            this.wings.addAll(fixture.wings);
        }

        public List<Wing> getWings() {
            return wingsUnmodifiable;
        }

        private static class Fixture extends LXAbstractFixture {

            private final List<Wing> wings = new ArrayList<>();

            private Fixture(String id, Butterfly.Type type, float x, float y, float z, float rx, float ry, float rz, LXTransform transform) {
                transform.push();
                transform.translate(x, y, z);
                //t.translate(type.EDGE_WIDTH/2, type.EDGE_HEIGHT/2, type.EDGE_WIDTH/2);
                transform.rotateX(rx * Math.PI / 180.);
                transform.rotateY(ry * Math.PI / 180.);
                transform.rotateZ(rz * Math.PI / 180.);
                //t.translate(-type.EDGE_WIDTH/2, -type.EDGE_HEIGHT/2, -type.EDGE_WIDTH/2);

                this.wings.add(new Wing(
                    id+"_upper_left_wing",
                    (type == Butterfly.Type.LARGE) ? Wing.Type.LARGE_UPPER : Wing.Type.SMALL_UPPER,
                    new float[] {0, 0, 0},
                    new float[] {0, 0, 40},
                    transform
                ));

                this.wings.add(new Wing(
                    id+"_upper_right_wing",
                    (type == Butterfly.Type.LARGE) ? Wing.Type.LARGE_UPPER : Wing.Type.SMALL_UPPER,
                    new float[] {-2, 0, 0},
                    new float[] {0, 180, 40},
                    transform
                ));

                this.wings.add(new Wing(
                    id+"_lower_left_wing",
                    (type == Butterfly.Type.LARGE) ? Wing.Type.LARGE_LOWER : Wing.Type.SMALL_LOWER,
                    new float[] {0, -5, 0},
                    new float[] {0, 0, 35},
                    transform
                ));

                this.wings.add(new Wing(
                    id+"_lower_right_wing",
                    (type == Butterfly.Type.LARGE) ? Wing.Type.LARGE_LOWER : Wing.Type.SMALL_LOWER,
                    new float[] {-2, -5, 0},
                    new float[] {0, 180, 35},
                    transform
                ));

                for (Wing wing : wings) {
                    for (LXPoint point : wing.points) {
                        this.points.add(point);
                    }
                }

                transform.pop();
            }
        }
    }

    public static class Wing extends Strip {

        public enum Type { 
            LARGE_UPPER, LARGE_LOWER, SMALL_UPPER, SMALL_LOWER
        }

        public static class ButterflyMetrics extends Strip.Metrics {
            public final int LEDS_PER_METER = 30;
            public final float POINT_SPACING = INCHES_PER_METER / LEDS_PER_METER;
            public final float length;
            public final float arcWidth;

            public ButterflyMetrics(int numPoints, float arcWidth) {
                super(numPoints);
                this.length = numPoints * POINT_SPACING;
                this.arcWidth = arcWidth;
            }
        }

        public final Type type;

        public final String id;

        public final LXVector[] coords = new LXVector[4];

        public Wing(String id, Type type, float[] coordinates, float[] rotations, LXTransform transform) {
            super(id, new Strip.Metrics(Wing.getNumPointsOnWing(type)), new Fixture(type, coordinates, rotations, transform));
            this.id = id;
            this.type = type;

            // Precompute boundary coordinates for faster rendering, these
            // can be dumped into a VBO for a shader.
            t.push();
            t.translate(-WIDTH/2, 0);
            this.coords[0] = t.vector();
            t.translate(0, LENGTH);
            this.coords[1] = t.vector();
            t.translate(WIDTH, 0);
            this.coords[2] = t.vector();
            t.translate(0, -LENGTH);
            this.coords[3] = t.vector();
            t.pop();
        }

        private static int getNumPointsOnWing(Type type) {
            switch(type) {
                case LARGE_UPPER: return 12;
                case LARGE_LOWER: return 8;
                case SMALL_UPPER: return 8;
                case SMALL_LOWER: return 6;
            }
            return 0;
        }

        private static class Fixture extends LXAbstractFixture {
            private Fixture(Type type, float[] coordinates, float[] rotations, LXTransform transform) {
                transform.push();
                transform.translate(coordinates[0], coordinates[1], coordinates[2]);
                transform.rotateX(rotations[0] * PI / 180.);
                transform.rotateY(rotations[1] * PI / 180.);
                transform.rotateZ(rotations[2] * PI / 180.);

                ButterflyMetrics metrics = null;
                switch (type) {
                    case LARGE_UPPER: metrics = new ButterflyMetrics(6, 10); break;
                    case LARGE_LOWER: metrics = new ButterflyMetrics(4,  6); break;
                    case SMALL_UPPER: metrics = new ButterflyMetrics(4,  7); break; // adjust
                    case SMALL_LOWER: metrics = new ButterflyMetrics(3,  4); break; // adjust
                }

                System.out.println("Wing type: " + type);

                // calculate positions
                List<float[]> positions = new ArrayList<float[]>();

                for (int i = 0; i < metrics.numPoints; i++) {
                    float t = i / (float)metrics.numPoints;
                    float x = bezierPoint(0, metrics.arcWidth*0.35f, metrics.arcWidth*0.5f, metrics.arcWidth, t);
                    float y = bezierPoint(0, metrics.arcWidth*-0.2f, metrics.arcWidth*-0.2f, 0, t);
                    positions.add(new float[] {x, y});
                }

                // add the points (up then wing and then back down the other side)
                for (int i = 0; i < metrics.numPoints; i++) {
                    transform.push();
                    transform.translate(positions.get(i)[0], positions.get(i)[1], 0);
                    this.points.add(new LXPoint(transform.x(), transform.y(), transform.z()));
                    transform.pop();
                }

                for (int i = metrics.numPoints-1; i > -1; i--) {
                    transform.push();
                    transform.translate(positions.get(i)[0]+(-0.025f*i)+0.1f, positions.get(i)[1]+0.25f, 0);
                    this.points.add(new LXPoint(transform.x(), transform.y(), transform.z()));
                    transform.pop();
                }

                // switch (type) {
                //   case LARGE_UPPER:
                //     ButterflyMetrics upperMetrics = new ButterflyMetrics(6, 10);

                //     for (int i = 0; i < upperMetrics.numPoints; i++) {
                //       float t = i / (float)upperMetrics.numPoints;
                //       float x = bezierPoint(0, upperMetrics.arcWidth*0.35f, upperMetrics.arcWidth*0.5f, upperMetrics.arcWidth, t);
                //       float y = bezierPoint(0, upperMetrics.arcWidth*-0.2f, upperMetrics.arcWidth*-0.2f, 0, t);
                //       positions.add(new float[] {x, y});
                //     }

                //     for (int i = 0; i < upperMetrics.numPoints; i++) {
                //       transform.push();
                //       transform.translate(positions.get(i)[0], positions.get(i)[1], 0);
                //       this.points.add(new LXPoint(transform.x(), transform.y(), transform.z()));
                //       transform.pop();
                //     }

                //     for (int i = upperMetrics.numPoints-1; i > -1; i--) {
                //       transform.push();
                //       transform.translate(positions.get(i)[0]+(-0.025f*i)+0.1f, positions.get(i)[1]+0.25f, 0);
                //       this.points.add(new LXPoint(transform.x(), transform.y(), transform.z()));
                //       transform.pop();
                //     }
                //     break;

                //   case LARGE_LOWER:
                //     ButterflyMetrics lowerMetrics = new ButterflyMetrics(4, 6);

                //     for (int i = 0; i < lowerMetrics.numPoints; i++) {
                //       float t = i / (float)lowerMetrics.numPoints;
                //       float x = bezierPoint(0, lowerMetrics.arcWidth*0.35f, lowerMetrics.arcWidth*0.5f, lowerMetrics.arcWidth, t);
                //       float y = bezierPoint(0, lowerMetrics.arcWidth*-0.25f, lowerMetrics.arcWidth*-0.25f, 0, t);
                //       positions.add(new float[] {x, y});
                //     }

                //     for (int i = 0; i < lowerMetrics.numPoints; i++) {
                //       transform.push();
                //       transform.translate(positions.get(i)[0], positions.get(i)[1], 0);
                //       this.points.add(new LXPoint(transform.x(), transform.y(), transform.z()));
                //       transform.pop();
                //     }

                //     for (int i = lowerMetrics.numPoints-1; i > -1; i--) {
                //       transform.push();
                //       transform.translate(positions.get(i)[0]+(-0.025f*i)+0.1f, positions.get(i)[1]+0.25f, 0);
                //       this.points.add(new LXPoint(transform.x(), transform.y(), transform.z()));
                //       transform.pop();
                //     }
                //     break;
                // }


                transform.pop();
            }

            private float bezierPoint(float a, float b, float c, float d, float t) {
                float t1 = 1.0f - t;
                return ((a * t1) + (3 * b * t)) * (t1 * t1) + ((3 * c * t1) + (d * t)) * (t * t);
            }
        }
    }

    // public static class ButterflyStrip extends Strip {
    //   public static class Metrics extends Strip.Metrics {
    //     public final int LEDS_PER_METER = 30;
    //     public final float POINT_SPACING = INCHES_PER_METER / LEDS_PER_METER;
    //     public final float length;
    //     public final float arcWidth;

    //     public Metrics(int numPoints, float arcWidth) {
    //       super(numPoints);
    //       this.length = numPoints * POINT_SPACING;
    //       this.arcWidth = arcWidth;
    //     }
    //   }

    //   public ButterflyStrip(Metrics metrics, LXTransform transform) {
    //     super("", metrics, new Fixture(metrics, transform));
    //   }

    //   private static class Fixture extends LXAbstractFixture {
    //     private Fixture(Metrics metrics, LXTransform transform) {
    //       transform.push();

    //       List<float[]> positions = new ArrayList<float[]>();

    //       switch ()
    //         for (int i = 0; i < metrics.numPoints; i++) {
    //           float t = i / (float)metrics.numPoints;
    //           float x = bezierPoint(0, metrics.arcWidth*0.35f, metrics.arcWidth*0.5f, metrics.arcWidth, t);
    //           float y = bezierPoint(0, metrics.arcWidth*-0.15f, metrics.arcWidth*-0.15f, 0, t);
    //           positions.add(new float[] {x, y});
    //         }

    //         for (int i = 0; i < metrics.numPoints; i++) {
    //           transform.push();
    //           transform.translate(positions.get(i)[0], positions.get(i)[1], 0);
    //           this.points.add(new LXPoint(transform.x(), transform.y(), transform.z()));
    //           transform.pop();
    //         }

    //         for (int i = metrics.numPoints-2; i > -1; i--) {
    //           transform.push();
    //           transform.translate(positions.get(i)[0]+(-0.025f*i)+0.1f, positions.get(i)[1]+0.25f, 0);
    //           this.points.add(new LXPoint(transform.x(), transform.y(), transform.z()));
    //           transform.pop();
    //         }
    //       transform.pop();
    //     }

    //     private float bezierPoint(float a, float b, float c, float d, float t) {
    //       float t1 = 1.0f - t;
    //       return ((a * t1) + (3 * b * t)) * (t1 * t1) + ((3 * c * t1) + (d * t)) * (t * t);
    //     }
    //   }
    // }
}
