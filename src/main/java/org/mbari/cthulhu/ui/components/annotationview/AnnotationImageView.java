package org.mbari.cthulhu.ui.components.annotationview;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import org.mbari.cthulhu.model.Annotation;
import org.mbari.cthulhu.settings.Settings;
import org.mbari.cthulhu.ui.components.imageview.ResizableImageView;
import org.mbari.cthulhu.ui.player.PlayerComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.mbari.cthulhu.app.CthulhuApplication.application;
import static org.mbari.cthulhu.ui.components.annotationview.ResourceFactory.*;

/**
 * A component that provides an interactive overlay for creating video annotations.
 * <p>
 * Creating an allocation does <em>not</em> immediately add it to the view, it is the remit of the caller to accept, or
 * not, the new annotation and specifically add it to the view if appropriate.
 * <p>
 * Similarly, it is the responsibility of some other component to manage what annotations are shown, when, and for how
 * long.
 */
public class AnnotationImageView extends ResizableImageView {

    private static final Logger log = LoggerFactory.getLogger(AnnotationImageView.class);

    private static final KeyCode CANCEL_DRAG_KEY_CODE = KeyCode.ESCAPE;

    private final Rectangle cursorRectangle = createCursorRectangle();

   // private final Rectangle dragRectangle = createDragRectangle();
    private Rectangle dragRectangle = null;
    // not sure if i need this line
    private final Rectangle annotationRectangle = createAnnotationRectangle();
    private final PlayerComponent playerComponent;

    /**
     * Callback invoked when a new annotation is created.
     */
    private Consumer<Annotation> onNewAnnotation;

    /**
     * Map of all currently active annotation components, keyed by their unique identifier.
     * <p>
     * This map contains only the currently active annotation components, i.e. those components that are visible in the view at the present time.
     */
    private Map<UUID, AnnotationComponent> annotationsById = new HashMap<>();

    /**
     * Elapsed time in the video (in milliseconds) when the mouse button was pressed to start creating an annotation.
     */
    private long mousePressedTime;

    /**
     * Anchor coordinate for mouse drags.
     * <p>
     * A value of -1 means that the drag is not active.
     */
    private double anchorX = -1d;
    private double anchorY = -1d;

    /**
     * Create a view with an interactive overlay for creating video annotations.
     *
     * @param playerComponent the associated player component
     */
    public AnnotationImageView(PlayerComponent playerComponent) {
        super(playerComponent.videoImageView());

        this.playerComponent = playerComponent;
        dragRectangle = createDragRectangle();
        getChildren().addAll(cursorRectangle, dragRectangle, annotationRectangle);

        registerEventHandlers();
    }

    private void registerEventHandlers() {
        imageView.setOnMouseEntered(this::mouseEntered);
        imageView.setOnMouseExited(this::mouseExited);
        imageView.setOnMouseMoved(this::mouseMoved);
        imageView.setOnMousePressed(this::mousePressed);
        imageView.setOnMouseDragged(this::mouseDragged);
        imageView.setOnMouseReleased(this::mouseReleased);


//        enableDrag();

        makeDraggable(dragRectangle,annotationRectangle);
        //makeDraggable(annotationRectangle);

        setOnKeyPressed(this::keyPressed);

        application().settingsChanged().subscribe(this::settingsChanged);
    }

    private void mouseEntered(MouseEvent event) {
        cursorRectangle.setVisible(application().settings().annotations().creation().enableCursor());
    }

    private void mouseExited(MouseEvent event) {
        cursorRectangle.setVisible(false);
    }

    private void mouseMoved(MouseEvent event) {
        cursorRectangle.setX(event.getX());
        cursorRectangle.setY(event.getY());
    }


    private void mousePressed(MouseEvent event) {
        requestFocus();

        mousePressedTime = playerComponent.mediaPlayer().status().time();

        double x = anchorX = event.getX();
        double y = anchorY = event.getY();

        dragRectangle.setX(x);
        dragRectangle.setY(y);
        dragRectangle.setWidth(0);
        dragRectangle.setHeight(0);
        dragRectangle.setVisible(true);
    }

