package org.mbari.cthulu.ui.components.annotationview;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

import static org.mbari.cthulu.app.CthulhuApplication.application;

/**
 * Support class to factor out creation of various UI resources.
 */
final class ResourceFactory {

    /**
     * Create a rectangle used to render the annotations cursor.
     *
     * @return rectangle
     */
    static Rectangle createCursorRectangle() {
        int cursorDotSize = application().settings().annotations().creation().cursorSize();
        Rectangle rectangle = new Rectangle(0, 0, cursorDotSize, cursorDotSize);
        rectangle.setFill(Color.web(application().settings().annotations().creation().cursorColour()));
        rectangle.setPickOnBounds(false);
        rectangle.setMouseTransparent(true);
        rectangle.setManaged(false);
        rectangle.setVisible(application().settings().annotations().creation().enableCursor());
        return rectangle;
    }

    /**
     * Create a rectangle used to drag new annotation rectangles.
     *
     * @return rectangle
     */
    static Rectangle createDragRectangle() {
        Rectangle rectangle = new Rectangle(0, 0, 0, 0);
        rectangle.setFill(null);
        rectangle.setStroke(Color.web(application().settings().annotations().creation().borderColour()));
        rectangle.setStrokeWidth(application().settings().annotations().creation().borderSize());
        rectangle.setStrokeType(StrokeType.OUTSIDE);
        rectangle.setManaged(false);
        rectangle.setVisible(false);
        return rectangle;
    }

    /**
     * Create a rectangle used to display an annotation.
     * <p>
     * Annotation rectangles are always created with an origin of (0,0) as they are always layed out within and
     * therefore relative to an {@link AnnotationComponent}.
     *
     * @param width rectangle width
     * @param height rectangle height
     * @return rectangle
     */
    static Rectangle createAnnotationRectangle(double width, double height) {
        Rectangle rectangle = new Rectangle(0, 0, width, height);
        rectangle.setFill(null);
        rectangle.setStroke(Color.web(application().settings().annotations().display().borderColour()));
        rectangle.setStrokeWidth(application().settings().annotations().display().borderSize());
        rectangle.setStrokeType(StrokeType.OUTSIDE);
        rectangle.setManaged(false);
        rectangle.setVisible(true);
        return rectangle;
    }

    private ResourceFactory() {
    }
}
