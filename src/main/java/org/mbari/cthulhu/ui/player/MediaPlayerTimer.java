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
 * optimal user experience, this component attempts to provide a more stable timer aiming to give smooth
 * updates for timer displays.
 * <p>
 * The accuracy and other characteristics of the time events from the native media player depend on the decoder that is
 * being used to play the media, therefore it is not trivial to come up with a perfect one-size-fits-all implementation
 * for this timer. At least the approximation implemented here as better than the default.
 */
final class MediaPlayerTimer {

    private static final Logger log = LoggerFactory.getLogger(MediaPlayerTimer.class);

    //  - While playing, this timer calls the given handler at every tick
    //  - The handler is called with the following time value depending on whether
    //    mediaPlayer.status().time() has reported a new time or not:
    //      - if the reported time is new, then that's the one used for the call
    //      - otherwise, the time is the previously reported time plus the difference in
    //        system time wrt when that last time was reported.
    //        This also takes into account the "direction of time" based on reported
    //        mediaPlayer.status().time() values, which should help better reflect the
    //        changes when the user drags the time control back and forth during playing.
    
    /**
     * A short period for this timer. At each tick, the given onTick consumer is called.
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
    private long previousSysMillisForMp = 0;

    /**
     * This is either +1 or -1 indicating time direction according to sign of
     * reported mediaPlayer time difference.
     */
    private long timeDirection;
    
    /**
     * Was media playing in previous call to the timer?
     */
    private boolean wasPlaying = false;
    
    /**
     * Captures system time when timer was called. Only actually used for logging, this
     * helps visualize how accurate the frequency of the calls are wrt intended PERIOD.
     */
    private long previousSysMillis = 0;
    
    /**
     * Create a timer.
     *
     * @param mediaPlayer media player that generates time-changed events
     * @param onTick function to execute on each tick of the timer
     */
    MediaPlayerTimer(MediaPlayer mediaPlayer, Consumer<Long> onTick) {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                final long newMpMillis = mediaPlayer.status().time();
                
                if (mediaPlayer.status().isPlaying()) {
                    wasPlaying = true;
                    
                    final long currSysMillis = System.currentTimeMillis();
                    
                    // the value to use for onTick.accept
                    long toCallTickMillis;

                    if (newMpMillis != previousMpMillis) {
                        // mediaPlayer has reported a new value.
                        timeDirection = newMpMillis >= previousMpMillis ? +1 : -1;
                        toCallTickMillis = previousMpMillis = newMpMillis;
                        previousSysMillisForMp = currSysMillis;
                    }
                    else {
                        // mediaPlayer has reported the same value as before.
                        // Let's use last known time direction and system time difference for the onTick call:
                        toCallTickMillis = previousMpMillis +
                            timeDirection * (currSysMillis - previousSysMillisForMp);
                    }
                    
                    /*
                    if (log.isTraceEnabled()) {
                        final long sysDiffMillis = currSysMillis - previousSysMillis;
                        previousSysMillis = currSysMillis;
                        log.trace("onTick: toCallTickMillis={} newMpMillis={} timeDirection={} (sysDiff={})",
                            toCallTickMillis, newMpMillis, timeDirection, sysDiffMillis
                        );
                    }
                    */

                    onTick.accept(toCallTickMillis);
                }
                else {
                    previousMpMillis = -1;
                    if (wasPlaying) {
                        // Media just paused. Let's do one more call to the handler indicating
                        // the time reported by the media player:
                        wasPlaying = false;
                        //log.trace("onTick: media just paused. Calling handler with newMpMillis={}", newMpMillis);
                        onTick.accept(newMpMillis);
                    }
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
