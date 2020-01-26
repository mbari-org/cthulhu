package org.mbari.cthulu;

import javafx.application.Application;
import javafx.stage.Stage;
import org.mbari.cthulu.ui.player.PlayerComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.mbari.cthulu.Banner.banner;
import static org.mbari.cthulu.app.CthulhuApplication.application;

/**
 * JavaFX application launcher for the Cthulu application.
 */
public class CthuluLauncher extends Application {

    private static final Logger log = LoggerFactory.getLogger(CthuluLauncher.class);

    /**
     * Emit the application banner to the log immediately on startup.
     */
    static {
        log.info(banner());
    }

    @Override
    public final void start(Stage primaryStage) {
        log.debug("start()");

        PlayerComponent playerComponent = application().open();

        // If a command-line argument was specified, assume it's an MRL and try and play it
        List<String> params = getParameters().getRaw();
        if (!params.isEmpty()) {
            String mrl = params.get(0);
            log.debug("mrl={}", mrl);
            playerComponent.mediaPlayer().media().play(mrl);
        }
    }

    @Override
    public final void stop() {
        log.debug("stop()");
        application().saveSettings();
    }

    /**
     * Application entry-point.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
