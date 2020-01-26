package org.mbari.cthulu.ui.components.mediaplayer;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

/**
 * A base for a standard media player button.
 */
abstract public class MediaPlayerButton extends Button {

    /**
     * Associated media player.
     */
    private final MediaPlayer mediaPlayer;

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
     * @param mediaPlayer associated media player
     * @param imageResource resource path for the media player button image
     * @param fitWidth fit the button image to this width
     * @param fitHeight fit the button image to this height
     */
    public MediaPlayerButton(MediaPlayer mediaPlayer, String imageResource, int fitWidth, int fitHeight) {
        this.mediaPlayer = mediaPlayer;

        image = new Image(getClass().getResourceAsStream(imageResource));
        imageView = new ImageView(this.image);
        imageView.setFitWidth(fitWidth);
        imageView.setFitHeight(fitHeight);
        setGraphic(imageView);

        registerEventHandlers();
    }

    private void registerEventHandlers() {
        addEventHandler(ActionEvent.ACTION, actionEvent -> {
            onAction(mediaPlayer);
        });
    }

    /**
     * Get the associated media player.
     *
     * @return media player
     */
    protected final MediaPlayer mediaPlayer() {
        return mediaPlayer;
    }

    /**
     * Template method used by sub-classes to execute the action when the button is pressed.
     *
     * @param mediaPlayer associated media player
     */
    protected abstract void onAction(MediaPlayer mediaPlayer);
}
