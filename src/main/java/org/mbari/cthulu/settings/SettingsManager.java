package org.mbari.cthulu.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A component that loads/saves user configuration settings.
 */
final public class SettingsManager {

    private static final Logger log = LoggerFactory.getLogger(SettingsManager.class);

    private static final String DEFAULT_SETTINGS_FILE = ".config/mbari/cthulu/cthulu-settings.json";

    private static final class Holder {
        private static final SettingsManager INSTANCE = new SettingsManager();
    }

    /**
     * Get the settings manager.
     *
     * @return settings manager
     */
    public static SettingsManager settingsManager() {
        return Holder.INSTANCE;
    }

    /**
     * File where the current settings where loaded from.
     */
    private final File settingsFile;

    /**
     * Create a settings manager.
     */
    private SettingsManager() {
        this.settingsFile = new File(String.format("%s/%s", System.getProperty("user.home"), DEFAULT_SETTINGS_FILE));
    }

    /**
     * Read the application settings from the configuration file.
     *
     * @return settings settings
     */
    public Settings read() {
        log.debug("read()");
        try {
            return new Gson().fromJson(new FileReader(settingsFile), Settings.class);
        } catch (Exception e) {
            log.warn("Failed to load settings file, using defaults: {}", e.getMessage());
            return createDefaults();
        }
    }

    /**
     * Write the settings to the configuration file.
     *
     * @param settings settings to write
     */
    public void write(Settings settings) {
        log.debug("write(settings={})", settings);
        if (!settingsFile.getParentFile().exists()) {
            settingsFile.getParentFile().mkdirs();
        }
        try (FileWriter writer = new FileWriter(settingsFile)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(settings, writer);
        } catch (IOException e) {
            log.error("Failed to write settings file: {}", e.getMessage());
        }
    }

    private Settings createDefaults() {
        log.debug("createDefaults()");
        Settings settings = new Settings();
        write(settings);
        return settings;
    }
}
