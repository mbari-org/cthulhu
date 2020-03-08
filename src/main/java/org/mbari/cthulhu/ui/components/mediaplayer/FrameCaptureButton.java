package org.mbari.cthulhu.ui.components.mediaplayer;

import uk.co.caprica.vlcj.player.base.MediaPlayer;

/**
 * A media player button for saving a frame capture.
 */
final public class FrameCaptureButton extends MediaPlayerButton {

    private static final String IMAGE_RESOURCE = "/org/mbari/cthulhu/icons/player/frame_capture_36dp.png";

    private static final int FIT_SIZE = 36;

    /**
     * Create a media player button.
     *
     * @param mediaPlayer associated media player
     */
    public FrameCaptureButton(MediaPlayer mediaPlayer) {
        super(mediaPlayer, IMAGE_RESOURCE, FIT_SIZE, FIT_SIZE);
    }

    @Override
    protected void onAction(MediaPlayer mediaPlayer) {
        mediaPlayer().snapshots().save();
    }
}
