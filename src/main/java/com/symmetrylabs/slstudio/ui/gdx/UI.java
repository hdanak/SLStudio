package com.symmetrylabs.slstudio.ui.gdx;

public class UI {
    public static int TREE_FLAG_LEAF;
    public static int TREE_FLAG_DEFAULT_OPEN;
    public static int TREE_FLAG_SELECTED;

    public static int WINDOW_HORIZ_SCROLL;

    public static int DEFAULT_WIDTH = 250;

    public static native boolean init(long windowPointer);
    public static native void newFrame();
    public static native void render();
    public static native boolean shutdown();

    /* Layout */
    public static native void setNextWindowDefaults(int x, int y, int w, int h);
    public static native void setNextWindowDefaultToCursor(int w, int h);
    public static native void setNextWindowContentSize(int w, int h);
    public static native void begin(String label);
    public static native void beginDocked(String label);
    public static native boolean beginClosable(String label);
    public static native void end();
    public static native void sameLine();

    public static native void columnsStart(int num, String id);
    public static native void nextColumn();
    public static native void columnsEnd();

    public static native void separator();
    public static native void spacing();

    public static native boolean beginChild(String id, boolean border, int flags);
    public static native void endChild();
    public static native void beginGroup();
    public static native void endGroup();

    /* Widgets */
    public static native void text(String t);
    public static void text(String t, Object... objs) {
        text(String.format(t, objs));
    }
    public static native boolean button(String t);
    public static native boolean checkbox(String label, boolean v);
    public static native boolean selectable(String label, boolean v);
    public static native String inputText(String label, String text);
    public static native int colorPicker(String label, int color);
    public static float sliderFloat(String label, float v, float v0, float v1) {
        return sliderFloat(label, v, v0, v1, false);
    }
    public static native float sliderFloat(String label, float v, float v0, float v1, boolean vert);
    public static native int sliderInt(String label, int v, int v0, int v1);
    public static native int combo(String label, int selected, String[] options);

    /* Menus */
    public static native boolean beginMainMenuBar();
    public static native void endMainMenuBar();
    public static native boolean beginMenu(String label);
    public static native void endMenu();
    public static native boolean menuItem(
        String label, String shortcut, boolean selected, boolean enabled);
    public static boolean menuItem(String label) {
        return menuItem(label, null, false, true);
    }

    /* Trees */
    public static boolean treeNode(String label) {
        return treeNode(label, 0, label);
    }
    public static boolean treeNode(String label, int flags) {
        return treeNode(label, flags, label);
    }
    public static native boolean treeNode(String id, int flags, String label);
    public static native void treePop();

    /* Interaction */
    public static boolean isItemClicked() {
        return isItemClicked(0);
    }
    public static native boolean isItemClicked(int mouseButton);
    public static native boolean isItemActive();

    /* IO */
    static native float getFrameRate();
    static native boolean wantCaptureKeyboard();
    static native boolean wantCaptureMouse();
    static native void keyDown(int keycode);
    static native void keyUp(int keycode);
    static native void addInputCharacter(char c);
    static native void scrolled(float amount);

    /* Testing */
    static native void showDemoWindow();

    static {
        System.loadLibrary("slimgui");
    }
}