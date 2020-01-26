package org.mbari.cthulu.settings;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Miscellaneous application state saved along with the settings, but not directly editable.
 */
final public class State {

    private int windowWidth;

    private int windowHeight;

    public State() {
        this.windowWidth = 1100;
        this.windowHeight = 770;
    }

    public State(State from) {
        this.windowWidth = from.windowWidth;
        this.windowHeight = from.windowHeight;
    }

    public int windowWidth() {
        return windowWidth;
    }

    public int windowHeight() {
        return windowHeight;
    }

    public void window(int windowWidth, int windowHeight) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
            .add("windowWidth", windowWidth)
            .add("windowHeight", windowHeight)
            .toString();
    }
}
