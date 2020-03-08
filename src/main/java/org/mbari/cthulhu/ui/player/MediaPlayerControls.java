package org.mbari.cthulhu.ui.player;

import org.mbari.cthulhu.ui.components.mediaplayer.FrameCaptureButton;
import org.mbari.cthulhu.ui.components.mediaplayer.PlayPauseButton;
import org.tbee.javafx.scene.layout.MigPane;

/**
 * A component implementing a control panel containing various media player controls.
 */
final class MediaPlayerControls extends MigPane {

    /**
     * Associated media player component.
     */
    private final PlayerComponent playerComponent;

    private final MediaPlayerVolumeControls volumeControls;

    private final PlayPauseButton playPauseButton;

    private final FrameCaptureButton frameCaptureButton;

    private final TimelineComponent timelineComponent;

    /**
     * Create media player controls.
     *
     * @param playerComponent
     */
    MediaPlayerControls(PlayerComponent playerComponent) {
        super("fill, ins 6", "[sg, left][center][sg, right]", "[]0[]");

        this.playerComponent = playerComponent;

        volumeControls = new MediaPlayerVolumeControls(playerComponent);
        playPauseButton = new PlayPauseButton(playerComponent.mediaPlayer());
        frameCaptureButton = new FrameCaptureButton(playerComponent.mediaPlayer());
        timelineComponent = new TimelineComponent(playerComponent);

        add(volumeControls);
        add(playPauseButton);
        add(frameCaptureButton, "wrap");
        add(timelineComponent, "span 3, grow");

        getStyleClass().add("media-player-controls");
    }
}
