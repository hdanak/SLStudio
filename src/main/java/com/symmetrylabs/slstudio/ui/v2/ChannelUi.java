package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import static heronarts.lx.LXChannel.CrossfadeGroup;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;
import heronarts.lx.warp.LXWarp;
import java.util.ArrayList;
import java.util.List;

public class ChannelUi {
    public static void drawWarps(LX lx, String chanName, LXBus chan) {
        List<LXWarp> warps = chan.getWarps();
        if (!warps.isEmpty()) {
            for (int i = 0; i < warps.size(); i++) {
                LXWarp warp = warps.get(i);
                String warpName = String.format("%s##%s/warp/%d", warp.getClass().getSimpleName(), chanName, i);

                UI.spacing();
                if (warp.enabled.getValueb()) {
                    UI.pushColor(UI.COLOR_HEADER, UI.BLUE);
                    UI.pushColor(UI.COLOR_HEADER_ACTIVE, UI.BLUE);
                    UI.pushColor(UI.COLOR_HEADER_HOVERED, UI.BLUE_HOVER);
                }
                UI.CollapseResult section = UI.collapsibleSection(warpName, true);
                if (warp.enabled.getValueb()) {
                    UI.popColor(3);
                }
                if (UI.isItemClicked() && UI.isAltDown()) {
                    warp.enabled.toggle();
                } else if (section.shouldRemove) {
                    lx.engine.addTask(() -> chan.removeWarp(warp));
                } else if (section.isOpen) {
                    new ComponentUI(lx, warp).draw();
                }
            }
            UI.spacing();
        }
    }

    public static void drawEffects(LX lx, String chanName, LXBus chan) {
        List<LXEffect> effects = chan.getEffects();
        if (!effects.isEmpty()) {
            for (int i = 0; i < effects.size(); i++) {
                LXEffect eff = effects.get(i);
                String effName = String.format("%s##%s/effect/%d", eff.getClass().getSimpleName(), chanName, i);

                UI.spacing();
                if (eff.enabled.getValueb()) {
                    UI.pushColor(UI.COLOR_HEADER, UI.BLUE);
                    UI.pushColor(UI.COLOR_HEADER_ACTIVE, UI.BLUE);
                    UI.pushColor(UI.COLOR_HEADER_HOVERED, UI.BLUE_HOVER);
                }
                UI.CollapseResult section = UI.collapsibleSection(effName, true);
                if (eff.enabled.getValueb()) {
                    UI.popColor(3);
                }
                if (UI.isItemClicked() && UI.isAltDown()) {
                    eff.enabled.toggle();
                } else if (section.shouldRemove) {
                    lx.engine.addTask(() -> chan.removeEffect(eff));
                } else if (section.isOpen) {
                    new ComponentUI(lx, eff).draw();
                }
            }
        }
    }

    public static void drawWepPopup(LX lx, LXBus bus, WepUi wepUi) {
        UI.spacing();
        if (UI.button("+")) {
            lx.engine.setFocusedChannel(bus);
            UI.setNextWindowContentSize(300, 600);
            wepUi.resetFilter();
            UI.openPopup("Warps / effects / patterns");
        }
        if (UI.beginPopup("Warps / effects / patterns", false)) {
            wepUi.draw();
            UI.endPopup();
        }
    }

    public static void draw(LX lx, LXChannel chan, WepUi wepUi) {
        String chanName = chan.getLabel();
        boolean isFocused = lx.engine.getFocusedChannel() == chan;

        int chanFlags = UI.TREE_FLAG_DEFAULT_OPEN |
            (isFocused ? UI.TREE_FLAG_SELECTED : 0);
        UI.pushColor(UI.COLOR_HEADER, UI.PURPLE);
        UI.pushColor(UI.COLOR_HEADER_ACTIVE, UI.PURPLE);
        UI.pushColor(UI.COLOR_HEADER_HOVERED, UI.PURPLE_HOVER);
        UI.selectable(" " + chanName, isFocused, 18);
        UI.popColor(3);
        if (UI.isItemClicked()) {
            lx.engine.addTask(() -> lx.engine.setFocusedChannel(chan));
        }
        UI.spacing();

        float fader = UI.vertSliderFloat("##fader", chan.fader.getValuef(), 0, 1, "LVL", 30, 100);
        if (fader != chan.fader.getValuef()) {
            lx.engine.addTask(() -> chan.fader.setValue(fader));
        }
        UI.sameLine();
        float speed = UI.vertSliderFloat("##speed", chan.speed.getValuef(), 0, 2, "SPD", 30, 100);
        if (speed != chan.speed.getValuef()) {
            lx.engine.addTask(() -> chan.speed.setValue(speed));
        }
        UI.sameLine();

        CrossfadeGroup group = chan.crossfadeGroup.getEnum();

        UI.beginGroup();
        UI.beginColumns(2, "cued-enabled-" + chanName);
        ParameterUI.draw(lx, chan.enabled);
        UI.nextColumn();
        boolean A = UI.checkbox("A", group == CrossfadeGroup.A);
        UI.nextColumn();
        ParameterUI.draw(lx, chan.cueActive, true);
        UI.nextColumn();
        boolean B = UI.checkbox("B", group == CrossfadeGroup.B);

        if (A && group != CrossfadeGroup.A) {
            group = CrossfadeGroup.A;
        } else if (B && group != CrossfadeGroup.B) {
            group = CrossfadeGroup.B;
        } else if (!A && !B) {
            group = CrossfadeGroup.BYPASS;
        }
        chan.crossfadeGroup.setValue(group);
        UI.endColumns();
        ParameterUI.draw(lx, chan.blendMode);
        UI.endGroup();

        UI.separator();

        drawWarps(lx, chanName, chan);

        int active = chan.getActivePatternIndex();
        List<LXPattern> patterns = chan.getPatterns();
        for (int i = 0; i < patterns.size(); i++) {
            final LXPattern pat = patterns.get(i);
            String patName = pat.getClass().getSimpleName();
            String id = String.format("%s / %s", chanName, patName);

            UI.spacing();

            if (active == i) {
                UI.pushColor(UI.COLOR_HEADER, UI.BLUE);
                UI.pushColor(UI.COLOR_HEADER_ACTIVE, UI.BLUE);
                UI.pushColor(UI.COLOR_HEADER_HOVERED, UI.BLUE_HOVER);
            }
            UI.CollapseResult section = UI.collapsibleSection(patName, patterns.size() > 1);
            if (active == i) {
                UI.popColor(3);
            }
            if (UI.isItemClicked() && UI.isAltDown()) {
                final int patternIndex = i;
                lx.engine.addTask(() -> chan.goIndex(patternIndex));
            }
            if (UI.beginContextMenu(patName)) {
                if (UI.contextMenuItem("Activate")) {
                    final int patternIndex = i;
                    lx.engine.addTask(() -> chan.goIndex(patternIndex));
                }
                if (UI.contextMenuItem("Pop out")) {
                    WindowManager.addTransient(new ComponentWindow(lx, id, pat));
                }
                UI.endContextMenu();
            }

            if (section.shouldRemove) {
                lx.engine.addTask(() -> chan.removePattern(pat));
            } else if (section.isOpen) {
                new ComponentUI(lx, pat).draw();
            }
        }

        UI.spacing();
        drawEffects(lx, chanName, chan);
        drawWepPopup(lx, chan, wepUi);
    }
}
