package org.mbari.cthulu.ui.player;

import javafx.application.Platform;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.mbari.cthulu.ui.components.timer.TimerLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.javafx.scene.layout.MigPane;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;

import static org.mbari.cthulu.app.CthulhuApplication.application;

/**
 * A media playback timeline component.
 */
final class TimelineComponent extends MigPane {

    private static final Logger log = LoggerFactory.getLogger(TimelineComponent.class);

    private final PlayerComponent playerComponent;

    private final MediaPlayerPositionControl slider;

    private final TimerLabel elapsedTimeLabel;

    private final TimerLabel durationLabel;

    private volatile long length;

    TimelineComponent(PlayerComponent playerComponent) {
        super("fill, ins 6", "[shrink]12[grow, fill]12[shrink]");

        this.playerComponent = playerComponent;

        slider = new MediaPlayerPositionControl(playerComponent);

        elapsedTimeLabel = new TimerLabel(TimerMode.ELAPSED);
        elapsedTimeLabel.setMinWidth(Region.USE_PREF_SIZE);
        elapsedTimeLabel.setTextFill(Color.WHITE);

        durationLabel = new TimerLabel(TimerMode.DURATION, TimerMode.REMAINING);
        durationLabel.setMinWidth(Region.USE_PREF_SIZE);
        durationLabel.setTextFill(Color.WHITE);

        add(elapsedTimeLabel);
        add(slider);
        add(durationLabel);

        registerEventHandlers();

        durationLabel.setMode(application().settings().mediaPlayer().timeDisplay());

        playerComponent.eventSource().time().distinctUntilChanged().subscribe(this::setTime);
        playerComponent.eventSource().position().distinctUntilChanged().subscribe(this::setPosition);
    }

    private void registerEventHandlers() {
        registerMediaPlayerEventHandlers();
    }

    private void registerMediaPlayerEventHandlers() {
        // Add the media player event handlers - note all of these events execute on a native media player thread
        playerComponent.mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void lengthChanged(MediaPlayer mediaPlayer, long newLength) {
                Platform.runLater(() -> setLength(newLength));
            }
        });
    }

    private void setLength(long newLength) {
        log.debug("setLength(newLength={})", newLength);
        this.length = newLength;
    }

    private void setTime(long time) {
        log.trace("setTime(time={})", time);
        Platform.runLater(() -> {
            elapsedTimeLabel.tick(time, length);
            durationLabel.tick(time, length);
        });
    }

    private void setPosition(float position) {
        log.trace("setPosition(position={})", position);
        slider.setControlPosition(position);
    }
}
