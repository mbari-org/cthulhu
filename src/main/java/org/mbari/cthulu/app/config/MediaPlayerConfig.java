package org.mbari.cthulu.app.config;

import com.google.gson.Gson;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;

import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Media player configuration.
 * <p>
 * Configuration is read from a file on the application classpath.
 */
final public class MediaPlayerConfig {

    /**
     * Name of the classpath resource containing the configuration.
     */
    private static final String CONFIG_RESOURCE = "/org/mbari/cthulu/config/media-player-config.json";

    /**
     * Configured arguments passed to LibVLC when creating a {@link MediaPlayerFactory}.
     */
    private List<String> libVlcArgs;

    /**
     * Read media player configuration.
     *
     * @return media player configuration
     */
    public static MediaPlayerConfig readMediaPlayerConfig() {
        return new Gson().fromJson(
            new InputStreamReader(MediaPlayerConfig.class.getResourceAsStream(CONFIG_RESOURCE)),
            MediaPlayerConfig.class
        );
    }

    /**
     * Get the initialisation arguments to pass to LibVLC when creating a {@link MediaPlayerFactory}.
     *
     * @return collection of LibVLC arguments
     */
    public List<String> libVlcArgs() {
        return Collections.unmodifiableList(libVlcArgs);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
            .add("libVlcArgs", libVlcArgs)
            .toString();
    }

    private MediaPlayerConfig() {
    }
}
