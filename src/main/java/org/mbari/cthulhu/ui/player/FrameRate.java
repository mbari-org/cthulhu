package org.mbari.cthulhu.ui.player;

import com.google.common.base.MoreObjects;

/**
 * Encapsulation of a frame-rate value.
 */
final public class FrameRate {

    private final int numerator;

    private final int denominator;

    private final long frameTime;

    /**
     * Create a frame-rate instance.
     *
     * @param numerator numerator part of the frame rate ratio
     * @param denominator denominator part of the frame rate ratio
     */
    public FrameRate(int numerator, int denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
        this.frameTime = Math.round(1 / ((double) numerator / denominator / 1000));
    }

    /**
     * Get the frame-rate numerator.
     *
     * @return numerator value
     */
    public int numerator() {
        return numerator;
    }

    /**
     * Get the frame-rate denominator
     *
     * @return denominator value
     */
    public int denominator() {
        return denominator;
    }

    /**
     * Get the approximate duration for a single frame.
     *
     * @return duration of a single frame, in milliseconds
     */
    public long frameTime() {
        return frameTime;
    }

    /**
     * Get the approximate frame number for a particular time.
     *
     * @param time time
     * @return frame number
     */
    public long frameForTime(long time) {
        return (long) Math.floor(time * (double) numerator / denominator / 1000);
    }

    /**
     * Get the approximate time for a particular frame number.
     *
     * @param frame frame number
     * @return time, in milliseconds
     */
    public long timeForFrame(long frame) {
        // Floor could have been used here instead, empirically using round seems to give better UX
        return Math.round(frame * ((double) 1000 * denominator / numerator));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("numerator", numerator)
            .add("denominator", denominator)
            .add("frameTime", frameTime)
            .toString();
    }
}
