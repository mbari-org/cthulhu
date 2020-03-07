package org.mbari.cthulhu.ui.player;

import com.google.gson.annotations.SerializedName;
import org.mbari.cthulhu.ui.components.timer.TimerLabel;

/**
 * Enumeration of timer modes that can be used with a {@link TimerLabel} component.
 */
public enum TimerMode {

    /**
     * Time elapsed from the start of the media.
     */
    @SerializedName("elapsed")
    ELAPSED,

    /**
     * Time remaining to the end of the media.
     */
    @SerializedName("remaining")
    REMAINING,

    /**
     * Duration of the media.
     */
    @SerializedName("duration")
    DURATION
}
