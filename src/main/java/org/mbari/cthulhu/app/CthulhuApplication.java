package org.mbari.cthulhu.app;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import javafx.application.Platform;
import org.mbari.cthulhu.app.config.BuildInfo;
import org.mbari.cthulhu.app.config.KeyMap;
import org.mbari.cthulhu.app.config.MediaPlayerConfig;
import org.mbari.cthulhu.settings.Settings;
import org.mbari.cthulhu.ui.player.PlayerComponent;
import org.mbari.vcr4j.sharktopoda.client.localization.LocalizationController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.log.NativeLog;

import java.util.UUID;

import static org.mbari.cthulhu.app.config.BuildInfo.readBuildInfo;
import static org.mbari.cthulhu.app.config.KeyMap.readKeyMap;
import static org.mbari.cthulhu.app.config.MediaPlayerConfig.readMediaPlayerConfig;
import static org.mbari.cthulhu.settings.SettingsManager.settingsManager;

/**
 * Application global state.
 */
final public class CthulhuApplication {

    private static final Logger log = LoggerFactory.getLogger(CthulhuApplication.class);

    private static final String APPLICATION_NAME = "MBARI Cthulu";

    private static final class Holder {
        private static final CthulhuApplication INSTANCE = new CthulhuApplication();
    }

    private final BuildInfo buildInfo;

    private final MediaPlayerConfig mediaPlayerConfig;

    private final KeyMap keyMap;

    private Settings settings;

    private final MediaPlayerFactory mediaPlayerFactory;

    private final PlayerComponents playerComponents;

    private final NativeLog nativeLog;

    private org.mbari.vcr4j.sharktopoda.client.udp.IO controlIo;

    private org.mbari.vcr4j.sharktopoda.client.localization.IO localizationIo;

    private final PublishSubject<Settings> settingsChanged = PublishSubject.create();

    private CthulhuApplication() {
        this.buildInfo = readBuildInfo();
        log.info("build version: {}", buildInfo.version());
        log.info("build timestamp: {}", buildInfo.timestamp());

        this.mediaPlayerConfig = readMediaPlayerConfig();
        log.debug("mediaPlayerConfig={}", mediaPlayerConfig);

        this.keyMap = readKeyMap();
        log.debug("keyMap={}", keyMap);

        this.settings = settingsManager().read();
        log.debug("settings={}", settings);

        this.mediaPlayerFactory = new MediaPlayerFactory(mediaPlayerConfig.libVlcArgs());

        this.playerComponents = new PlayerComponents();

        this.nativeLog = mediaPlayerFactory.application().newLog();
        this.nativeLog.addLogListener(new NativeLogHandler());

        initControlPort();
        initLocalizationPort();
    }

    /**
     * Get the singleton reference to the global application state.
     *
     * @return application state
     */
    public static CthulhuApplication application() {
        return Holder.INSTANCE;
    }

    /**
     * Get the application name.
     *
     * @return name
     */
    public String applicationName() {
        return APPLICATION_NAME;
    }

    /**
     * Get the application build/version information.
     *
     * @return build information
     */
    public BuildInfo buildInfo() {
        return buildInfo;
    }

    /**
     * Get the application key-bindings.
     *
     * @return key-bindings
     */
    public KeyMap keyMap() {
        return keyMap;
    }

    /**
     * Get the current application settings.
     *
     * @return settings
     */
    public Settings settings() {
        return settings;
    }

    /**
     * Get the factory used to create native media players.
     *
     * @return media player factory
     */
    public MediaPlayerFactory mediaPlayerFactory() {
        return mediaPlayerFactory;
    }

    /**
     * Get the player component manager.
     *
     * @return player component manager
     */
    public PlayerComponents playerComponents() {
        return playerComponents;
    }

    /**
     * Get an observable that tracks when the application settings have been changed.
     *
     * @return settings changed observable
     */
    public Observable<Settings> settingsChanged() {
        return settingsChanged;
    }

    /**
     * Get the "localization" (i.e. bounding box) component.
     *
     * @return localization component
     */
    public LocalizationController localization() {
        return localizationIo.getController();
    }

