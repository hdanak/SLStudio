package com.symmetrylabs.shows.mindboggle;

import java.util.List;
import java.util.ArrayList;

import heronarts.lx.transform.LXVector;

import com.symmetrylabs.util.FileUtils;

public class ObjParser {

    public static abstract class BaseObject {
        public final String name;
        public final List<LXVector> verts;

        public BaseObject(String name) {
            this.name = name;

            verts = new ArrayList<>();
        }
    }

    public static class ShapeObject extends BaseObject {
        public ShapeObject(String name) {
            super(name);
        }
    }

    public static class FixtureObject extends BaseObject {
        public final int output;

        public FixtureObject(String name, int output) {
            super(name);

            this.output = output;
        }
    }

    public List<ShapeObject> shapes;
    public List<FixtureObject> fixtures;

    private BaseObject curObject;

    public ObjParser() {
        reset();
    }

    public void reset() {
        shapes = new ArrayList<>();
        fixtures = new ArrayList<>();

        curObject = null;
    }

    private void closeObject() {
        if (curObject instanceof ShapeObject) {
            shapes.add((ShapeObject)curObject);
        }
        if (curObject instanceof FixtureObject) {
            fixtures.add((FixtureObject)curObject);
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

                // object named like Fixture.001_Output.001
                if (parts[1].startsWith("Fixture")) {
                    int output = 0;
                    String[] nameParts = parts[1].split("_");
                    if (nameParts.length > 1 && nameParts[1].matches("^Output.\\d+$")) {
                        output = Integer.parseInt(nameParts[1].split("\\.")[1], 10);
                    }

                    curObject = new FixtureObject(parts[1], output);
                }
                // object named like Shape.001
                if (parts[1].startsWith("Shape")) {
                    curObject = new ShapeObject(parts[1]);
                }
            }
            else if ("v".equals(parts[0]) && parts.length > 3) {
                curObject.verts.add(new LXVector(Float.parseFloat(parts[1]),
                            Float.parseFloat(parts[2]), Float.parseFloat(parts[3])));
            }
        }

        closeObject();
    }
}
