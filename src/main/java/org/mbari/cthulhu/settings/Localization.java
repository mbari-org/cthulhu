package org.mbari.cthulhu.settings;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Settings for video localization (bounding boxes).
 */
final public class Localization {

    private int incomingPort;

    private int outgoingPort;

    private String incomingTopic;

    private String outgoingTopic;

    public Localization() {
        this.incomingPort = 5561;
        this.outgoingPort = 5562;
        this.incomingTopic = "localization";
        this.outgoingTopic = "localization";
    }

    /**
     * Copy settings.
     *
     * @param from settings to copy
     */
    public Localization(Localization from) {
        this.incomingPort = from.incomingPort;
        this.outgoingPort = from.outgoingPort;
        this.incomingTopic = from.incomingTopic;
        this.outgoingTopic = from.outgoingTopic;
    }

    public int incomingPort() {
        return incomingPort;
    }

    public void incomingPort(int incomingPort) {
        this.incomingPort = incomingPort;
    }

    public int outgoingPort() {
        return outgoingPort;
    }

    public void outgoingPort(int outgoingPort) {
        this.outgoingPort = outgoingPort;
    }

    public String incomingTopic() {
        return incomingTopic;
    }

    public void incomingTopic(String incomingTopic) {
        this.incomingTopic = incomingTopic;
    }

    public String outgoingTopic() {
        return outgoingTopic;
    }

    public void outgoingTopic(String outgoingTopic) {
        this.outgoingTopic = outgoingTopic;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
            .add("incomingPort", incomingPort)
            .add("outgoingPort", outgoingPort)
            .add("incomingTopic", incomingTopic)
            .add("outgoingTopic", outgoingTopic)
            .toString();
    }
}