    /**
     * Apply new application settings.
     *
     * @param newSettings settings to apply
     */
    public void applySettings(Settings newSettings) {
        log.debug("applySettings(newSettings={})", newSettings);

        Settings oldSettings = this.settings;

        this.settings = newSettings;
        saveSettings();

        if (controlIo == null || oldSettings.network().controlPort() != newSettings.network().controlPort()) {
            initControlPort();
        }

        if (localizationIo == null || !oldSettings.network().localization().equals(newSettings.network().localization())) {
            initLocalizationPort();
        }

        this.settingsChanged.onNext(newSettings);
    }

    /**
     * Open a new player component.
     *
     * @return player component
     */
    public PlayerComponent open() {
        log.debug("open()");
        return playerComponents.open();
    }

    /**
     * Close a player component.
     *
     * @param uuid unique identifier of the player component to close
     */
    public void close(UUID uuid) {
        log.debug("close(uuid={})", uuid);
        playerComponents.close(uuid);
    }

    /**
     * Set the currently active (focussed) player component.
     *
     * @param playerComponent player component that is currently active
     */
    public void activePlayerComponent(PlayerComponent playerComponent) {
        log.debug("activePlayerComponent(playerComponent={})", playerComponent);
        playerComponents.active(playerComponent);
    }

    /**
     * Save the current settings to the configuration file.
     */
    public void saveSettings() {
        log.debug("saveSettings()");
        settingsManager().write(settings);
    }

    public void quit() {
        log.debug("quit()");
        playerComponents.closeAll();
        close();
    }

    /**
     * Initialise the network control port.
     * <p>
     * If the configured port number is already in use it is still possible to continue but with a disabled control
     * port.
     */
    private void initControlPort() {
        log.debug("initControlPort()");

        if (this.controlIo != null) {
            log.debug("Closing existing control port...");
            this.controlIo.close();
            this.controlIo = null;
            log.debug("Closed existing control port.");
        }

        log.debug("Establishing new control port...");

        try {
            int portNumber = settings.network().controlPort();
            log.debug("portNumber={}", portNumber);
            // Note if the external library fails to bind the socket, we will only see an error in the log - we can not detect that failure
            org.mbari.vcr4j.sharktopoda.client.udp.IO io = new org.mbari.vcr4j.sharktopoda.client.udp.IO(new CthulhuClientController(), portNumber);
            log.info("application initialised, listening on UDP port {}", portNumber);
            this.controlIo =  io;
        } catch (Exception e) {
            log.error("Failed to initialise network control port", e.getMessage());
            this.controlIo = null;
        }
    }

    /**
     * Initialise the network "localization" port.
     */
    private void initLocalizationPort() {
        log.debug("initLocalizationPort()");

        if (this.localizationIo != null) {
            log.debug("Closing existing localization interface...");
            this.localizationIo.close();
            this.localizationIo = null;
            log.debug("Closed existing localization interface.");
        }

        log.debug("Establishing new localization interface...");

        try {
            int incomingPort = settings.network().localization().incomingPort();
            int outgoingPort = settings.network().localization().outgoingPort();
            String incomingTopic = settings.network().localization().incomingTopic();
            String outgoingTopic = settings.network().localization().outgoingTopic();
            log.debug("incomingPort={}", incomingPort);
            log.debug("outgoingPort={}", outgoingPort);
            log.debug("incomingTopic={}", incomingTopic);
            log.debug("outgoingTopic={}", outgoingTopic);
            // Note if the external library fails to bind the socket, we will only see an error in the log - we can not detect that failure
            org.mbari.vcr4j.sharktopoda.client.localization.IO io = new org.mbari.vcr4j.sharktopoda.client.localization.IO(incomingPort, outgoingPort, incomingTopic, outgoingTopic);
            log.info("localization initialised, incoming {}:{}, outgoing {}:{}", incomingPort, incomingTopic, outgoingPort, outgoingTopic);
            this.localizationIo = io;
        } catch (Exception e) {
            log.error("Failed to initialise network localization", e.getMessage());
            this.localizationIo = null;
        }
    }

    /**
     * Close down the application.
     */
    private void close() {
        log.debug("close()");

        if (localizationIo != null) {
            localizationIo.close();
        }

        if (controlIo != null) {
            controlIo.close();
        }

        nativeLog.release();
        mediaPlayerFactory.release();

        Platform.exit();
    }
}
