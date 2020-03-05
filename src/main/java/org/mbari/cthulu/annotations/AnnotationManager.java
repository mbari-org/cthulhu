package org.mbari.cthulu.annotations;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import org.mbari.cthulu.model.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static org.mbari.cthulu.app.CthulhuApplication.application;

/**
 * Component responsible for providing the annotation/localization instances that are active at any given point in time.
 * <p>
 * An annotation is active if the current elapsed time of the media is greater than or equal to the annotation start time, and the current elapsed time is less
 * then or equal to the annotation end time.
 * <p>
 * The fundamental approach is to maintain a {@link RangeMap} which can be given a key (the current elapsed time) to return the list of annotations that has a
 * start-time/end-time range that spans that key value.
 * <p>
 * There are some complexities however.
 * <p>
 * The RangeMap does not intrinsically support more than one value with the exact same range, so for example if two annotations each had the exact same start
 * time and end time this would ordinarily not be possible. The situation may be extremely unlikely, but it is clearly possible. Therefore rather than having
 * the map values be a single {@link Annotation} instance, we must instead maintain a {@link List} of those annotations.
 * <p>
 * To support this approach, we must also use {@link RangeMap#merge(Range, Object, BiFunction)} rather than simply {@link RangeMap#put(Range, Object)} so that
 * if we do encounter two values with the exact same range we can add the new value to the existing list.
 * <p>
 * Similarly when removing values, it is not straightforward to simply remove the precise value - instead we must get the list of annotations that have the same
 * start time as the annotation to remove. If only one item is returned, we remove the entire list from the map (the range key must be reconstituted by using
 * the start time and the end time for the annotation being removed). If more than one item is returned, we must instead find the item to remove within the list
 * (by comparing the unique ids) and remove only that item, preserving the list.
 * <p>
 * Note there is no concept of updating an in-place annotation - an update should be performed by a separate remove then an add.
 */
@SuppressWarnings("UnstableApiUsage")
final class AnnotationManager {

    private static final Logger log = LoggerFactory.getLogger(AnnotationManager.class);

    /**
     * Maintain a separate map of annotation ids to the time range they are active.
     * <p>
     * This map is used because it is not possible to rely on the start time and and time to locate an already existing annotation. This is due to the possibly
     * varying time window padding settings which extends the start and time by some delta. This map is therefore used to remember the exact range that was used
     * (which would include the time window value at the instant the annotation was added) so it can be retrieved subsequently by id.
     */
    private final Map<UUID, Range<Long>> rangesByUuid = new HashMap<>();

    /**
     * Map of all currently known annotations, keyed by their active time range.
     * <p>
     * This map contains all of the currently known annotation components whether they are currently active (based on their timestamp) or not.
     * <p>
     * The map values are lists of annotations, since although unlikely it is possible that two distinct annotations have the exact same time range.
     */
    private final RangeMap<Long, List<Annotation>> annotationsByElapsedTime = TreeRangeMap.create();

    /**
     * Add a collection of annotations.
     *
     * @param annotations annotations to add
     */
    void add(List<Annotation> annotations) {
        log.debug("add(annotations={})", annotations);
        annotations.forEach(this::add);
    }

    /**
     * Update a collection of annotations.
     *
     * @param annotations annotations to update
     */
    void update(List<Annotation> annotations) {
        log.debug("update(annotations={})", annotations);
        annotations.forEach(this::update);
    }

    /**
     * Remove a collection of annotations.
     *
     * @param annotations annotations to remove
     */
    void remove(List<Annotation> annotations) {
        log.debug("remove(annotations={})", annotations);
        annotations.forEach(this::remove);
    }

    List<Annotation> current(long elapsedTime) {
        log.trace("current(elapsedTime={})", elapsedTime);
        List<Annotation> result = annotationsByElapsedTime.get(elapsedTime);
        return result != null ? result: emptyList();
    }

    /**
     * Add a single annotation.
     *
     * @param addedAnnotation annotation
     */
    private void add(Annotation addedAnnotation) {
        log.debug("add(addedAnnotation={})", addedAnnotation);
        Range<Long> range = range(addedAnnotation);
        rangesByUuid.put(addedAnnotation.id(), range);
        // Most of the time this will create a lightweight singleton list wrapper, it is only the unlikely case that an annotation has the exact same start and
        // end times that will cause a list concatenation
        annotationsByElapsedTime.merge(
            range,
            singletonList(addedAnnotation),
            (annotations1, annotations2) -> concat(annotations1.stream(), annotations2.stream()).collect(toList())
        );
    }

    private void update(Annotation updatedAnnotation) {
        log.debug("update(updatedAnnotation={})", updatedAnnotation);
        Range<Long> range = rangesByUuid.get(updatedAnnotation.id());
        if (range == null) {
            log.warn("Update ignored unknown annotation {}", updatedAnnotation.id());
            return;
        }
        annotationsByElapsedTime.get(range.lowerEndpoint()).stream()
            .filter(annotation -> annotation.id().equals(updatedAnnotation.id()))
            .forEach(existingAnnotation -> existingAnnotation.caption(updatedAnnotation.caption().orElse(null)));
    }

    /**
     * Remove a single annotation.
     *
     * @param removedAnnotation annotation
     */
    private void remove(Annotation removedAnnotation) {
        log.debug("remove(removedAnnotation={})", removedAnnotation);
        Range<Long> range = rangesByUuid.get(removedAnnotation.id());
        if (range == null) {
            log.warn("Remove ignored unknown annotation {}", removedAnnotation.id());
            return;
        }
        List<Annotation> inRangeAnnotations = annotationsByElapsedTime.get(range.lowerEndpoint());
        if (inRangeAnnotations.size() == 1) {
            // This is the only annotation in the list, so remove the entire list
            log.debug("Removing last annotation in this range");
            annotationsByElapsedTime.remove(range);
        } else {
            // This is not the only annotation in the list, so iterate the list to find the specific one and remove it
            log.debug("Removing annotation from range");
            inRangeAnnotations.removeIf(annotation -> annotation.id().equals(removedAnnotation.id()));
        }
    }

    private Range<Long> range(Annotation annotation) {
        int timeWindow = application().settings().annotations().display().timeWindow() * 1000;
        return Range.closed(annotation.startTime() - timeWindow, annotation.endTime() + timeWindow);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
            .add("annotationsByElapsedTime", annotationsByElapsedTime)
            .toString();
    }
}
