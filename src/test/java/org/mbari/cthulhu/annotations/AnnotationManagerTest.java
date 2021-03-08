package org.mbari.cthulhu.annotations;

import javafx.geometry.BoundingBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mbari.cthulhu.model.Annotation;

import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mbari.cthulhu.app.CthulhuApplication.application;

/**
 * Tests for the {@link AnnotationManager component.}
 */
public class AnnotationManagerTest {

    private AnnotationManager annotationManager;

    @BeforeAll
    public static void prepare() {
        application().settings().annotations().display().timeWindowMillis(0);
    }

    @BeforeEach
    public void setup() {
        annotationManager = new AnnotationManager();
    }

    @Test
    public void currentReturnsEmptyListWhenNoAnnotations() {
        assertTrue(annotationManager.current(0L).isEmpty());
    }

    @Test
    public void currentReturnsValueForInRange() {
        add(annotation("1", 1000L, 1999L));

        assertEquals(1, annotationManager.current(1000L).size());
        assertEquals(1, annotationManager.current(1999L).size());
    }

    @Test
    public void currentReturnsEmptyListForOutOfRange() {
        add(annotation("1", 1000L, 1999L));

        assertEquals(0, annotationManager.current(999L).size());
        assertEquals(0, annotationManager.current(2000).size());
    }

    @Test
    public void currentReturnsAllValuesForIdenticalRange() {
        add(annotation("1", 1000L, 1999L));
        add(annotation("2", 1000L, 1999L));

        assertEquals(2, annotationManager.current(1000L).size());
        assertEquals("1", annotationManager.current(1000L).get(0).caption().get());
        assertEquals("2", annotationManager.current(1000L).get(1).caption().get());

        assertEquals(2, annotationManager.current(1999L).size());
        assertEquals("1", annotationManager.current(1999L).get(0).caption().get());
        assertEquals("2", annotationManager.current(1999L).get(1).caption().get());
    }

    @Test
    public void currentReturnsAllValuesForOverlappingRange() {
        add(annotation("1", 1000, 1999));
        add(annotation("2", 1400, 1599));

        assertEquals(1, annotationManager.current(1000L).size());
        assertEquals("1", annotationManager.current(1000L).get(0).caption().get());

        assertEquals(2, annotationManager.current(1400L).size());
        assertEquals("1", annotationManager.current(1400L).get(0).caption().get());
        assertEquals("2", annotationManager.current(1400L).get(1).caption().get());

        assertEquals(1, annotationManager.current(1600L).size());
        assertEquals("1", annotationManager.current(1600L).get(0).caption().get());
    }

    @Test
    public void currentReturnsEmptyListWhenValueRemoved() {
        Annotation a1 = annotation("1", 1000, 1999);

        add(a1);
        assertEquals(1, annotationManager.current(1000L).size());

        remove(a1);
        assertEquals(0, annotationManager.current(1000L).size());
    }

    @Test
    public void currentReturnsRemainingValuesWhenValueRemovedFromIdenticalRange() {
        Annotation a1 = annotation("1", 1000, 1999);
        Annotation a2 = annotation("2", 1000, 1999);

        add(a1);
        add(a2);
        assertEquals(2, annotationManager.current(1000L).size());

        remove(a2);
        assertEquals(1, annotationManager.current(1000L).size());
        assertEquals("1", annotationManager.current(1000L).get(0).caption().get());

        remove(a1);
        assertEquals(0, annotationManager.current(1000L).size());
    }

    @Test
    public void currentReturnsRemainingValuesWhenValueRemovedFromOverlappingRange() {
        Annotation a1 = annotation("1", 1000, 1999);
        Annotation a2 = annotation("2", 1400, 1599);

        add(a1);
        add(a2);
        assertEquals(2, annotationManager.current(1400L).size());

        remove(a2);
        assertEquals(1, annotationManager.current(1400L).size());
        assertEquals("1", annotationManager.current(1400L).get(0).caption().get());

        remove(a1);
        assertEquals(0, annotationManager.current(1400L).size());
    }

    @Test
    public void removingUnknownValueHasNoEffect() {
        Annotation a1 = annotation("1", 1000, 1999);
        Annotation a2 = annotation("2", 1000, 1999);

        add(a1);
        assertEquals(1, annotationManager.current(1000L).size());

        remove(a2);
        assertEquals(1, annotationManager.current(1000L).size());
    }

    private static Annotation annotation(String caption, long start, long end) {
        return new Annotation(UUID.randomUUID(), start, end, new BoundingBox(0, 0, 0, 0), caption);
    }

    private void add(Annotation annotation) {
        annotationManager.add(singletonList(annotation));
    }

    private void remove(Annotation annotation) {
        annotationManager.remove(singletonList(annotation));
    }
}
