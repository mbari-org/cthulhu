package org.mbari.cthulu.annotations;

import org.mbari.cthulu.model.Annotation;

import java.util.List;
import java.util.UUID;

final class AnnotationChanges {

    private final List<Annotation> adds;

    private final List<UUID> removes;

    AnnotationChanges(List<Annotation> adds, List<UUID> removes) {
        this.adds = adds;
        this.removes = removes;
    }

    List<Annotation> adds() {
        return adds;
    }

    List<UUID> removes() {
        return removes;
    }
}
