package org.mbari.cthulu.ui.components.mediaplayer;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

/**
 * A base for a standard media player toggle button.
 * <p>
 * It is up to sub-classes to implement the toggle behaviour (since it may depend on external factors).
 */
abstract public class MediaPlayerToggleButton extends MediaPlayerButton {

    /**
     * Alternate image to display when the button is toggled.
     */
    private final Image alternateImage;

    /**
     * Original image to display when the button is not toggled.
     */
    private final Image originalImage;

    /**
     * Create a media player toggle button.
     *
     * @param mediaPlayer associated media player
     * @param imageResource resource path for the media player button image
     * @param alternateImageResource resource path for the alternate (toggled) media player button image
     * @param fitWidth fit the button image to this width
     * @param fitHeight fit the button image to this height
     */
    public MediaPlayerToggleButton(MediaPlayer mediaPlayer, String imageResource, String alternateImageResource, int fitWidth, int fitHeight) {
        super(mediaPlayer, imageResource, fitWidth, fitHeight);

        this.alternateImage = new Image(getClass().getResourceAsStream(alternateImageResource));
        this.originalImage = imageView().getImage();
    }

    /**
     * Get the image wrapper component.
     *
     * @return image view
     */
    private final ImageView imageView() {
        // This cast is always safe for this component
        return (ImageView) getGraphic();
    }

    /**
     * Show the alternate (toggled) image.
     */
    protected final void setAlternateImage() {
        imageView().setImage(alternateImage);
    }

    /**
     * Show the default image.
     */
    protected final void setOriginalImage() {
        imageView().setImage(originalImage);
    }
}
