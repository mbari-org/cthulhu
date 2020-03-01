package org.mbari.cthulu.annotations;

import javafx.collections.ListChangeListener.Change;
import javafx.geometry.BoundingBox;
import org.mbari.cthulu.model.Annotation;
import org.mbari.cthulu.ui.components.annotationview.AnnotationImageView;
import org.mbari.cthulu.ui.player.PlayerComponent;
import org.mbari.vcr4j.sharktopoda.client.localization.Localization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mbari.cthulu.app.CthulhuApplication.application;

/**
 * Controller component that manages creation, queuing and display of video annotations.
 */
final public class AnnotationController {

    private static final Logger log = LoggerFactory.getLogger(AnnotationController.class);

    private static final String DEFAULT_CONCEPT = "Annotation"; // FIXME configured?

    /**
     * Support component that manages all of the known annotations and provides those that are currently active given a particular timestamp.
     */
    private final AnnotationManager annotationManager = new AnnotationManager();

    /**
     * Associated view that displays the annotations.
     */
    private final AnnotationImageView annotationView;

    /**
     * Create an annotations controller.
     *
     * @param playerComponent associated player component
     * @param annotationView associated annotations view component
     */
    public AnnotationController(PlayerComponent playerComponent, AnnotationImageView annotationView) {
        this.annotationView = annotationView;

        application().localization()
            .getLocalizations()
            .filtered(localization -> playerComponent.uuid().equals(localization.getVideoReferenceUuid()))
            .addListener(this::handleLocalizationChanged);
    }

    /**
     * Invoked when a new annotations was created.
     *
     * @param annotation annotation that was created
     */
    public void annotationCreated(Annotation annotation) {
        log.info("annotationCreated(annotation={})", annotation);

        annotation.caption(DEFAULT_CONCEPT);

        // Immediately add the new annotation to the view
        annotationView.add(annotation);
        // Add the new annotation to the model
        annotationManager.add(singletonList(annotation));
        // Send the new annotation to the network sink
        application().localization().addLocalization(annotationToLocalization(annotation));
    }

    private void handleLocalizationChanged(Change<? extends Localization> change) {
        log.debug("handleLocalizationChanged(change={})", change);

        if (change.wasRemoved()) {
            List<? extends Localization> removed = change.getRemoved();
            log.debug("removed={}", removed);

            List<Annotation> annotations = removed.stream().map(this::localizationToAnnotation).collect(toList());
            removeAnnotations(annotations);
        } else {
            log.debug("no localization removes");
        }

        if (change.wasAdded()) {
            List<? extends Localization> added = change.getAddedSubList();
            log.debug("added={}", added);

            List<Annotation> annotations = added.stream().map(this::localizationToAnnotation).collect(toList());
            addAnnotations(annotations);
        } else {
            log.debug("no localization adds");
        }
    }

    /**
     * Remove a collection of annotations.
     * <p>
     * The annotations are immediately removed from the view (if they were currently associated with the view), and then removed from the model via the
     * {@link #annotationManager}.
     *
     * @param annotations annotations to remove
     */
    private void removeAnnotations(List<Annotation> annotations) {
        log.debug("removeAnnotations(annotations={})", annotations);

        List<UUID> removedIds = annotations.stream()
            .map(Annotation::id)
            .collect(toList());

        annotationView.remove(removedIds);

        annotationManager.remove(annotations);
    }

    /**
     * Add a collection of annotations.
     *
     * @param annotations annotations to add
     */
    private void addAnnotations(List<Annotation> annotations) {
        log.debug("addAnnotations(annotations={})", annotations);
        annotationManager.add(annotations);
    }

    /**
     * Convert an outgoing {@link Annotation to a {@link Localization} for the remote network sink.
     *
     * @param annotation annotation to convert
     * @return converted localization
     */
    private Localization annotationToLocalization(Annotation annotation) {
        return new Localization(
            annotation.caption().orElse(""),
            Duration.ofMillis(annotation.startTime()),
            annotation.id(),
            intValue(annotation.bounds().getMaxX()),
            intValue(annotation.bounds().getMinY()),
            intValue(annotation.bounds().getWidth()),
            intValue(annotation.bounds().getHeight())
        );
    }

    /**
     * Convert an incoming {@link Localization} from the remote network source to a local {@link Annotation} instance.
     *
     * @param localization localization to convert
     * @return converted annotation
     */
    private Annotation localizationToAnnotation(Localization localization) {
        return new Annotation(
            localization.getLocalizationUuid(),
            localization.getElapsedTime().toMillis(),
            localization.getEndTime().toMillis(),
            new BoundingBox(
                localization.getX(),
                localization.getY(),
                localization.getWidth(),
                localization.getHeight()
            ),
            localization.getConcept()
        );
    }

    private int intValue(Double d) {
        return d.intValue();
    }
}
