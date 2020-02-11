package org.mbari.cthulu.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import javafx.geometry.BoundingBox;

/**
 * Model of an immutable area of interest, composed of a start time and a bounding rectangle.
 */
final public class AreaOfInterest {

    /**
     * Start time, milliseconds from the start of the media, at which the area of interest is first active.
     */
    private final long startTime;

    /**
     * Bounds of the area of interest.
     */
    private final BoundingBox bounds;

    /**
     * Create an area of interest.
     *
     * @param startTime time, in milliseconds, from the start of the media when the area of interest becomes active
     * @param bounds bounding box of the area of interest
     */
    public AreaOfInterest(long startTime, BoundingBox bounds) {
        this.startTime = startTime;
        this.bounds = bounds;
    }

    /**
     * Get the start time, from the start of the media, when the area of interest first becomes active.
     *
     * @return start time, milliseconds
     */
    public long startTime() {
        return startTime;
    }

    /**
     * Get the bounding box encapsulating the area of interest.
     *
     * @return bounding box
     */
    public BoundingBox bounds() {
        return bounds;
    }

    /**
     * Check whether this area of interest has the save bounding rectangle as another.
     *
     * @param other other instance to compare
     * @return <code>true</code> if the bounding rectangles are the same; <code>false</code> otherwise
     */
    public boolean sameBounds(AreaOfInterest other) {
        return bounds.equals(other.bounds);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(37, startTime, bounds);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AreaOfInterest other = (AreaOfInterest) obj;
        return Objects.equal(startTime, other.startTime) && Objects.equal(bounds, other.bounds);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("startTime", startTime)
            .add("bounds", bounds)
            .toString();
    }
}
