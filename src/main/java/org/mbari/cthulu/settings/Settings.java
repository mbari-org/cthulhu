package org.mbari.cthulu.settings;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Application settings.
 */
final public class Settings {

    /**
     * The various settings values.
     */
    private Network network;
    private UserInterface userInterface;
    private MediaPlayer mediaPlayer;
    private Annotations annotations;
    private State state;

    /**
     * Create settings with default values.
     */
    public Settings() {
        this.network = new Network();
        this.userInterface = new UserInterface();
        this.mediaPlayer = new MediaPlayer();
        this.annotations = new Annotations();
        this.state = new State();
    }

    /**
     * Create a copy of the settings.
     *
     * @param from settings to copy from
     */
    public Settings(Settings from) {
        this.network = new Network(from.network);
        this.userInterface = new UserInterface(from.userInterface);
        this.mediaPlayer = new MediaPlayer(from.mediaPlayer);
        this.annotations = new Annotations(from.annotations);
        this.state = new State(from.state);
    }

    public Network network() {
        return network;
    }

    public UserInterface userInterface() {
        return userInterface;
    }

    public MediaPlayer mediaPlayer() {
        return mediaPlayer;
    }

    public Annotations annotations() {
        return annotations;
    }

    public State state() {
        return state;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
            .add("network", network)
            .add("userInterface", userInterface)
            .add("mediaPlayer", mediaPlayer)
            .add("annotations", annotations)
            .add("state", state)
            .toString();
    }
}
