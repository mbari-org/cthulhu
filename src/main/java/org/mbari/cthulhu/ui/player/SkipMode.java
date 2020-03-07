package org.mbari.cthulhu.ui.player;

import com.google.gson.annotations.SerializedName;

/**
 * Enumeration of "skip" modes.
 */
public enum SkipMode {

    /**
     * Skip by (approximate) number of video frames.
     */
    @SerializedName("frames")
    FRAMES,

    /**
     * Skip by time, i.e. a number of milliseconds.
     */
    @SerializedName("milliseconds")
    MILLISECONDS
}
