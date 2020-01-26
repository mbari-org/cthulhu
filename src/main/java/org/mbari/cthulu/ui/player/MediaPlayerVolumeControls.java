package org.mbari.cthulu.ui.player;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import org.mbari.cthulu.ui.components.mediaplayer.VolumeMaxButton;
import org.mbari.cthulu.ui.components.mediaplayer.VolumeMinButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.javafx.scene.layout.MigPane;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;

/**
 *
 */
final class MediaPlayerVolumeControls extends MigPane {

    private static final Logger log = LoggerFactory.getLogger(MediaPlayerVolumeControls.class);

    private final PlayerComponent playerComponent;

    private final Slider slider;
    private final Button minButton;
    private final Button maxButton;

    public MediaPlayerVolumeControls(PlayerComponent playerComponent) {
        super("ins 6", "[][][]");

        this.playerComponent = playerComponent;

        slider = new Slider();
        slider.setMin(0.0);
        slider.setMax(1.0);
        slider.setValue(0.0);

        slider.getStyleClass().add("volume");

        minButton = new VolumeMinButton(playerComponent.mediaPlayer());
        maxButton = new VolumeMaxButton(playerComponent.mediaPlayer());

        add(minButton);
        add(slider);
        add(maxButton);

        registerEventHandlers();
    }

    private void registerEventHandlers() {
        registerSliderEventHandlers();
        registerMediaPlayerEventHandlers();
    }

    private void registerSliderEventHandlers() {
        slider.valueChangingProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!newValue) {
                setMediaPlayerVolume(slider.getValue());
            }
        });
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (slider.isValueChanging()) {
                setMediaPlayerVolume(newValue.doubleValue());
            }
        });
        playerComponent.eventSource().volume().distinctUntilChanged().subscribe(this::setControlVolume);
    }

    private void registerMediaPlayerEventHandlers() {
        playerComponent.mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void corked(MediaPlayer mediaPlayer, boolean corked) {
                log.debug("corked(corked={})", corked);
                setMuted(corked);
            }

            @Override
            public void muted(MediaPlayer mediaPlayer, boolean muted) {
                log.debug("muted(muted={})", muted);
                setMuted(muted);
            }
        });
    }

    /**
     * Set the new volume for the media player.
     *
     * @param newVolume fractional volume value, 0.0 to 1.0
     */
    private void setMediaPlayerVolume(double newVolume) {
        log.trace("setMediaPlayerVolume(newVolume={})", newVolume);
        playerComponent.mediaPlayer().audio().setVolume((int) (newVolume * 100));
    }

    /**
     * Update the slider control position.
     * <p>
     * This method will be called on a native event callback thread.
     *
     * @param newVolume new fractional position
     */
    private void setControlVolume(float newVolume) {
        log.trace("setControlVolume(newVolume={})", newVolume);
        Platform.runLater(() -> {
            // Ignore media player position changed events if the slider is currently being interacted with
            if (!slider.isValueChanging()) {
                slider.setValue(newVolume);
            }
        });
    }

    /**
     * Update the muted control status.
     * <p>
     * This method will be called on a native event callback thread.
     *
     * @param newMuted new muted status
     */
    private void setMuted(boolean newMuted) {
        log.debug("setMuted(newMuted={}", newMuted);
        // FIXME update some control? this may not be needed, it may use zero volume instead (needs checking)
    }
}
