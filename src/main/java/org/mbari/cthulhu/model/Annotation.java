package org.mbari.cthulhu.model;

import com.google.common.base.MoreObjects;
import javafx.geometry.BoundingBox;

import java.util.Optional;
import java.util.UUID;

/**
 * Model of a video annotation.
 */
public class Annotation {

    /**
     * Unique identifier for the annotation.
     */
    private final UUID id;

    /**
     *
     */
    private final long startTime;

    /**
     *
     */
    private final long endTime;

    /**
     * Area of interest, a bounding rectangle and timestamp.
     */
    private BoundingBox bounds;

    /**
     * Optional caption text.
     */
    private String caption;

    /**
     * Create a video annotation, with a specific unique identifier and a caption.
     *
     * @param id unique identifier
     * @param startTime
     * @param endTime
     * @param bounds area of interest
     * @param caption caption text
     */
    public Annotation(UUID id, long startTime, long endTime, BoundingBox bounds, String caption) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.bounds = bounds;
        this.caption = caption;
    }

    /**
     * Create a video annotation, without a caption.
     * <p>
     * The end time will be set to the same value as the start time (this is OK as when annotations are rendered there is a padding "time window" that will
     * extend any such zero duration.
     *
     * @param startTime
     * @param bounds area of interest
     */
    public Annotation(long startTime, BoundingBox bounds) {
        this(UUID.randomUUID(), startTime, startTime, bounds, null);
    }

    /**
     * Get the unique identifier for this video annotation.
     *
     * @return identifier
     */
    public final UUID id() {
        return id;
    }

    /**
     *
     *
     * @return
     */
    public final long startTime() {
        return startTime;
    }

    /**
     *
     *
     * @return
     */
    public final long endTime() {
        return endTime;
    }

    /**
     *
     *
     * @return
     */
    public final long duration() {
        return endTime - startTime;
    }

    /**
     * Get the area of interest for this video annotation.
     *
     * @return area of interest
     */
    public final BoundingBox bounds() {
        return bounds;
    }

    /**
     * Get the optional caption text for this video annotation.
     *
     * @return caption text
     */
    public final Optional<String> caption() {
        return Optional.ofNullable(caption);
    }

    /**
     * Set the area of interest.
     *
     * @param bounds area of interest
     */
    public final void bounds(BoundingBox bounds) {
        this.bounds = bounds;
    }

    /**
     * Set the caption text.
     *
     * @param caption caption text
     */
    public final void caption(String caption) {
        this.caption = caption;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("id", id)
            .add("startTime", startTime)
            .add("endTime", endTime)
            .add("bounds", bounds)
            .add("caption", caption)
            .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Annotation that = (Annotation) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
