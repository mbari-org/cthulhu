package org.mbari.cthulhu.ui.main;

import com.google.common.base.Strings;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.mbari.cthulhu.ui.components.about.AboutDialog;
import org.mbari.cthulhu.ui.player.PlayerComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static javafx.scene.input.KeyCombination.keyCombination;
import static org.mbari.cthulhu.app.CthulhuApplication.application;

/**
 * Main application launcher user interface component.
 */
final public class MainStage extends Stage {

    private static final Logger log = LoggerFactory.getLogger(MainStage.class);

    private static final String APPLICATION_ICON_RESOURCE_NAME = "/org/mbari/cthulhu/icons/app/app-logo.png";

    private static final String APPLICATION_STYLESHEET_RESOURCE_NAME = "/org/mbari/cthulhu/css/cthulhu.css";

    public MainStage() {
        setTitle(application().applicationName());
        getIcons().add(new Image(getClass().getResourceAsStream(APPLICATION_ICON_RESOURCE_NAME)));

        ApplicationButtons applicationButtons = new ApplicationButtons(this);

        Scene scene = new Scene(applicationButtons, Color.BLACK);
        scene.getStylesheets().add(getClass().getResource(APPLICATION_STYLESHEET_RESOURCE_NAME).toExternalForm());
        setScene(scene);

        setResizable(false);

        // Drag/drop from external sources
        scene.setOnDragOver(MainStage::handleDragOver);
        scene.setOnDragDropped(MainStage::handleDropped);

        // Map the standard keyboard shortcut for paste to open media based on the clipboard contents
        scene.getAccelerators().put(keyCombination("shortcut+v"), MainStage::openClipboard);
        scene.getAccelerators().put(keyCombination("shift+f1"), this::showAboutDialog);

        // Window close button
        scene.getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, MainStage::handleWindowCloseRequest);

        log.debug("rememberWindowPosition={}", application().settings().userInterface().rememberWindowPosition());
        if (application().settings().userInterface().rememberWindowPosition()) {
            setX(application().settings().state().windowX());
            setY(application().settings().state().windowY());
        }

        show();
    }

    /**
     * Handler for a drag-over event.
     * <p>
     * This is invoked when an item is dragged over the scene.
     *
     * @param dragEvent event
     */
    private static void handleDragOver(DragEvent dragEvent) {
        log.trace("handleDragOver()");
        Dragboard dragboard = dragEvent.getDragboard();
        if (dragboard.hasFiles()) {
            dragEvent.acceptTransferModes(TransferMode.COPY);
        }
        dragEvent.consume();
    }

    /**
     * Handler for a dropped event.
     * <p>
     * This is invoked when an external item (e.g. a file) is dropped onto a scene.
     *
     * @param dragEvent event
     */
    private static void handleDropped(DragEvent dragEvent) {
        log.trace("handleDropped()");
        if (dragEvent.getDragboard().hasFiles()) {
            dragEvent.getDragboard().getFiles().forEach(MainStage::openFile);
            dragEvent.setDropCompleted(true);
        } else {
            dragEvent.setDropCompleted(false);
        }
        dragEvent.consume();
    }

    /**
     * Open a file in a new media player component.
     *
     * @param file file to open
     */
    private static void openFile(File file) {
        log.debug("openFile(file={})", file);
        PlayerComponent playerComponent = application().open();
        playerComponent.mediaPlayer().media().play(file.getAbsolutePath());
    }

    /**
     * Open the contents of the clipboard in a new media player comppnent.
     */
    private static void openClipboard() {
        log.debug("openClipboard()");
        String clipboard = Clipboard.getSystemClipboard().getString();
        log.debug("clipboard={}", clipboard);
        if (!Strings.isNullOrEmpty(clipboard)) {
            PlayerComponent playerComponent = application().open();
            playerComponent.mediaPlayer().media().play(clipboard);
        }
    }

    /**
     * Handle a window-close request, cleanly shutdown and quit the application.
     *
     * @param windowEvent window event
     */
    private static void handleWindowCloseRequest(WindowEvent windowEvent) {
        log.debug("handleWindowCloseRequest()");
        application().quit();
    }

    /**
     * Show the application "About" dialog.
     */
    private void showAboutDialog() {
        log.debug("showAboutDialog()");
        new AboutDialog(this).showAndWait();
    }
}
