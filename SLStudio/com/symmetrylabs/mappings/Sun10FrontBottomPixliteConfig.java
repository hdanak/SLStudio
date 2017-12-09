package com.symmetrylabs.mappings;

import com.symmetrylabs.model.Slice;
import com.symmetrylabs.pixlites.Pixlite;
import com.symmetrylabs.pixlites.PixliteOutput;
import com.symmetrylabs.pixlites.PointsGrouping;
import heronarts.lx.LX;

import java.net.SocketException;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class Sun10FrontBottomPixliteConfig {
    public Sun10FrontBottomPixliteConfig(LX lx, Slice slice, String ipAddress, Pixlite pixlite) throws SocketException {

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("1")
                .addPoints(slice.getStripById("1").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("2").points)
                .addPoints(slice.getStripById("3").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("4").points)
                .addPoints(slice.getStripById("5").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("6").points)
                .addPoints(slice.getStripById("7").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("8").points)
                .addPoints(slice.getStripById("9").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("10").points)
                .addPoints(slice.getStripById("11").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("12").points)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("2")
                .addPoints(slice.getStripById("13").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("14").points)
                .addPoints(slice.getStripById("15").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("16").points)
                .addPoints(slice.getStripById("17").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("18").points)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("3")
                .addPoints(slice.getStripById("19").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("20").points)
                .addPoints(slice.getStripById("21").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("22").points)
                .addPoints(slice.getStripById("23").points, PointsGrouping.REVERSE_ORDERING)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("4")
                .addPoints(slice.getStripById("24").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("25").points)
                .addPoints(slice.getStripById("26").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("27").points)
                .addPoints(slice.getStripById("28").points, PointsGrouping.REVERSE_ORDERING)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("5")
                .addPoints(slice.getStripById("29").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("30").points)
                .addPoints(slice.getStripById("31").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("32").points)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("6")
                .addPoints(slice.getStripById("33").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("34").points)
                .addPoints(slice.getStripById("35").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("36").points, PointsGrouping.Shift.RIGHT)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("7")
                .addPoints(slice.getStripById("37").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("38").points)
                .addPoints(slice.getStripById("39").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("40").points)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("8")
                .addPoints(slice.getStripById("41").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("42").points)
                .addPoints(slice.getStripById("43").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("44").points)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("9")
                .addPoints(slice.getStripById("45").points, PointsGrouping.REVERSE_ORDERING, PointsGrouping.Shift.LEFT)
                .addPoints(slice.getStripById("46").points, PointsGrouping.Shift.LEFT)
                .addPoints(slice.getStripById("47").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("48").points, PointsGrouping.Shift.LEFT)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("10")
                .addPoints(slice.getStripById("49").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("50").points)
                .addPoints(slice.getStripById("51").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("52").points)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("11")
                .addPoints(slice.getStripById("53").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("54").points)
                .addPoints(slice.getStripById("55").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("56").points)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("12")
                .addPoints(slice.getStripById("57").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("58").points)
                .addPoints(slice.getStripById("59").points, PointsGrouping.REVERSE_ORDERING)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("13")
                .addPoints(slice.getStripById("60").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("61").points)
                .addPoints(slice.getStripById("62").points, PointsGrouping.REVERSE_ORDERING)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("14")
                .addPoints(slice.getStripById("63").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("64").points)
                .addPoints(slice.getStripById("65").points, PointsGrouping.REVERSE_ORDERING)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("15")
                .addPoints(slice.getStripById("66").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("67").points)
                .addPoints(slice.getStripById("68").points, PointsGrouping.REVERSE_ORDERING)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("16")
                .addPoints(slice.getStripById("69").points, PointsGrouping.REVERSE_ORDERING)
        ));
    }
}
