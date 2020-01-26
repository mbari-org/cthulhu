package org.mbari.cthulu.ui.components.mediaplayer;

import uk.co.caprica.vlcj.player.base.MediaPlayer;

/**
 * A media player button for setting maximum volume.
 */
final public class VolumeMaxButton extends MediaPlayerButton {

    private static final String IMAGE_RESOURCE = "/org/mbari/cthulu/icons/player/volume_up_18dp.png";

    private static final int FIT_SIZE = 18;

    private static final int MAX_VOLUME = 100;

    /**
     * Create a media player button.
     *
     * @param mediaPlayer associated media player
     */
    public VolumeMaxButton(MediaPlayer mediaPlayer) {
        super(mediaPlayer, IMAGE_RESOURCE, FIT_SIZE, FIT_SIZE);
    }

    @Override
    protected void onAction(MediaPlayer mediaPlayer) {
        mediaPlayer().audio().setVolume(MAX_VOLUME);
    }
}
