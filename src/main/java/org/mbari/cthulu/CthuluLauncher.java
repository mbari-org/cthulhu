package org.mbari.cthulu;

import javafx.application.Application;
import javafx.stage.Stage;
import org.mbari.cthulu.ui.main.MainStage;
import org.mbari.cthulu.ui.player.PlayerComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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

    private Stage stage;

    @Override
    public final void start(Stage primaryStage) {
        log.debug("start()");

        this.stage = new MainStage();

        // If command-line arguments were specified, assume they are MRL and try and play them
        List<String> params = getParameters().getRaw();
        params.forEach(CthuluLauncher::openFile);
    }

    @Override
    public final void stop() {
        log.debug("stop()");

        application().settings().state().position(
            (int) Math.round(stage.getX()),
            (int) Math.round(stage.getY())
        );

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

    /**
     * Open a file in a new media player component.
     *
     * @param file file to open
     */
    private static void openFile(String file) {
        log.debug("openFile(file={})", file);
        PlayerComponent playerComponent = application().open();
        playerComponent.mediaPlayer().media().play(file);
    }
}
