package org.mbari.cthulu.annotations;

import javafx.collections.ListChangeListener.Change;
import javafx.geometry.BoundingBox;
import org.mbari.cthulu.model.Annotation;
import org.mbari.cthulu.model.AreaOfInterest;
import org.mbari.cthulu.ui.components.annotationview.AnnotationImageView;
import org.mbari.cthulu.ui.player.PlayerComponent;
import org.mbari.vcr4j.sharktopoda.client.localization.Localization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.mbari.cthulu.app.CthulhuApplication.application;

/**
 * Controller component that manages creation, queuing and display of video annotations.
 */
final public class AnnotationsController {

    private static final Logger log = LoggerFactory.getLogger(AnnotationsController.class);

    private static final String DEFAULT_CONCEPT = "Annotation"; // FIXME configured?

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
    public AnnotationsController(PlayerComponent playerComponent, AnnotationImageView annotationView) {
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
        annotationView.add(annotation);

        application().localization().addLocalization(new Localization(
            annotation.caption().orElse(""),
            Duration.ofMillis(annotation.areaOfInterest().startTime()),
            UUID.randomUUID(),
            intValue(annotation.areaOfInterest().bounds().getMaxX()),
            intValue(annotation.areaOfInterest().bounds().getMinY()),
            intValue(annotation.areaOfInterest().bounds().getWidth()),
            intValue(annotation.areaOfInterest().bounds().getHeight())
        ));
    }

    private int intValue(Double d) {
        return d.intValue();
    }

    private void handleLocalizationChanged(Change<? extends Localization> change) {
        log.debug("handleLocalizationChanged(change={})", change);

        if (change.wasRemoved()) {
            List<? extends Localization> removed = change.getRemoved();
            log.debug("removed={}", removed);

            List<UUID> removedIds = removed.stream()
                .map(Localization::getLocalizationUuid)
                .collect(toList());

            annotationView.remove(removedIds);
        } else {
            log.debug("no localization removes");
        }

        if (change.wasAdded()) {
            List<? extends Localization> added = change.getAddedSubList();
            log.debug("added={}", added);

            List<Annotation> annotations = added.stream().map(this::localizationToAnnotation).collect(toList());

            annotationView.add(annotations);
        } else {
            log.debug("no localization adds");
        }
    }

    /**
     * Convert an incoming {@link Localization} from the remote network source to a local {@link Annotation} instance.
     *
     * @param localization localization to convert
     * @return converted annotation
     */
    private Annotation localizationToAnnotation(Localization localization) {
        // FIXME need to use the endTime or duration
        return new Annotation(
            localization.getLocalizationUuid(),
            new AreaOfInterest(
                localization.getElapsedTime().toMillis(),
                new BoundingBox(
                    localization.getX(),
                    localization.getY(),
                    localization.getWidth(),
                    localization.getHeight()
                )
            ),
            localization.getConcept()
        );
    }
}
