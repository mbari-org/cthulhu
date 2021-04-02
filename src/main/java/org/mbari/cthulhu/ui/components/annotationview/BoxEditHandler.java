package org.mbari.cthulhu.ui.components.annotationview;

import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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
        
        void moveDragRectangle(double x, double y, double w, double h);
        
        void completeDragRectangle(UUID id);
        
        void deleteDragRectangle(UUID id);
    }

    private static final Logger log = LoggerFactory.getLogger(BoxEditHandler.class);

    private final Circle[] corners = {
        createCornerHandle(),
        createCornerHandle(),
        createCornerHandle(),
        createCornerHandle()
    };
    
    private final Listener listener;
    
    // Info to control the repositioning of the rectangle (not resizing)
    private static class MoveInfo {
        final BoundingBox bb;
        final double mouseX;
        final double mouseY;
        boolean dragging = false;
        
        public MoveInfo(BoundingBox bb, double mouseX, double mouseY) {
            this.bb = bb;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
        }
    }
    
    // Non-null only while dragging rectangle
    private MoveInfo moveInfo = null;
    
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
            corner.toFront();
        }
        setCornersVisible(true);
        this.id = id;
    }
    
    void deactivateHandling() {
        if (isActive() && dragging()) {
            corners[0].getScene().setCursor(Cursor.DEFAULT);
        }
        setCornersVisible(false);
        id = null;
        moveInfo = null;
    }
    
    void deleteRequested() {
        if (isActive()) {
            listener.deleteDragRectangle(id);
            deactivateHandling();
        }
    }
    
    private void setCornersVisible(boolean visible) {
        for (Circle corner : corners) {
            corner.setVisible(visible);
        }
    }
    
    private void setCornersToFront() {
        for (Circle corner : corners) {
            corner.toFront();
        }
    }
    
    boolean isActive() {
        return id != null;
    }
    
    // mouse* methods below for handling repositioning (not resizing)
    
    private boolean dragging() {
        return moveInfo != null && moveInfo.dragging;
    }
    
    void mousePressed(double mouseX, double mouseY) {
        assert(isActive());

        final BoundingBox bb = getBoundingBox();
        final boolean inside = bb.contains(mouseX, mouseY);
        if (inside) {
            // prepare for possible dragging:
            log.debug("mousePressed: bb={} mouseX={} mouseY={}", bb, mouseX, mouseY);
            moveInfo = new MoveInfo(bb, mouseX, mouseY);
            corners[0].getScene().setCursor(Cursor.CLOSED_HAND);
        }
        else {
            moveInfo = null;
        }
    }

    void mouseDragged(double mouseX, double mouseY) {
        if (moveInfo != null) {
            log.debug("mouseDragged mouseX={} mouseY={}", mouseX, mouseY);
            final double deltaX = mouseX - moveInfo.mouseX;
            final double deltaY = mouseY - moveInfo.mouseY;
            moveInfo.dragging = moveInfo.dragging || deltaX != 0 || deltaY != 0;
            if (moveInfo.dragging) {
                double x = moveInfo.bb.getMinX() + deltaX;
                double y = moveInfo.bb.getMinY() + deltaY;
                double w = moveInfo.bb.getWidth();
                double h = moveInfo.bb.getHeight();
                setCornersVisible(false);
                listener.moveDragRectangle(x, y, w, h);
            }
        }
    }
    
    void mouseReleased() {
        if (dragging()) {
            log.debug("mouseReleased: box edit is active");
            listener.completeDragRectangle(id);
            deactivateHandling();
        }
    }
    
    private BoundingBox getBoundingBox() {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
        for (Circle corner : corners) {
            double x = corner.getCenterX();
            double y = corner.getCenterY();
            if (minX > x) minX = x;
            if (minY > y) minY = y;
            if (maxX < x) maxX = x;
            if (maxY < y) maxY = y;
        }
        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }
    
    /**
     * Handler for resizing purposes.
     */
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
                setCornersToFront();
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
