package org.mbari.cthulu.ui.player;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * A simple view component to show when no video is playing.
 * <p>
 * This component simply renders an unscaled application logo image.
 */
final class PlayerDefaultView extends ImageView {

    private static final String LOGO_RESOURCE_NAME = "/org/mbari/cthulu/images/app/logo-mbari.png";

    PlayerDefaultView() {
        setImage(new Image(getClass().getResourceAsStream(LOGO_RESOURCE_NAME)));
    }
}
