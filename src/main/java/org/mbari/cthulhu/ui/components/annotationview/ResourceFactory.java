package org.mbari.cthulhu.ui.components.annotationview;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.stage.Stage;

import java.util.Arrays;

import static org.mbari.cthulhu.app.CthulhuApplication.application;

/**
 * Support class to factor out creation of various UI resources.
 */
final class ResourceFactory {

    private static final StrokeType STROKE_TYPE = StrokeType.INSIDE;

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
//        rectangle.setStroke(Color.ORANGE);
        //rectangle.setStroke(Color.web(application().settings().annotations().creation().borderColour()));
        rectangle.setStroke(Color.BLUE);
        rectangle.setStrokeWidth(application().settings().annotations().creation().borderSize());
        rectangle.setStrokeType(STROKE_TYPE);
        rectangle.setManaged(false);
        rectangle.setVisible(false);
        return rectangle;
    }

    /**
     * Create a rectangle used to display an annotation.
     * <p>
     * Annotation rectangles are always created with an origin of (0,0) as they are always layed out within and
     * therefore relative to an {@link AnnotationComponent}.
     * <p>
     * Initial width and height will be zero, the caller is expected to fill these later.
     *
     * @return rectangle
     */
    static Rectangle createAnnotationRectangle() {
        Rectangle rectangle = new Rectangle(0, 0, 0, 0);
        rectangle.setFill(null);
        //rectangle.setStroke(Color.web(application().settings().annotations().display().borderColour()));
        rectangle.setStroke(Color.RED);
        rectangle.setStrokeWidth(application().settings().annotations().display().borderSize());
        rectangle.setStrokeType(STROKE_TYPE);
        rectangle.setManaged(false);
        rectangle.setVisible(true);
        return rectangle;
    }


    private ResourceFactory() {
    }

//    static class Delta { double x, y; }
//
//    Rectangle enableDrag(final Rectangle rect){
//        Rectangle rectangle = new Rectangle(0, 0, 0, 0);
//        final Delta dragDelta = new Delta();
//
//        rect.setOnMousePressed(new EventHandler<MouseEvent>() {
//            @Override
//            public void handle(MouseEvent mouseEvent) {
//
////                double offsetX = t.getSceneX() - getX();
////                double offsetY = t.getSceneY() - getY();
//            dragDelta.x = rect.getSceneX() - mouseEvent.getX();
//            dragDelta.y = rect.getY() - mouseEvent.getY();
//            rect.setCursor(Cursor.MOVE);
//            }
//        });
//
//
//        return rectangle;
//    }




}
