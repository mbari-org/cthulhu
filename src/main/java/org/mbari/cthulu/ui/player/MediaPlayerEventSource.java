package org.mbari.cthulu.ui.player;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A component that manages various event sources related to the media player.
 * <p>
 * Time-changed events from the media player are used to update various timers (playback time, duration, time remaining)
 * and similarly position-changed events are used to update a timeline progress or slider control.
 * <p>
 * There are various nuances...
 * <p>
 * When the native media player is paused, it will not send time-changed or position-changed events even if the time or
 * position is changed programmatically.
 * <p>
 * When the user interacts with timeline controls (such as dragging a slider, or jumping to a particular position within
 * the timeline, or when using "jog" controls) it is desirable that those actions also generate new time and position
 * events even when paused.
 * <p>
 * It is desirable therefore to have a single source of truth for the time and position values that are reliably updated
 * in all of the different scenarios just described.
 * <p>
 * Other components that are interested in time and/or position events use {@link #time()} or {@link #position()} to
 * get a reference to an {@link Observable} that may be subscribed to in order to receive those events.
 * <p>
 * There are similar concerns with volume changes.
 */
final class MediaPlayerEventSource {

    private static final Logger log = LoggerFactory.getLogger(MediaPlayerEventSource.class);

    /**
     * A (slightly more) stable source of time events, used to update the various media player timers.
     * <p>
     * This will be updated with regular per-second changes from the media player, and immediate changes from any user
     * interactions (like seeking).
     */
    private final PublishSubject<Long> time = PublishSubject.create();

    /**
     * Source of position events, used to update the media player timeline.
     * <p>
     * This will be updated with regular position changes from the media player, and immediate changes from any user
     * interactions (like seeking).
     */
    private final PublishSubject<Float> position = PublishSubject.create();

    /**
     * Source of volume changed events, used to update the media player volume control.
     * <p>
     * This will be updated with volume changed events from the media player (if the volume is changed by an external
     * source like an operating system volume settings widget), and immediate changes from any user interaction (like
     * using the volume slider control or the associated volume change buttons).
     */
    private final PublishSubject<Float> volume = PublishSubject.create();

    MediaPlayerEventSource() {
    }

    /**
     * Set a new time.
     *
     * @param time time value, in milliseconds
     */
    void newTime(long time) {
        log.trace("newTime(time={})", time);
        long newSeconds = Math.round((double) time / 1000);
        this.time.onNext(newSeconds * 1000);
    }

    /**
     * Set a new position.
     *
     * @param position factional position value, 0.0 to 1.0
     */
    void newPosition(float position) {
        log.trace("newPosition(position={})", position);
        this.position.onNext(position);
    }

    /**
     * Set a new volume.
     *
     * @param volume fractional volume value, 0.0 to 1.0
     */
    void newVolume(float volume) {
        log.trace("newVolume(position={})", volume);
        this.volume.onNext(volume);
    }

    /**
     * Get an observable for changes to the time.
     *
     * @return time observable
     */
    Observable<Long> time() {
        return time;
    }

    /**
     * Get an observable for changes to the position.
     *
     * @return position observable
     */
    Observable<Float> position() {
        return position;
    }

    /**
     * Get an observable for changes to the volume.
     *
     * @return volume observable
     */
    Observable<Float> volume() {
        return volume;
    }
}
