package org.mbari.cthulu.settings;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Settings for the network control interface.
 */
final public class Network {

    private int controlPort;

    private Localization localization;

    public Network() {
        this.controlPort = 5005;
        this.localization = new Localization();
    }

    /**
     * Copy settings.
     *
     * @param from settings to copy
     */
    public Network(Network from) {
        this.controlPort = from.controlPort;
        this.localization = new Localization(from.localization);
    }

    public int controlPort() {
        return controlPort;
    }

    public void controlPort(int controlPort) {
        this.controlPort = controlPort;
    }

    public Localization localization() {
        return localization;
    }

    public void setLocalization(Localization localization) {
        this.localization = localization;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
            .add("controlPort", controlPort)
            .add("localization", localization)
            .toString();
    }
}
