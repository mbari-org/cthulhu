package org.mbari.cthulu.settings;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Settings for the user interface.
 */
final public class UserInterface {

    private boolean rememberWindowSize;

    /**
     * Create settings with default values.
     */
    public UserInterface() {
        this.rememberWindowSize = false;
    }

    /**
     * Copy settings.
     *
     * @param from settings to copy
     */
    public UserInterface(UserInterface from) {
        this.rememberWindowSize = from.rememberWindowSize;
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
            .add("rememberWindowSize", rememberWindowSize)
            .toString();
    }
}
