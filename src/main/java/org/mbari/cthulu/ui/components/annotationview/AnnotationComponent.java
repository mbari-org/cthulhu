package org.mbari.cthulu.ui.components.annotationview;

import com.google.common.base.Strings;
import javafx.geometry.BoundingBox;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import org.mbari.cthulu.model.Annotation;

import static org.mbari.cthulu.ui.components.annotationview.ResourceFactory.createAnnotationRectangle;

/**
 * A visual component to render an annotation.
 * <p>
 * An annotation comprises a bounding rectangle for an area of an interest, and an associated caption.
 */
class AnnotationComponent extends Group {

    private final Annotation annotation;

    private final Rectangle rectangle;

    private final LinkComponent linkComponent;

    private final CaptionComponent captionComponent;

    /**
     * Create a video annotation component.
     *
     * @param annotation associated annotation
     */
    public AnnotationComponent(Annotation annotation) {
        this.annotation = annotation;

        rectangle = createAnnotationRectangle(0, 0); // FIXME remove params
        linkComponent = new LinkComponent();
        captionComponent = new CaptionComponent();

        setManaged(false);

        setCaption(annotation.caption().orElse(null));

        repositionComponents();

        getChildren().addAll(rectangle, linkComponent, captionComponent);
    }

    /**
     * Get the associated {@link Annotation}.
     *
     * @return annotation
     */
    final Annotation annotation() {
        return annotation;
    }

    /**
     * Set the caption text for this annotation.
     *
     * @param caption caption text
     */
    final void setCaption(String caption) {
         captionComponent.setCaption(caption);
         boolean showCaption = !Strings.isNullOrEmpty(caption);
         captionComponent.setVisible(showCaption);
         linkComponent.setVisible(showCaption);
    }

    /**
     * Set new display bounds for this annotation.
     *
     * @param bounds new bounds
     */
    final void setBounds(BoundingBox bounds) {
        setLayoutX(bounds.getMinX());
        setLayoutY(bounds.getMinY());

        rectangle.setWidth(bounds.getWidth());
        rectangle.setHeight(bounds.getHeight());

        repositionComponents();
    }

    final void settingsChanged() {
        // FIXME reapply settings to all components
//        rectangle.applySettings();
//        linkComponent.applySettings();
        captionComponent.applySettings();
    }

    private void repositionComponents() {
        // FIXME this hard-coded adjustment needs to be replaced, caption needs to make sure it fits in the parent

        linkComponent.setStartX(rectangle.getWidth());
        linkComponent.setStartY(0);
        linkComponent.setEndX(linkComponent.getStartX() + 16);
        linkComponent.setEndY(linkComponent.getStartY() - 16);

        captionComponent.setLayoutX(linkComponent.getEndX());
        captionComponent.setLayoutY(linkComponent.getEndY() - 16);
    }
}
