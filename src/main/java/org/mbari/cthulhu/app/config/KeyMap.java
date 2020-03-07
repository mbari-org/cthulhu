package org.mbari.cthulhu.app.config;

import com.google.gson.Gson;

import java.io.InputStreamReader;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Configuration of key-bindings for various actions (not triggered by regular menu actions).
 * <p>
 * Configuration is read from a file on the application classpath.
 */
final public class KeyMap {

    /**
     * Name of the classpath resource containing the configuration.
     */
    private static final String CONFIG_RESOURCE = "/org/mbari/cthulhu/config/keymap.json";

    /**
     * Read media player configuration.
     *
     * @return media player configuration
     */
    public static KeyMap readKeyMap() {
        return new Gson().fromJson(
            new InputStreamReader(KeyMap.class.getResourceAsStream(CONFIG_RESOURCE)),
            KeyMap.class
        );
    }

    private String[] playPause;

    private String[] shortSkip;

    private String[] normalSkip;

    private String[] longSkip;

    private String[] shortBack;

    private String[] normalBack;

    private String[] longBack;

    private String[] nextFrame;

    public String[] playPause() {
        return playPause;
    }

    public String[] shortSkip() {
        return shortSkip;
    }

    public String[] normalSkip() {
        return normalSkip;
    }

    public String[] longSkip() {
        return longSkip;
    }

    public String[] shortBack() {
        return shortBack;
    }

    public String[] normalBack() {
        return normalBack;
    }

    public String[] longBack() {
        return longBack;
    }

    public String[] nextFrame() {
        return nextFrame;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
            .add("playPause", playPause)
            .add("shortSkip", shortSkip)
            .add("normalSkip", normalSkip)
            .add("longSkip", longSkip)
            .add("shortBack", shortBack)
            .add("normalBack", normalBack)
            .add("longBack", longBack)
            .add("nextFrame", nextFrame)
            .toString();
    }

    private KeyMap() {
    }
}
