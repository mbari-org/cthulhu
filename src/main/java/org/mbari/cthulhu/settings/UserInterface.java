package org.mbari.cthulhu.settings;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Settings for the user interface.
 */
final public class UserInterface {

    private boolean rememberWindowPosition;

    private boolean rememberWindowSize;

    /**
     * Create settings with default values.
     */
    public UserInterface() {
        this.rememberWindowPosition = true;
        this.rememberWindowSize = false;
    }

    /**
     * Copy settings.
     *
     * @param from settings to copy
     */
    public UserInterface(UserInterface from) {
        this.rememberWindowPosition = from.rememberWindowPosition;
        this.rememberWindowSize = from.rememberWindowSize;
    }

    public boolean rememberWindowPosition() {
        return rememberWindowPosition;
    }

    public void rememberWindowPosition(boolean rememberWindowPosition) {
        this.rememberWindowPosition = rememberWindowPosition;
    }

    public boolean rememberWindowSize() {
        return rememberWindowSize;
    }

    public void rememberWindowSize(boolean rememberWindowSize) {
        this.rememberWindowSize = rememberWindowSize;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
            .add("rememberWindowPosition", rememberWindowPosition)
            .add("rememberWindowSize", rememberWindowSize)
            .toString();
    }
}
