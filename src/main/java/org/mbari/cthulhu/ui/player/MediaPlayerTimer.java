package org.mbari.cthulhu.ui.player;

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
     * #4: was 500ms, but we should use a smaller period -- see more comments below.
     */
    private static final long PERIOD = 100;

    /**
     * Timer implementation.
     */
    private Timer timer = new Timer(true);

    /**
     * Value used in previous call to onTick.accept.
     * #4 trying finer resolution
     */
    private long previousMillis = -1;

    /**
     * Create a timer.
     *
     * @param mediaPlayer media player that generates time-changed events
     * @param onTick function to execute on each tick of the timer depending of time of previous call
     */
    MediaPlayerTimer(MediaPlayer mediaPlayer, Consumer<Long> onTick) {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (mediaPlayer.status().isPlaying()) {
                    // #4: finer resolution to notify tick:
                    final long newMillis = mediaPlayer.status().time();
                    if (newMillis != previousMillis) {
                        log.debug("onTick(newMillis={}, diff={})", newMillis, newMillis - previousMillis);
                        previousMillis = newMillis;
                        onTick.accept(newMillis);
                    }
                    // Note: even with timer period 100ms, delta changes in reported mediaPlayer.status().time()
                    // are often in the order of 400-500ms, and sometimes ~250ms, but one would expect it to be
                    // also in the order of the timer period while the media is playing.
                    //  ... onTick(newMillis=15405, diff=500)
                    //  ... onTick(newMillis=15904, diff=499)
                    //  ... onTick(newMillis=16155, diff=251)
                    //  ... onTick(newMillis=16655, diff=500)
                    //  ... onTick(newMillis=17155, diff=500)
                    //  ... onTick(newMillis=17652, diff=497)
                    //  ... onTick(newMillis=17902, diff=250)
                    //  ... onTick(newMillis=18403, diff=501)
                    // In there a way to instruct libvlc to use a finer resolution for the time status above?
                }
            }
        }, 0, PERIOD);
    }

    /**
     * Cancel the timer.
     */
    void cancel() {
        timer.cancel();
        timer = null;
    }
}
