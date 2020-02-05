package org.mbari.cthulu.settings;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Miscellaneous application state saved along with the settings, but not directly editable.
 */
final public class State {

    private int windowX;

    private int windowY;

    private int windowWidth;

    private int windowHeight;

    public State() {
        this.windowX = 300;
        this.windowY = 300;
        this.windowWidth = 1100;
        this.windowHeight = 770;
    }

    public State(State from) {
        this.windowX = from.windowX;
        this.windowY = from.windowY;
        this.windowWidth = from.windowWidth;
        this.windowHeight = from.windowHeight;
    }

    public int windowX() {
        return windowX;
    }

    public int windowY() {
        return windowY;
    }

    public int windowWidth() {
        return windowWidth;
    }

    public int windowHeight() {
        return windowHeight;
    }

    public void position(int windowX, int windowY) {
        this.windowX = windowX;
        this.windowY = windowY;
    }

    public void window(int windowWidth, int windowHeight) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
            .add("windowX", windowX)
            .add("windowY", windowY)
            .add("windowWidth", windowWidth)
            .add("windowHeight", windowHeight)
            .toString();
    }
}
