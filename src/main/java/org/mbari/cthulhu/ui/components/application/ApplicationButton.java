package org.mbari.cthulhu.ui.components.application;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * A base for a standard application button.
 */
abstract public class ApplicationButton extends Button {

    private static final String STYLE_CLASS = "app-button";

    protected final Stage stage;

    /**
     * Button image.
     */
    private final Image image;

    /**
     * Container for the button image.
     */
    private final ImageView imageView;

    /**
     * Create a media player button.
     *
     * @param stage
     * @param imageResource resource path for the media player button image
     */
    public ApplicationButton(Stage stage, String imageResource) {
        this.stage = stage;

        getStyleClass().add(STYLE_CLASS);

        image = new Image(getClass().getResourceAsStream(imageResource));
        imageView = new ImageView(this.image);
        setGraphic(imageView);

        registerEventHandlers();
    }

    private void registerEventHandlers() {
        addEventHandler(ActionEvent.ACTION, actionEvent -> {
            onAction();
        });
    }

    /**
     * Template method used by sub-classes to execute the action when the button is pressed.
     */
    protected abstract void onAction();
}
