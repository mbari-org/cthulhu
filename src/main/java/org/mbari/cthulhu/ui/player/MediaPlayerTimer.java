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
     * Captures previously reported mediaPlayer.status().time()
     */
    private long previousMpMillis = -1;
    
    /**
     * Captures system time when previousMpMillis is updated.
     */
    private long previousSysMillis = 0;

    /**
     * This is either +1 or -1 indicating time direction according to sign of
     * reported mediaPlayer time difference.
     */
    private long timeDirection;

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
                    final long newMpMillis = mediaPlayer.status().time();
                    
                    // to value to use for onTick.accept
                    long toCallTickMillis;
    
                    if (newMpMillis != previousMpMillis) {
                        // mediaPlayer has reported a new value.
                        timeDirection = newMpMillis >= previousMpMillis ? +1 : -1;
                        toCallTickMillis = previousMpMillis = newMpMillis;
                        previousSysMillis = System.currentTimeMillis();
                    }
                    else {
                        // mediaPlayer has reported the same value as before.
                        // Let's use last known time direction and system time difference for the onTick call:
                        toCallTickMillis = previousMpMillis +
                            timeDirection * (System.currentTimeMillis() - previousSysMillis);
                    }
                    
                    log.trace("onTick: toCallTickMillis={}  timeDirection={}", toCallTickMillis, timeDirection);
                    onTick.accept(toCallTickMillis);
                }
                else {
                    previousMpMillis = -1;
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
