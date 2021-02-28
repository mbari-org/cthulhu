package org.mbari.cthulhu.annotations;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import org.mbari.cthulhu.model.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static org.mbari.cthulhu.app.CthulhuApplication.application;

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
     * Maintain a separate map of annotation ids to the annotation itself.
     * <p>
     * This is used for direct lookup instead finding the range then searching the list for that range - useful e.g. when updating the selection status of an
     * annotation.
     */
    private final Map<UUID, Annotation> annotationsByUuid = new HashMap<>();

    /**
     * Map of all currently known annotations, keyed by their active time range.
     * <p>
     * This map contains all of the currently known annotation components whether they are currently active (based on their timestamp) or not.
     * <p>
     * The map values are lists of annotations, since although unlikely it is possible that two distinct annotations have the exact same time range.
     */
    private final RangeMap<Long, List<Annotation>> annotationsByElapsedTime = TreeRangeMap.create();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Add a collection of annotations.
     *
     * @param annotations annotations to add
     */
    void add(List<Annotation> annotations) {
        log.debug("add(annotations={})", annotations);
        lock.writeLock().lock();
        try {
            annotations.forEach(this::add);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Update a collection of annotations.
     *
     * @param annotations annotations to update
     */
    void update(List<Annotation> annotations) {
        log.debug("update(annotations={})", annotations);
        lock.writeLock().lock();
        try {
            annotations.forEach(this::update);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Remove a collection of annotations.
     *
     * @param annotations annotations to remove
     */
    void remove(List<Annotation> annotations) {
        log.debug("remove(annotations={})", annotations);
        lock.writeLock().lock();
        try {
            annotations.forEach(this::remove);
        } finally {
            lock.writeLock().unlock();
        }
    }

    void select(List<UUID> annotations) {
        log.debug("select(annotations={})", annotations);
        lock.writeLock().lock();
        try {
            annotations.forEach(this::select);
        } finally {
            lock.writeLock().unlock();
        }
    }

    void deselect(List<UUID> annotations) {
        log.debug("deselect(annotations={})", annotations);
        lock.writeLock().lock();
        try {
            annotations.forEach(this::deselect);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Get the list of annotations active given a specific time.
     *
     * @param elapsedTime time
     * @return list of annotations active for the given time
     */
    List<Annotation> current(long elapsedTime) {
        log.trace("current(elapsedTime={})", elapsedTime);
        lock.readLock().lock();
        try {
            List<Annotation> result = annotationsByElapsedTime.get(elapsedTime);
            return result != null ? new ArrayList<>(result): emptyList();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Remove all annotations.
     */
    void reset() {
        log.debug("reset()");
        lock.writeLock().lock();
        try {
            annotationsByUuid.clear();
            annotationsByElapsedTime.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Add a single annotation.
     *
     * @param addedAnnotation annotation
     */
    private void add(Annotation addedAnnotation) {
        log.debug("add(addedAnnotation={})", addedAnnotation);
        
        // #4 TODO this may be removed -- just to facilitate following the code for now
        if (addedAnnotation.startTime() >= addedAnnotation.endTime()) {
            log.warn("addedAnnotation startTime={} >= endTime={}", addedAnnotation.startTime(), addedAnnotation.endTime());
        }
        
        annotationsByUuid.put(addedAnnotation.id(), addedAnnotation);
        Range<Long> range = range(addedAnnotation);
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
        Annotation existingAnnotation = annotationsByUuid.get(updatedAnnotation.id());
        if (existingAnnotation == null) {
            log.warn("Update ignored unknown annotation {}", updatedAnnotation.id());
            return;
        }
        existingAnnotation.caption(updatedAnnotation.caption().orElse(null));
        existingAnnotation.bounds(updatedAnnotation.bounds());
    }

    /**
     * Remove a single annotation.
     *
     * @param removedAnnotation annotation
     */
    private void remove(Annotation removedAnnotation) {
        log.debug("remove(removedAnnotation={})", removedAnnotation);

        // Get a range sub-map covering the entire period of the annotation that was removed - this sub-map will give us one or more ranges, any number of
        // which may contain the removed annotation (the same annotation may be present in multiple ranges)
        RangeMap<Long, List<Annotation>> subMap = annotationsByElapsedTime.subRangeMap(range(removedAnnotation));

        List<Range> obsoleteRanges = new ArrayList<>();

        // Process each range in this sub-map...
        subMap.asMapOfRanges().forEach((range, list) -> {
            if (list.size() == 1) {
                if (list.get(0).id().equals(removedAnnotation.id())) { // Must this always be true?
                    log.debug("removing empty range");
                    obsoleteRanges.add(range);
                }
            } else {
                list.removeIf(annotation -> annotation.id().equals(removedAnnotation.id()));
            }
        });

        // Delete any empty ranges post-iteration
        for (Range range : obsoleteRanges) {
            subMap.remove(range);
        }
    }

    private void select(UUID id) {
        log.debug("select(id={})", id);
        Annotation existingAnnotation = annotationsByUuid.get(id);
        if (existingAnnotation == null) {
            log.warn("Select ignored unknown annotation {}", id);
            return;
        }
        existingAnnotation.selected(true);
    }

    private void deselect(UUID id) {
        log.debug("deselect(id={})", id);
        Annotation existingAnnotation = annotationsByUuid.get(id);
        if (existingAnnotation == null) {
            log.warn("Deselect ignored unknown annotation {}", id);
            return;
        }
        existingAnnotation.selected(false);
    }

    private Range<Long> range(Annotation annotation) {
        int timeWindow = application().settings().annotations().display().timeWindowMillis();
        return Range.closed(annotation.startTime() - timeWindow, annotation.endTime() + timeWindow);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
            .add("annotationsByElapsedTime", annotationsByElapsedTime)
            .toString();
    }

    private void dumpState(String msg) {
        log.trace("{}:", msg);
        Map<Range<Long>, List<Annotation>> map = annotationsByElapsedTime.asMapOfRanges();
        for (Range<Long> range : map.keySet()) {
            log.trace("Range {} to {}", range.lowerEndpoint(), range.upperEndpoint());
            List<Annotation> values = map.get(range);
            for (Annotation value : values) {
                log.trace(" {} \"{}\" {}-{}", value.id(), value.caption().get(), value.startTime(), value.endTime());
            }
        }
        log.debug("No more ranges");
    }
}
