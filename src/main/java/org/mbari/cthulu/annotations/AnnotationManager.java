package org.mbari.cthulu.annotations;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import javafx.geometry.BoundingBox;
import org.mbari.cthulu.model.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
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
     * Map of all currently known annotations, keyed by their active time range.
     * <p>
     * This map contains all of the currently known annotation components whether they are currently active (based on their timestamp) or not.
     * <p>
     * The map values are lists of annotations, since although unlikely it is possible that two distinct annotations have the exact same time range.
     */
    private final RangeMap<Long, List<Annotation>> annotationsByElapsedTime = TreeRangeMap.create();

    /**
     * Identifiers for the annotations that are active, based on the current elapsed time.
     */
    private final Set<UUID> activeIds = new HashSet<>();

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
     * Remove a collection of annotations.
     *
     * @param annotations annotations to remove
     */
    void remove(List<Annotation> annotations) {
        log.debug("remove(annotations={})", annotations);
        annotations.forEach(this::remove);
    }

    Optional<AnnotationChanges> setElapsedTime(long elapsedTime) {
        log.trace("setElapsedTime(elapsedTime={})", elapsedTime);

        // Start with all annotations that are active for the given time
        List<Annotation> activeAnnotations = current(elapsedTime);

        // Determine which annotations are newly active, filtering out those that are already tracked
        List<Annotation> newActiveAnnotations = activeAnnotations.stream()
            .filter(annotation -> !activeIds.contains(annotation.id()))
            .collect(toList());

        // From the newly active annotations, extract the unique set of ids and add them to the collection of those that are active
        Set<UUID> newActiveIds = newActiveAnnotations.stream()
            .map(Annotation::id)
            .collect(toSet());
        activeIds.addAll(newActiveIds);

        // To determine which annotations are no longer active, start with the set of all ids that are currently active (not just the newly added ones)
        Set<UUID> allActiveIds = activeAnnotations.stream()
            .map(Annotation::id)
            .collect(toSet());

        // From the collection of those annotations that are currently active, remove any where their id is not in the set of all currently active ids
        List<UUID> noLongerActiveIds = activeIds.stream()
            .filter(id -> !allActiveIds.contains(id))
            .collect(toList());
        activeIds.removeAll(noLongerActiveIds);

        return Optional.of(new AnnotationChanges(newActiveAnnotations, noLongerActiveIds));
    }

    List<Annotation> current(long elapsedTime) {
        List<Annotation> result = annotationsByElapsedTime.get(elapsedTime);
        return result != null ? result: emptyList();
    }

    /**
     * Add a single annotation.
     *
     * @param annotationToAdd annotation
     */
    private void add(Annotation annotationToAdd) {
        log.debug("add(annotationToAdd={})", annotationToAdd);
        // Most of the time this will create a lightweight singleton list wrapper, it is only the unlikely case that an annotation has the exact same start and
        // end times that will cause a list concatenation
        annotationsByElapsedTime.merge(
            range(annotationToAdd),
            singletonList(annotationToAdd),
            (annotations1, annotations2) -> concat(annotations1.stream(), annotations2.stream()).collect(toList())
        );
    }

    /**
     * Remove a single annotation.
     *
     * @param annotationToRemove annotation
     */
    private void remove(Annotation annotationToRemove) {
        log.debug("remove(annotationToRemove={})", annotationToRemove);
        boolean removed = false;
        // Get the list of annotations that have the same start time as the annotation to remove
        List<Annotation> annotations = annotationsByElapsedTime.get(annotationToRemove.startTime());
        if (annotations != null) {
            log.debug("annotations=({})", annotations.size());
            // Is this the only annotation in the list for this time?
            if (annotations.size() == 1) {
                // This is the only annotation in the list, confirm that the requested annotation is actually in the list and if so remove the entire list
                if (annotations.stream().anyMatch(annotation -> annotation.id().equals(annotationToRemove.id()))) {
                    annotationsByElapsedTime.remove(range(annotationToRemove));
                    removed = true;
                }
            } else {
                // This is not the only annotation in the list, so remove it from the list (keeping the list)
                removed = annotations.removeIf(annotation -> annotation.id().equals(annotationToRemove.id()));
            }
        }
        log.debug("removed={}", removed);
        if (!removed) {
            log.warn("Attempt to remove unknown annotation {}", annotationToRemove);
        }
    }

    private Range range(Annotation annotation) {
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
