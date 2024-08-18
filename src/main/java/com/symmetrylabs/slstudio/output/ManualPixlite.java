package com.symmetrylabs.slstudio.output;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.output.LXDatagramOutput;

import java.net.SocketException;
import java.util.List;
import java.util.Arrays;
import java.util.function.BiFunction;

public class ManualPixlite extends ArtNetOutput {
    private final LX lx;
    private boolean logConnections = true;

    // (pointIndices, universeIndex) => ArtNetDmxDatagram
    protected BiFunction<int[], Integer, ArtNetDmxDatagram> defaultDatagramFactory;

    public ManualPixlite(LX lx, String ipAddress) {
        this(lx, ipAddress, (int[] pointIndices, Integer universeIndex)
                -> new ArtNetDmxDatagram(lx, ipAddress, pointIndices, universeIndex));
    }
    public ManualPixlite(LX lx, String ipAddress,
            BiFunction<int[], Integer, ArtNetDmxDatagram> defaultDatagramFactory) {
        super(lx, ipAddress);

        this.lx = lx;
        this.defaultDatagramFactory = defaultDatagramFactory;
    }

    public ManualPixlite setLogConnections(boolean logConnections) {
        this.logConnections = logConnections;
        return this;
    }

    public ManualPixlite addPoints(int startUniverseIndex, List<LXPoint> points) {
        return addPoints(startUniverseIndex, points, defaultDatagramFactory);
    }
    public ManualPixlite addPoints(
            int startUniverseIndex, List<LXPoint> points,
            BiFunction<int[], Integer, ArtNetDmxDatagram> datagramFactory) {

        try {
            ManualPixliteOutput mpo = new ManualPixliteOutput(startUniverseIndex, points, datagramFactory);
            mpo.setLogConnections(logConnections);
            addChild(mpo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    private class ManualPixliteOutput extends LXDatagramOutput {
        private static final int MAX_NUM_POINTS_PER_UNIVERSE = 170;

        public ManualPixliteOutput(int startUniverseIndex, List<LXPoint> points,
                BiFunction<int[], Integer, ArtNetDmxDatagram> datagramFactory) throws SocketException {
            super(lx);

            // the points for one pixlite output have to be spread across multiple universes
            int numPoints = points.size();
            int numUniverses = (numPoints / MAX_NUM_POINTS_PER_UNIVERSE) + 1;
            int counter = 0;

            for (int i = 0; i < numUniverses; i++) {
                int numIndices = ((i + 1) * MAX_NUM_POINTS_PER_UNIVERSE) > numPoints
                        ? (numPoints % MAX_NUM_POINTS_PER_UNIVERSE)
                        : MAX_NUM_POINTS_PER_UNIVERSE;

                int[] pointIndices = new int[numIndices];
                for (int j = 0; j < numIndices; ++j) {
                    pointIndices[j] = points.get(counter++).index;
                }

                int universeIndex = startUniverseIndex + i;
                addDatagram(datagramFactory.apply(pointIndices, universeIndex));
            }
        }
    }

}
