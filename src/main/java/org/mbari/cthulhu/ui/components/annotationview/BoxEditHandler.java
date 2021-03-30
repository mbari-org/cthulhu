package org.mbari.cthulhu.ui.components.annotationview;

import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

/**
 * Helper for editing of existing box.
 */
public class BoxEditHandler {
    /**
     * Interface for AnnotationImageView to react to events associated with the editing.
     */
    interface Listener {
        void startDragRectangle(double anchorX, double anchorY, double width, double height, double x, double y);
        void continueDragRectangle(double x, double y);
        void completeDragRectangle(UUID id);
    }

    private static final Logger log = LoggerFactory.getLogger(BoxEditHandler.class);

    private final Circle[] corners = {
        createCornerHandle(),
        createCornerHandle(),
        createCornerHandle(),
        createCornerHandle()
    };
    
    private final Listener listener;
    
    // Non-null only when this handler is active
    private UUID id = null;
    
    /**
     * Create a helper instance.
     */
    public BoxEditHandler(Listener listener) {
        this.listener = listener;
        registerEventHandlers();
    }
    
    public Collection<Shape> getComponents() {
        return Arrays.asList(corners);
    }

    private void registerEventHandlers() {
        for (int index = 0; index < corners.length; ++index) {
            CornerMouseEventHandler cmh = new CornerMouseEventHandler(index);
            corners[index].setOnMousePressed(cmh);
            corners[index].setOnMouseDragged(cmh);
            corners[index].setOnMouseReleased(cmh);
        }
    }
    
    /**
     * Activate the dispatch.
     *
     * @param id   ID of associated annotation.
     *             This will be notified upon completion of the editing.
     * @param b    Bounding box corresponding to associated rectangle.
     */
    void activateHandling(UUID id, BoundingBox b) {
        deactivateHandling();

        log.debug("activateHandling id={} b={}", id, b);
        // set position of the 4 corners:
        double x1 = b.getMinX();
        double y1 = b.getMinY();
        double x2 = b.getMaxX();
        double y2 = b.getMaxY();
        double[] xys = {x1, y1, x2, y1, x2, y2, x1, y2};
        for (int j = 0; j < xys.length; j += 2) {
            double x = xys[j];
            double y = xys[j + 1];
            int index = j / 2;
            Circle corner = corners[index];
            corner.setCenterX(x);
            corner.setCenterY(y);
            corner.setVisible(true);
            corner.toFront();
        }
        this.id = id;
    }
    
    void deactivateHandling() {
        this.id = null;
        for (Circle corner : corners) {
            corner.setVisible(false);
        }
    }
    
    boolean isActive() {
        return id != null;
    }
    
    private class CornerMouseEventHandler implements EventHandler<MouseEvent> {
        private final int index;
        
        CornerMouseEventHandler(int index) {
            this.index = index;
        }
        
        @Override
        public void handle(MouseEvent event) {
            if (!isActive()) {
                return;
            }

            //log.debug("CornerMouseEventHandler: index={} event={}", index, event);
            if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                final int oppositeCornerIndex = (index + 2) % 4;
                final double anchorX = corners[oppositeCornerIndex].getCenterX();
                final double anchorY = corners[oppositeCornerIndex].getCenterY();
                final double width = Math.abs(corners[index].getCenterX() - anchorX);
                final double height = Math.abs(corners[index].getCenterY() - anchorY);
                final double x = event.getX();
                final double y = event.getY();
                listener.startDragRectangle(anchorX, anchorY, width, height, x, y);
            }
            else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                final double x = event.getX();
                final double y = event.getY();
                corners[index].setCenterX(x);
                corners[index].setCenterY(y);
                listener.continueDragRectangle(x, y);
            }
            else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
                listener.completeDragRectangle(id);
                deactivateHandling();
            }
        }
    }
    
    /**
     * Create a handle to be associated with a corner of some rectangle
     * for purposes of resizing such rectangle by dragging the corner handle.
     */
    private static Circle createCornerHandle() {
        Circle corner = new Circle(0, 0, 0);
        corner.setFill(null);
        corner.setPickOnBounds(true);
        corner.setMouseTransparent(false);
        corner.setManaged(false);
        corner.setStrokeType(StrokeType.INSIDE);
        corner.setVisible(false);
        
        // TODO take some of these attributes from application settings
        corner.setStroke(Color.RED);
        corner.setStrokeWidth(2);
        corner.setRadius(10);
        
        return corner;
    }
}