    // make a targetNode movable by dragging it around with the mouse.
    static void makeDraggable(Rectangle node, Rectangle curosor) {

        curosor.xProperty().bind(node.xProperty());
        curosor.yProperty().bind(node.yProperty());
        final Delta dragDelta = new Delta();
        curosor.setOnMouseDragged(me -> {
            double oldX = node.getX();
            double oldY = node.getY();


            node.setX(me.getX() - dragDelta.x);
            node.setY(me.getY() - dragDelta.y);
            node.setWidth(node.getWidth() - dragDelta.x);
            node.setHeight(node.getHeight() - dragDelta.y);

            double newX = node.getX();
            double newY = node.getY();
        });



        node.setOnMouseEntered(me -> {
            if (!me.isPrimaryButtonDown()) {
                node.getScene().setCursor(Cursor.HAND);
            }
        });
        node.setOnMouseExited(me -> {
            if (!me.isPrimaryButtonDown()) {
                node.getScene().setCursor(Cursor.DEFAULT);
            }
        });
        node.setOnMousePressed(me -> {
            if (me.isPrimaryButtonDown()) {
                node.getScene().setCursor(Cursor.DEFAULT);
            }
            dragDelta.x = me.getX() - node.getX();
            dragDelta.y = me.getY() - node.getY();
            node.getScene().setCursor(Cursor.MOVE);
        });
        node.setOnMouseReleased(me -> {
            if (!me.isPrimaryButtonDown()) {
                node.getScene().setCursor(Cursor.DEFAULT);
            }
        });
        node.setOnMouseDragged(me -> {
            double oldX = node.getX();
            double oldY = node.getY();


            node.setX(me.getX() - dragDelta.x);
            node.setY(me.getY() - dragDelta.y);

            double newX = node.getX();
            double newY = node.getY();
        });
    }

