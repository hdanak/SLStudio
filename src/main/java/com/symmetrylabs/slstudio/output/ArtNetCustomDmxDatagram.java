package com.symmetrylabs.slstudio.output;

import java.util.List;
import java.util.ArrayList;

import heronarts.lx.LX;
import heronarts.lx.output.LXDatagram;

import com.symmetrylabs.color.Ops8;

public class ArtNetCustomDmxDatagram extends ArtNetDmxDatagram {
    public enum ColorType {
        RGB(3), RGBW(4), RGBWYP(6);

        public final int byteLength;

        ColorType(int byteLength) {
            this.byteLength = byteLength;
        }

        public static ColorType fromString(String s) {
            try {
                return ColorType.valueOf(s.toUpperCase());
            }
            catch (Exception e) {
                return RGB;
            }
        }
    };

    public static class Segment {
        public final int[] pointIndices;
        public final ColorType colorType;

        public Segment(int[] pointIndices, ColorType colorType) {
            this.pointIndices = pointIndices;
            this.colorType = colorType;
        }
    }

    public static class Builder {
        private List<Segment> segments = new ArrayList<>();

        public Builder addSegment(int[] pointIndices, ColorType colorType) {
            Segment s = new Segment(pointIndices, colorType);
            segments.add(s);
            return this;
        }

        public ArtNetCustomDmxDatagram createDatagram(LX lx, String ipAddress, int universeIndex) {
            int dataLength = 0;
            int pointCount = 0;
            for (Segment s : segments) {
                dataLength += s.pointIndices.length * s.colorType.byteLength;
                pointCount += s.pointIndices.length;
            }
            int[] pointIndices = new int[pointCount];
            int total = 0;
            for (Segment s : segments) {
                for (int pointIndex : s.pointIndices) {
                    pointIndices[total++] = pointIndex;
                }
            }
            return new ArtNetCustomDmxDatagram(lx, ipAddress, pointIndices, dataLength, universeIndex, segments);
        }
    }

    private final List<Segment> segments;

    protected ArtNetCustomDmxDatagram(LX lx, String ipAddress, int[] indices, int dataLength, int universeNumber, List<Segment> segments) {
        super(lx, ipAddress, indices, dataLength, universeNumber);

        this.segments = segments;
    }

    @Override
    protected LXDatagram copyPointsGamma(int[] colors, int[] pointIndices, int offset, int unmappedColor) {
        int[] byteOffset = BYTE_ORDERING[this.byteOrder.ordinal()];
        int channelIndex = offset;
        for (Segment s : segments) {
            for (int index : s.pointIndices) {
                int colorValue = (index >= 0) ? colors[index] : unmappedColor;
                int gammaExpanded = gammaExpander.getExpandedColor(colorValue);

                int r = Ops8.red(gammaExpanded);
                int g = Ops8.green(gammaExpanded);
                int b = Ops8.blue(gammaExpanded);

                int w = 0, y = 0, p = 0;

                switch (s.colorType) {
                case RGBW:
                case RGBWYP:
                    w = r < g ? r : g;
                    if (b < w) w = b;
                    r -= w;
                    g -= w;
                    b -= w;
                }
                switch (s.colorType) {
                case RGBWYP:
                    y = r < g ? r : g; // yellow/amber
                    r -= y;
                    g -= y;
                    p = r < b ? r : b; // purple/UV
                }

                buffer[channelIndex + byteOffset[0]] = (byte)r;
                buffer[channelIndex + byteOffset[1]] = (byte)g;
                buffer[channelIndex + byteOffset[2]] = (byte)b;
                channelIndex += 3;

                switch (s.colorType) {
                case RGBW:
                case RGBWYP:
                    buffer[channelIndex++] = (byte)w;
                }

                switch (s.colorType) {
                case RGBWYP:
                    buffer[channelIndex++] = (byte)y;
                    buffer[channelIndex++] = (byte)p;
                }
            }
        }

        return this;
    }
}
