package org.mbari.cthulu.annotations;

import org.mbari.cthulu.model.Annotation;
import org.mbari.cthulu.ui.components.annotationview.AnnotationImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller component that manages creation, queuing and display of video annotations.
 *
 * FIXME this component is presently the bare minimal placeholder
 */
final public class AnnotationsController {

    private static final Logger log = LoggerFactory.getLogger(AnnotationsController.class);

    /**
     * Associated view that displays the annotations.
     */
    private final AnnotationImageView annotationView;

    /**
     * Create an annotations controller.
     *
     * @param annotationView associated annotations view component
     */
    public AnnotationsController(AnnotationImageView annotationView) {
        this.annotationView = annotationView;
    }

    // Just a temporary counter
    private int count = 1;

    /**
     * Invoked when a new annotations was created.
     *
     * @param annotation annotation that was created
     */
    public void annotationCreated(Annotation annotation) {
        log.info("annotationCreated(annotation={})", annotation);
        // FIXME for now, simply set some arbitrary caption text and add the new annotation immediately back to the view
        annotation.caption(String.format("Annotation %d", count++));
        annotationView.add(annotation);
    }
}
