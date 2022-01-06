package org.mbari.cthulhu.ui.components.annotationview;

import javafx.application.Platform;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.mbari.cthulhu.model.Annotation;
import org.mbari.cthulhu.settings.Settings;
import org.mbari.cthulhu.ui.components.imageview.ResizableImageView;
import org.mbari.cthulhu.ui.player.PlayerComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
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
public class AnnotationImageView extends ResizableImageView implements BoxEditHandler.Listener {

    private static final Logger log = LoggerFactory.getLogger(AnnotationImageView.class);

    private static final KeyCode CANCEL_KEY_CODE = KeyCode.ESCAPE;
    
    private static final KeyCode EDIT_KEY_CODE = KeyCode.E;

    private final Rectangle cursorRectangle = createCursorRectangle();

    private final Rectangle dragRectangle = createDragRectangle();
    
    private final BoxEditHandler boxEditHandler = new BoxEditHandler(this);
    
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
     * Variable also used when starting to edit an existing box.
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

        getChildren().addAll(cursorRectangle, dragRectangle);
        getChildren().addAll(boxEditHandler.getComponents());

        registerEventHandlers();
    }

    private void registerEventHandlers() {
        imageView.setOnMouseEntered(this::mouseEntered);
        imageView.setOnMouseExited(this::mouseExited);
        imageView.setOnMouseMoved(this::mouseMoved);
        imageView.setOnMousePressed(this::mousePressed);
        imageView.setOnMouseDragged(this::mouseDragged);
        imageView.setOnMouseReleased(this::mouseReleased);

        setOnKeyPressed(this::keyPressed);
    
        playerComponent.eventSource().time().subscribe(this::handleTimeChanged);

        application().settingsChanged().subscribe(this::settingsChanged);
    }

    // Deactivate box editing if media is resumed playing
    private void handleTimeChanged(long ignored) {
        if (boxEditHandler.isActive()) {
            log.debug("handleTimeChanged: deactivating box edit handling");
            boxEditHandler.deactivateHandling();
            mousePressedTime = -1L;
            anchorX = anchorY = -1d;
            dragRectangle.setVisible(false);
        }
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
        double x = event.getX();
        double y = event.getY();
        log.debug("mousePressed x={} y={} mousePressedTime={}", x, y, mousePressedTime);
    
        if (boxEditHandler.isActive()) {
            boxEditHandler.mousePressed(x, y);
        }
        else {
            // brand new box started to be drawn
            startDragRectangle(x, y, 0, 0, x, y);
        }
    }
    
    public void startDragRectangle(double anchorX, double anchorY, double width, double height, double x, double y) {
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        dragRectangle.setX(anchorX);
        dragRectangle.setY(anchorY);
        dragRectangle.setWidth(width);
        dragRectangle.setHeight(height);
        dragRectangle.setVisible(true);
        dragRectangle.toFront();
    
        continueDragRectangle(x, y);
    }

    private void mouseDragged(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        if (boxEditHandler.isActive()) {
            boxEditHandler.mouseDragged(x, y);
        }
        else {
            log.debug("mouseDragged x={} y={}", x, y);
            continueDragRectangle(x, y);
        }
    }
    
    /**
     * Called in the case of creating a brand new box or resizing an existing one,
     * so this is related with dragging a corner of the box.
     * A constrain is applied so the rectangle is within the underlying video view.
     */
    public void continueDragRectangle(double x, double y) {

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
    
    /**
     * Called in the case of repositioning an existing box.
     * The rectangle is constrained to remain within the underlying video view.
     */
    public void moveDragRectangle(double x, double y, final double w, final double h) {
        //log.debug("moveDragRectangle: x={} y={} w={} h={}", x, y, w, h);
        Bounds videoViewBounds = videoViewBounds();
        int borderSize = application().settings().annotations().creation().borderSize();
        
        double maxX = videoViewBounds.getWidth() - borderSize;
        double maxY = videoViewBounds.getHeight() - borderSize;
    
        x = Math.max(x, borderSize);
        x = Math.min(x, maxX - w);
        
        y = Math.max(y, borderSize);
        y = Math.min(y, maxY - h);
        
        dragRectangle.setX(x);
        dragRectangle.setY(y);
        dragRectangle.setWidth(w);
        dragRectangle.setHeight(h);
        dragRectangle.setVisible(true);
        dragRectangle.toFront();
    }

    private void mouseReleased(MouseEvent event) {
        if (boxEditHandler.isActive()) {
            boxEditHandler.mouseReleased();
        }
        else {
            // possibly ending a brand new box
            completeDragRectangle(null);
        }
    }
    
    /**
     * Completes a box being drawn.
     * Handles brand-new box, or the editing of an existing box.
     *
     * @param id  `null` indicating this is a brand new box;
     *            otherwise, the ID of existing box whose editing has just been completed.
     */
    public void completeDragRectangle(UUID id) {
        log.debug("completeDragRectangle: id={} mousePressedTime={}", id, mousePressedTime);
        dragRectangle.setVisible(false);
        if (dragRectangle.getWidth() > 0 && dragRectangle.getHeight() > 0) {
            if (id != null) {
                // first, remove existing box:
                log.debug("completeDragRectangle: removing existing box with id={}", id);
                remove(Collections.singleton(id));
                // notify the network sink:
                application().localization().removeLocalization(id);
                // then, the just updated box is added below.

                // TODO create a newAnnotation method that creates an annotation using an exising UUID
                // (localization UUID which maps to a VARS association)
                newAnnotation(id);
            }
            else {
                // Else: it's a brand-new annotation.
                newAnnotation();
            }

        }

        mousePressedTime = -1L;
        anchorX = anchorY = -1d;
    }
    
    public void deleteDragRectangle(UUID id) {
        if (id != null) {
            log.debug("deleteDragRectangle: id={}", id);
            remove(Collections.singleton(id));
            application().localization().removeLocalization(id);
        }
        cancelDragRectangle();
    }
    
    public void cancelDragRectangle() {
        dragRectangle.setWidth(0);
        dragRectangle.setHeight(0);
        dragRectangle.setVisible(false);
        mousePressedTime = -1L;
        anchorX = anchorY = -1d;
    }
    
    private void keyPressed(KeyEvent event) {
        //log.debug("keyPressed: event={}", event);
        final KeyCode keyCode = event.getCode();

        if (CANCEL_KEY_CODE == keyCode) {
            cancelDragRectangle();
            boxEditHandler.deactivateHandling();
        }
        else if (EDIT_KEY_CODE == keyCode) {
            startBoxEditHandling();
        }
        else if (KeyCode.DELETE == keyCode && event.isShiftDown()) {
            boxEditHandler.deleteRequested();
        }
    }
    
    /**
     * Start box editing, only if media is paused, editing is not already happening,
     * and only when one single annotation is currently selected.
     */
    private void startBoxEditHandling() {
        if (playerComponent.mediaPlayer().status().isPlaying()) {
            return;
        }
        if (boxEditHandler.isActive()) {
            return;
        }
        
        // get selected annotation components:
        List<Node> annotationComponents = getChildren()
            .filtered(child ->
                (child instanceof AnnotationComponent) &&
                ((AnnotationComponent) child).isSelected()
            );

        // for now, only handling one selection:
        if (annotationComponents.size() == 1) {
            mousePressedTime = playerComponent.mediaPlayer().status().time();
            // which should equal annotationComponent.annotation().startTime().
            AnnotationComponent annotationComponent = (AnnotationComponent) annotationComponents.get(0);
            UUID id = annotationComponent.annotation().id();
            double lx = annotationComponent.getLayoutX();
            double ly = annotationComponent.getLayoutY();
            Rectangle r = annotationComponent.getRectangle();
            BoundingBox bb = new BoundingBox(lx, ly, r.getWidth(), r.getHeight());
            Platform.runLater(() -> boxEditHandler.activateHandling(id, bb));
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
        newAnnotation(UUID.randomUUID());
    }

    private void newAnnotation(UUID id) {
        log.trace("newAnnotation()");

        BoundingBox displayBounds = new BoundingBox(dragRectangle.getX(), dragRectangle.getY(), dragRectangle.getWidth(), dragRectangle.getHeight());
        log.trace("displayBounds={}", displayBounds);

        BoundingBox absoluteBounds = displayToAbsoluteBounds(dragRectangle);
        log.trace("absoluteBounds={}", absoluteBounds);

        Annotation annotation = new Annotation(id, mousePressedTime, absoluteBounds);
        log.trace("annotation={}", annotation);

        if (onNewAnnotation != null) {
            onNewAnnotation.accept(annotation);
        } else {
            log.warn("No callback for new annotations");
        }
    }

    private void settingsChanged(Settings settings) {
        log.trace("settingsChanged()");

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

        if (!boxEditHandler.isActive()) {
            Platform.runLater(this::startBoxEditHandling);
        }
    }

    public void deselect(List<UUID> annotations) {
        boxEditHandler.deactivateHandling();
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
