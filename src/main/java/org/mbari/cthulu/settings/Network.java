package org.mbari.cthulu.settings;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Settings for the network control interface.
 */
final public class Network {

    private int controlPort;

    public Network() {
        this.controlPort = 5005;
    }

    /**
     * Copy settings.
     *
     * @param from settings to copy
     */
    public Network(Network from) {
        this.controlPort = from.controlPort;
    }

    public int controlPort() {
        return controlPort;
    }

    public void controlPort(int controlPort) {
        this.controlPort = controlPort;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
            .add("controlPort", controlPort)
            .toString();
    }
}
