package com.symmetrylabs.shows.mikey;

import com.google.common.collect.Lists;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.CandyBar;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import com.symmetrylabs.slstudio.model.DoubleStrip;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXMatrix;
import heronarts.lx.transform.LXTransform;
import java.util.ArrayList;
import java.util.List;

public class MikeyShow implements Show {
    public static final String SHOW_NAME = "mikey";

    @Override
    public SLModel buildModel() {
        return MikeyModel.create();
    }

    @Override
    public void setupLx(LX lx) {
        MikeyPixlite pixlite = new MikeyPixlite(lx, "10.200.1.128", (MikeyModel) lx.model);
        lx.addOutput(pixlite);
    }

    static class MikeyModel extends StripsModel<Strip> {
        public MikeyModel(List<Strip> strips) {
            super(SHOW_NAME, strips);
        }

        public static MikeyModel create() {
    List<Strip> strips = new ArrayList<>();
    LXTransform t = new LXTransform();

    float startX = 50; // Starting X position, might need to be adjusted
    float spacingX = 10; // Horizontal spacing between the bars

    DoubleStrip.Metrics metrics = new DoubleStrip.Metrics(139, 1, 0.5, 10);  // Example metrics with gap and verticalOffset

    for (int i = 0; i < 10; i++) {
        t.push();

        float posX = startX - (i * spacingX);  // Calculate X position for each strip
        float posY = 0;  // Initial Y position, adjust if necessary for vertical layout

        t.translate(posX, posY, 0);  // Apply translation to position each strip

        DoubleStrip strip = new DoubleStrip("bar" + i, metrics, t);
        strips.add(strip);

        t.pop();
    }
    return new MikeyModel(strips);
        }
    }
    static class MikeyPixlite extends SimplePixlite {
        public MikeyPixlite(LX lx, String ip, MikeyModel model) {
            super(lx, ip);
            // for (int i = startStrip; i <= endStrip; i++){
            //     addPixliteOutput(
            //     new PointsGrouping((i+1)+"").addPoints(model.getStripByIndex(i).getPoints()));
            // }

            addPixliteOutput(
                new PointsGrouping("1").addPoints(model.getStripByIndex(0).getPoints()));
            addPixliteOutput(
                new PointsGrouping("2").addPoints(model.getStripByIndex(1).getPoints()));
            addPixliteOutput(
                new PointsGrouping("3").addPoints(model.getStripByIndex(2).getPoints()));
            addPixliteOutput(
                new PointsGrouping("4").addPoints(model.getStripByIndex(3).getPoints()));
            addPixliteOutput(
                new PointsGrouping("5").addPoints(model.getStripByIndex(4).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("6").addPoints(model.getStripByIndex(5).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("7").addPoints(model.getStripByIndex(6).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("8").addPoints(model.getStripByIndex(7).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("9").addPoints(model.getStripByIndex(8).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("10").addPoints(model.getStripByIndex(9).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("11").addPoints(model.getStripByIndex(10).getPoints()));
            addPixliteOutput(
                new PointsGrouping("12").addPoints(model.getStripByIndex(5).getPoints()));
            addPixliteOutput(
                new PointsGrouping("13").addPoints(model.getStripByIndex(6).getPoints()));
            addPixliteOutput(
                new PointsGrouping("14").addPoints(model.getStripByIndex(7).getPoints()));
            addPixliteOutput(
                new PointsGrouping("15").addPoints(model.getStripByIndex(8).getPoints()));
            addPixliteOutput(
                new PointsGrouping("16").addPoints(model.getStripByIndex(9).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("12").addPoints(model.getStripByIndex(2).getPoints()).addPoints(model.getStripByIndex(3).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("13").addPoints(model.getStripByIndex(4).getPoints()).addPoints(model.getStripByIndex(5).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("14").addPoints(model.getStripByIndex(6).getPoints()).addPoints(model.getStripByIndex(7).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("15").addPoints(model.getStripByIndex(8).getPoints()).addPoints(model.getStripByIndex(9).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("16").addPoints(model.getStripByIndex(10).getPoints()).addPoints(model.getStripByIndex(11).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("7").addPoints(model.getStripByIndex(12).getPoints()).addPoints(model.getStripByIndex(13).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("8").addPoints(model.getStripByIndex(14).getPoints()).addPoints(model.getStripByIndex(15).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("9").addPoints(model.getStripByIndex(16).getPoints()).addPoints(model.getStripByIndex(17).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("10").addPoints(model.getStripByIndex(18).getPoints()).addPoints(model.getStripByIndex(19).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("11").addPoints(model.getStripByIndex(20).getPoints()).addPoints(model.getStripByIndex(21).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("12").addPoints(model.getStripByIndex(22).getPoints()).addPoints(model.getStripByIndex(23).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("13").addPoints(model.getStripByIndex(24).getPoints()).addPoints(model.getStripByIndex(25).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("14").addPoints(model.getStripByIndex(26).getPoints()).addPoints(model.getStripByIndex(27).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("15").addPoints(model.getStripByIndex(28).getPoints()).addPoints(model.getStripByIndex(29).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("16").addPoints(model.getStripByIndex(30).getPoints()).addPoints(model.getStripByIndex(31).getPoints()));
        }

        @Override
        public SimplePixlite addPixliteOutput(PointsGrouping pointsGrouping) {
            try {
                SimplePixliteOutput spo = new SimplePixliteOutput(pointsGrouping);
                spo.setLogConnections(false);
                addChild(spo);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return this;
        }
    }
}
