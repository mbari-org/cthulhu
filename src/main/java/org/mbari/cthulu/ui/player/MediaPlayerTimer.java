package org.mbari.cthulu.ui.player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

/**
 * A more stable source of time events for a media player.
 * <p>
 * The native media player sends somewhat irregular time changes which if strictly adhered to will provide a less than
 * optimal user experience, this component attempts to provide a more stable timer aiming to give smooth per-second
 * updates for timer displays.
 * <p>
 * The accuracy and other characteristics of the time events from the native media player depend on the decoder that is
 * being used to play the media, therefore it is not trivial to come up with a perfect one-size-fits-all implementation
 * for this timer. At least the approximation implemented here as better than the default.
 */
final class MediaPlayerTimer {

    private static final Logger log = LoggerFactory.getLogger(MediaPlayerTimer.class);

    /**
     * The ideal update period of one second can sometimes  cause whole seconds to be missed, so a shorter period is
     * used.
     */
    private static final long PERIOD = 500;

    /**
     * Timer implementation.
     */
    private final Timer timer = new Timer(true);

    /**
     * Previous whole seconds value.
     */
    private long previousSeconds = -1;

    /**
     * Create a timer.
     *
     * @param mediaPlayer media player that generates time-changed events
     * @param onTick function to execute on each per-second tick of the timer
     */
    MediaPlayerTimer(MediaPlayer mediaPlayer, Consumer<Long> onTick) {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (mediaPlayer.status().isPlaying()) {
                    long newTime = mediaPlayer.status().time();
                    // There are a number of strategies that could be used such as flooring rather than rounding,
                    // however empirically the approach here seems to give the best UX
                    long newSeconds = Math.round((double) newTime / 1000);
                    // Only send a new event if the whole-second value has changed since the last event
                    if (newSeconds != previousSeconds) {
                        previousSeconds = newSeconds;
                        log.trace("onTick(newSeconds={})", newSeconds);
                        onTick.accept(newSeconds * 1000);
                    }
                }
            }
        }, 0, PERIOD);
    }
}
