package org.mbari.cthulu.ui.player;

import io.reactivex.rxjava3.subjects.PublishSubject;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.control.Slider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;

import java.util.concurrent.TimeUnit;

import static org.mbari.cthulu.app.CthulhuApplication.application;

/**
 * A slider component used to indicate, and change, the current playback position for a media player.
 * <p>
 * Both user interaction via the slider and native media player events can affect the slider position - it is important
 * that these two sources of change do not interfere with one another.
 * <p>
 * If the user is interacting with the slider, any media player position changed events must be ignored until the user
 * has finished.
 * <p>
 * Further there are multiple types of user interaction - i.e. dragging the slider via its thumb or clicking in the
 * slider either side of the thumb.
 * <p>
 * A click either side of the thumb immediately sets the playback position to that value.
 * <p>
 * Dragging the slider via its thumb generates multiple position changed events which are throttled before being used
 * to set the new playback position.
 * <p>
 * Implementation note on fast seeking, especially while paused:
 * <p>
 * The native media player can sometimes choke on many rapid seeks back and forth while "scrubbing" through the media,
 * resulting in the current frame no longer updating until the media is toggled to play again or some other event
 * triggers a change in media position.
 * <p>
 * Sadly this is unpredictable (it does not always happen) and it can not be detected when it does happen.
 * <p>
 * Throttling the rate at which seek events are dispatched to the native media player seems to help mitigate, but not
 * entirely eradicate, this problem.
 */
final class MediaPlayerPositionControl extends Slider {

    private static final Logger log = LoggerFactory.getLogger(MediaPlayerPositionControl.class);

    private static final String STYLE_CLASS_NAME = "position";

    private final PlayerComponent playerComponent;

    /**
     * Observable used to throttle media player seek requests.
     */
    private final PublishSubject<Float> seekRequests = PublishSubject.create();

    /**
     * Flag if external position events (from the native media player) should be temporarily ignored.
     * <p>
     * This is used in addition to relying on {@link #isValueChanging()} to prevent dispatching redundant position
     * change tasks via {@link Platform#runLater(Runnable)}.
     */
    private volatile boolean ignoreExternalEvents = false;

    /**
     * Create a media player slider component.
     *
     * @param playerComponent associated player component
     */
    MediaPlayerPositionControl(PlayerComponent playerComponent) {
        this.playerComponent = playerComponent;

        getStyleClass().add(STYLE_CLASS_NAME);

        setOrientation(Orientation.HORIZONTAL);
        setValue(0);
        setMin(0);
        setMax(1);
        setSeekable(false);

        registerSliderEventHandlers();
        registerMediaPlayerEventHandlers();
    }

    /**
     * Register event handlers for the slider component.
     * <p>
     * There are numerous handlers needed to implement the full gamut of required behaviour:
     * <ul>
     *   <li>
     *     for the simple case of dragging the thumb, track the value property and emit position events while the value
     *     is changing (this will prevent external media player events from updating the slider position);
     *   </li>
     *   <li>
     *     when clicking in the slider either side of the thumb, immediately seek to that position (this will also
     *     ignore external media player events while the button is held down);
     *   </li>
     *   <li>
     *     when the mouse is subsequently released, stop ignoring external media player events;
     *   </li>
     *   <li>
     *     for actually translating the position events to media player seek requests, a throttle is used.
     *   </li>
     * </ul>
     */
    private void registerSliderEventHandlers() {
        valueChangingProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!newValue) {
                newPosition((float) getValue());
                seekRequests.onNext((float) getValue()); // this listener may not actually be needed
            }
        });
        valueProperty().addListener((observable, oldValue, newValue) -> {
            if (isValueChanging()) {
                newPosition((float) getValue());
                seekRequests.onNext(newValue.floatValue());
            }
        });
        setOnMousePressed(mouseEvent -> {
            setValueChanging(true);
            ignoreExternalEvents = true;
            // Seek immediately on a mouse press
            setMediaPlayerPosition((float) getValue());
        });
        setOnMouseReleased(mouseEvent -> {
            ignoreExternalEvents = false;
            setValueChanging(false);
        });
        seekRequests
            .distinctUntilChanged()
            .sample(application().settings().mediaPlayer().scrubThrottle(), TimeUnit.MILLISECONDS)
            .subscribe(this::setMediaPlayerPosition);
    }

    private void newPosition(float newPosition) {
        seekRequests.onNext(newPosition);
        playerComponent.eventSource().newPosition(newPosition);
        playerComponent.eventSource().newTime(playerComponent.mediaPlayer().status().time());
    }

    /**
     * Register event handlers for the media player component.
     * <p>
     * All of these event handlers execute on a native media player thread.
     */
    private void registerMediaPlayerEventHandlers() {
        playerComponent.mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void seekableChanged(MediaPlayer mediaPlayer, int newSeekable) {
                Platform.runLater(() -> setSeekable(newSeekable != 0));
            }

            @Override
            public void stopped(MediaPlayer mediaPlayer) {
                // Can not seek if stopped
                Platform.runLater(() -> setSeekable(false));
            }
        });
    }

    /**
     * Enable/disable the slider component depending on the seekable state of the current media.
     *
     * @param newSeekable <code>true</code> if the current media is seekable; <code>false</code> if it is not
     */
    private void setSeekable(boolean newSeekable) {
        log.debug("setSeekable(seekable={})", newSeekable);
        setDisable(!newSeekable);
    }

    /**
     * Request that the media player seek playback to a new position.
     *
     * @param newPosition new fractional playback position, from 0.0 to 1.0
     */
    private void setMediaPlayerPosition(float newPosition) {
        log.trace("setMediaPlayerPosition(newPosition={})", newPosition);
        playerComponent.mediaPlayer().controls().setPosition(newPosition);
    }

    /**
     * Update the slider control position.
     *
     * @param newPosition new fractional position, from 0.0 to 1.0
     */
    void setControlPosition(float newPosition) {
        log.trace("setControlPosition(newPosition={})", newPosition);
        if (!ignoreExternalEvents) {
            Platform.runLater(() -> {
                // Ignore media player position changed events if the slider is currently being interacted with
                if (!isValueChanging()) {
                    setValue(newPosition);
                }
            });
        }
    }
}