    static class Delta { double x, y; }
    // make a node movable by dragging it around with the mouse.
    private void enableDrag( Circle circle) {
        final Delta dragDelta = new Delta();
        dragRectangle.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                // record a delta distance for the drag and drop operation.
                dragDelta.x = circle.getCenterX() - mouseEvent.getX();
                dragDelta.y = circle.getCenterY() - mouseEvent.getY();
                circle.getScene().setCursor(Cursor.MOVE);
            }
        });
        dragRectangle.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                circle.getScene().setCursor(Cursor.HAND);
            }
        });
        dragRectangle.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                circle.setCenterX(mouseEvent.getX() + dragDelta.x);
                circle.setCenterY(mouseEvent.getY() + dragDelta.y);
            }
        });
        dragRectangle.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                if (!mouseEvent.isPrimaryButtonDown()) {
                    circle.getScene().setCursor(Cursor.HAND);
                }
            }
        });
        dragRectangle.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                if (!mouseEvent.isPrimaryButtonDown()) {
                    circle.getScene().setCursor(Cursor.DEFAULT);
                }
            }
        });
    }

    private void mouseDragged(MouseEvent event) {
        if (anchorX == -1) {
            return;
        }

        double x = event.getX();
        double y = event.getY();

        // Constrain the drag rectangle the display bounds of the underlying video view
        Bounds videoViewBounds = videoViewBounds();

        // Tighten the constraint to prevent the border being drawn outside the image
        int borderSize = application().settings().annotations().creation().borderSize();

        x = Math.min(x, videoViewBounds.getWidth() - borderSize);
        x = Math.max(x, borderSize);

        y = Math.min(y, videoViewBounds.getHeight() - borderSize);
        y = Math.max(y, borderSize);

        dragRectangle.setWidth(Math.abs(x - anchorX));
        dragRectangle.setHeight(Math.abs(y - anchorY));
        dragRectangle.setX(Math.min(anchorX, x));
        dragRectangle.setY(Math.min(anchorY, y));

        cursorRectangle.setX(x);
        cursorRectangle.setY(y);
    }

    private void mouseReleased(MouseEvent event) {
        if (anchorX == -1) {
            return;
        }

        //dragRectangle.setVisible(false);
        if (dragRectangle.getWidth() > 0 && dragRectangle.getHeight() > 0) {
            //newAnnotation();
        }

        mousePressedTime = -1L;
        anchorX = anchorY = -1d;
    }


    private void keyPressed(KeyEvent event) {
        if (dragRectangle.isVisible() && CANCEL_DRAG_KEY_CODE == event.getCode()) {
            dragRectangle.setVisible(false);

            mousePressedTime = -1L;
            anchorX = anchorY = -1d;
        }
    }

    /**
     * Invoked when a new annotation was created.
     * <p>
     * This does <em>not</em> create the visual representation of the annotation, nor even add that annotation as a
     * child of this component - rather it passes the new annotation to to the callback which is then in control of the
     * annotation's display.
     */
    private void newAnnotation() {
        log.debug("newAnnotation()");

        BoundingBox displayBounds = new BoundingBox(dragRectangle.getX(), dragRectangle.getY(), dragRectangle.getWidth(), dragRectangle.getHeight());
        log.debug("displayBounds={}", displayBounds);

        BoundingBox absoluteBounds = displayToAbsoluteBounds(dragRectangle);
        log.debug("absoluteBounds={}", absoluteBounds);

        Annotation annotation = new Annotation(mousePressedTime, absoluteBounds);
        log.debug("annotation={}", annotation);

        if (onNewAnnotation != null) {
            onNewAnnotation.accept(annotation);
        } else {
            log.warn("No callback for new annotations");
        }
    }

    private void settingsChanged(Settings settings) {
        log.debug("settingsChanged()");

        cursorRectangle.setVisible(settings.annotations().creation().enableCursor());
        cursorRectangle.setWidth(settings.annotations().creation().cursorSize());
        cursorRectangle.setHeight(settings.annotations().creation().cursorSize());
        cursorRectangle.setFill(Color.web(settings.annotations().creation().cursorColour()));

        dragRectangle.setStroke(Color.web(application().settings().annotations().creation().borderColour()));
        dragRectangle.setStrokeWidth(application().settings().annotations().creation().borderSize());

        getChildren()
            .filtered(child -> child instanceof AnnotationComponent)
            .forEach(annotationComponent -> ((AnnotationComponent) annotationComponent).settingsChanged());
    }

    /**
     * Set the callback to invoke when a new annotation is created.
     *
     * @param onNewAnnotation the annotation that was created
     */
    public void setOnNewAnnotation(Consumer<Annotation> onNewAnnotation) {
        this.onNewAnnotation = onNewAnnotation;
    }

    /**
     * Add a video annotation to the view.
     * <p>
     * This adds the visual representation of an annotation and overlays it on the video view.
     *
     * @param annotation annotation to add
     */
    public void add(Annotation annotation) {
        log.trace("add(annotation={})", annotation);
        if (annotationsById.containsKey(annotation.id())) {
            log.debug("Not adding already added annotation with same UUID");
            return;
        }
        AnnotationComponent annotationComponent = new AnnotationComponent(annotation);
        annotationComponent.select(annotation.selected());
        BoundingBox absoluteBounds = annotationComponent.annotation().bounds();
        add(annotationComponent);
        annotationComponent.setBounds(absoluteToDisplayBounds(absoluteBounds));
        annotationsById.put(annotation.id(), annotationComponent);
    }

    public void update(Annotation annotation, AnnotationComponent annotationComponent) {
        annotationComponent.setCaption(annotation.caption().orElse(null));
        annotationComponent.setBounds(absoluteToDisplayBounds(annotation.bounds()));
    }

    /**
     * Add one or more annotations to the view.
     *
     * @param annotations annotations to add
     */
    public void add(List<Annotation> annotations) {
        log.trace("add(annotations={})", annotations);
        annotations.forEach(this::add);
    }

    /**
     * Remove one or more annotations, given their unique identifier.
     *
     * @param idsToRemove collection of unique identifiers of the annotations to remove
     */
    public void remove(Set<UUID> idsToRemove) {
        log.trace("remove(idsToRemove={})", idsToRemove);
        List<AnnotationComponent> componentsToRemove = idsToRemove.stream()
            .map(id -> annotationsById.get(id))
            .filter(Objects::nonNull)
            .collect(toList());
        log.trace("componentsToRemove={}", componentsToRemove);
        Platform.runLater(() -> getChildren().removeAll(componentsToRemove));
        idsToRemove.forEach(annotationsById::remove);
    }

    public void select(List<UUID> annotations) {
        log.debug("select(annotations={})", annotations);
        annotations.stream()
            .map(id -> annotationsById.get(id))
            .filter(Objects::nonNull)
            .forEach(annotationComponent -> annotationComponent.select(true));
    }

    public void deselect(List<UUID> annotations) {
        log.debug("deselect(annotations={})", annotations);
        annotations.stream()
            .map(id -> annotationsById.get(id))
            .filter(Objects::nonNull)
            .forEach(annotationComponent -> annotationComponent.select(false));
    }

    @Override
    protected void onNewSize() {
        log.trace("onNewSize()");
        getChildren()
            .filtered(child -> child instanceof AnnotationComponent)
            .forEach(child -> {
                AnnotationComponent annotationComponent = (AnnotationComponent) child;
                BoundingBox absoluteBounds = annotationComponent.annotation().bounds();
                annotationComponent.setBounds(absoluteToDisplayBounds(absoluteBounds));
            });
    }

    /**
     * Set the current annotations.
     * <p>
     * This may result in the deletion of no longer active annotations, updates of existing active annotations, or the addition on new active annotations.
     *
     * @param activeAnnotations
     */
    public void setAnnotations(List<Annotation> activeAnnotations) {
        log.trace("setAnnotations(activeAnnotations={})", activeAnnotations);

        // Start with the set of all currently active ids
        Set<UUID> allIds = activeAnnotations.stream().map(Annotation::id).collect(toSet());
        // Remove the annotations that are not in the set of active ids
        Set<UUID> idsToDelete = annotationsById.keySet().stream()
            .filter(id -> !allIds.contains(id))
            .collect(toSet());
        log.trace("idsToDelete={}", idsToDelete);
        remove(idsToDelete);

        // Now adds or updates, for each of the current active annotations...
        activeAnnotations.forEach(annotation -> {
            // Is there already a visual component for this annotation?
            AnnotationComponent annotationComponent = annotationsById.get(annotation.id());
            if (annotationComponent != null) {
                // We already have a visual component for this, so it must be an update
                update(annotation, annotationComponent);
            } else {
                // We do not already have a visual component fo this, so it must be an add
                add(annotation);
            }
        });
    }

    /**
     * Reset the view, removing all annotations.
     */
    public void reset() {
        log.info("reset()");

        getChildren().removeAll(annotationsById.values());
        annotationsById = new HashMap<>();
    }

}
