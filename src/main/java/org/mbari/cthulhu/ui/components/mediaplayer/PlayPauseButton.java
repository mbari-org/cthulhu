package org.mbari.cthulhu.ui.components.mediaplayer;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;

/**
 * A play/pause media player toggle button.
 */
final public class PlayPauseButton extends MediaPlayerToggleButton {

    private static final Logger log = LoggerFactory.getLogger(PlayPauseButton.class);

    private static final String PLAY_IMAGE_RESOURCE = "/org/mbari/cthulhu/icons/player/play_24dp.png";

    private static final String PAUSE_IMAGE_RESOURCE = "/org/mbari/cthulhu/icons/player/pause_24dp.png";

    private static final int FIT_SIZE = 48;

    /**
     * Track the stopped state of the media player.
     * <p>
     * This is updated by a native media player thread.
     */
    private volatile boolean stopped = true;

    /**
     * Create a play/pause button.
     *
     * @param mediaPlayer associated media player
     */
    public PlayPauseButton(MediaPlayer mediaPlayer) {
        super(mediaPlayer, PLAY_IMAGE_RESOURCE, PAUSE_IMAGE_RESOURCE, FIT_SIZE, FIT_SIZE);

        registerEventHandlers();
    }

    private void registerEventHandlers() {
        mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void playing(MediaPlayer mediaPlayer) {
                setPauseImage();
                stopped = false;
            }

            @Override
            public void paused(MediaPlayer mediaPlayer) {
                setPlayImage();
            }

            @Override
            public void stopped(MediaPlayer mediaPlayer) {
                setPlayImage();
                stopped = true;
            }
        });
    }

    private void setPlayImage() {
        Platform.runLater(() -> setOriginalImage());
    }

    private void setPauseImage() {
        Platform.runLater(() -> setAlternateImage());
    }

    @Override
    protected void onAction(MediaPlayer mediaPlayer) {
        log.debug("stopped={}  mediaPlayer.status().time()={}", stopped, mediaPlayer.status().time());
        if (stopped) {
            // If the media player is stopped, then play() is used to restart the media from the beginning
            mediaPlayer().controls().play();
        } else {
            // If the media player is not stopped, pause() is used to toggle play/plause
            mediaPlayer().controls().pause();
        }
    }
}
