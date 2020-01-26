package org.mbari.cthulu.model;

import com.google.common.base.MoreObjects;

import java.util.Optional;
import java.util.UUID;

/**
 * Model of a video annotation.
 */
public class Annotation {

    /**
     * Unique identifier for the annotation.
     */
    private final String id = UUID.randomUUID().toString();

    /**
     * Area of interest, a bounding rectangle and timestamp.
     */
    private final AreaOfInterest areaOfInterest;

    /**
     * Optional caption text.
     */
    private String caption;

    /**
     * Create a video annotation, with a caption.
     *
     * @param areaOfInterest area of interest
     * @param caption caption text
     */
    public Annotation(AreaOfInterest areaOfInterest, String caption) {
        this.areaOfInterest = areaOfInterest;
        this.caption = caption;
    }

    /**
     * Create a video annotation, without a caption.
     *
     * @param areaOfInterest area of interest
     */
    public Annotation(AreaOfInterest areaOfInterest) {
        this(areaOfInterest, null);
    }

    /**
     * Get the unique identifier for this video annotation.
     *
     * @return identifier
     */
    public final String id() {
        return id;
    }

    /**
     * Get the area of interest for this video annotation.
     *
     * @return area of interest
     */
    public final AreaOfInterest areaOfInterest() {
        return areaOfInterest;
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
            .add("areaOfInterest", areaOfInterest)
            .add("caption", caption)
            .toString();
    }
}
