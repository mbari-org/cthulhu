package org.mbari.cthulu.ui.components.annotationview;

import com.google.common.base.Strings;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import org.mbari.cthulu.model.Annotation;
import org.mbari.cthulu.ui.components.layout.LayoutSupport;

import static org.mbari.cthulu.app.CthulhuApplication.application;
import static org.mbari.cthulu.ui.components.annotationview.ResourceFactory.createAnnotationRectangle;
import static org.mbari.cthulu.ui.components.layout.LayoutSupport.layoutNode;

/**
 * A visual component to render an annotation.
 * <p>
 * An annotation comprises a bounding rectangle for an area of an interest, and an associated caption.
 */
class AnnotationComponent extends Group {

    private final Annotation annotation;

    private final Rectangle rectangle;

    private final CaptionComponent captionComponent;

    /**
     * Create a video annotation component.
     *
     * @param annotation associated annotation
     */
    public AnnotationComponent(Annotation annotation) {
        this.annotation = annotation;

        rectangle = createAnnotationRectangle(0, 0); // FIXME remove params
        captionComponent = new CaptionComponent();

        setManaged(false);

        setCaption(annotation.caption().orElse(null));

        // Perform an immediate layout of the caption component so its proper size can be determined
        layoutNode(captionComponent);

        getChildren().addAll(rectangle, captionComponent);
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
        AnnotationImageView videoView = (AnnotationImageView) getParent();
        if (videoView == null) {
            return;
        }

        Bounds videoBounds = videoView.videoViewBounds();

        int borderSize = application().settings().annotations().display().borderSize();
        int captionBorderSize = 3;
        int captionGap = 2;

        captionComponent.applyCss();
        captionComponent.layout();

        Bounds captionBounds = captionComponent.getBoundsInParent();

        double captionWidth = captionBounds.getWidth() + (2 * captionBorderSize);
        double captionHeight = captionBounds.getHeight();

        // Optimal position is to left-align
        double x = -borderSize;
        // Check if the caption fits
        if (getLayoutX() + captionWidth > videoBounds.getWidth()) {
            // Optimal position for caption does not fit, so right-align instead
            x = rectangle.getWidth() - captionWidth + (2 * borderSize);
        }

        // Optional position is to place above the annotation
        double y = 0 - borderSize - captionHeight - captionBorderSize - captionGap;
        // Check if the caption fits
        if (getLayoutY() + y < 0) {
            // Optimal position for caption does not fit, so place below the annotation instead
            y = rectangle.getHeight() + (2 * borderSize) + captionGap - 1;
        }

        // Check if the optimal position for the label fits in the display area
        captionComponent.setLayoutX(x);
        captionComponent.setLayoutY(y);
    }
}
