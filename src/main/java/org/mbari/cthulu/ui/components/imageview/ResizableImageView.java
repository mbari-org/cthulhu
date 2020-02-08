package org.mbari.cthulu.ui.components.imageview;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper component to make an {@link ImageView} component resize properly in its container, and to properly overlay
 * child components on the image view.
 * <p>
 * Ordinarily an {@link ImageView} component, in a layout with other components, does not properly resize even when
 * bound to the width/height properties of its parent container. Usually this manifests as correctly scaling the image
 * <em>up</em> when increasing the window size, but not correctly scaling back <em>down</em> again when the window size
 * is decreased.
 * <p>
 * The strategy here is to override the {@link #layoutChildren()} method of a wrapper {@link Pane} to specifically
 * adjust the size of the image view to match its parent, and to reposition the {@link ImageView} to ensure it is
 * centered within its parent.
 * <p>
 * In addition, any other children added to the component will have their position translated so as to be relative to
 * the wrapped {@link ImageView} rather than the container itself.
 * <p>
 * This is necessary because the {@link ImageView} itself can not contain children.
 */
public class ResizableImageView extends Pane {

    private static final Logger log = LoggerFactory.getLogger(ResizableImageView.class);

    /**
     * The component being wrapped.
     */
    protected final ImageView imageView;

    /**
     * Image width scaling factor for the image within its container.
     */
    private double scaleX;

    /**
     * Image height scaling factor for the image within its container.
     */
    private double scaleY;

    /**
     * Previous value of the width of this container.
     */
    private double lastWidth = -1;

    /**
     * Previous value of the height of this container.
     */
    private double lastHeight = -1;

    /**
     * Create a wrapper for a resizable {@link ImageView}.
     *
     * @param imageView the component to wrap
     */
    public ResizableImageView(ImageView imageView) {
        this.imageView = imageView;

        getChildren().add(imageView);

        registerEventHandlers();
    }

    /**
     * Register the event handlers necessary to react to changes in component size.
     */
    private void registerEventHandlers() {
        imageView.imageProperty().addListener((observableValue, oldValue, newValue) -> imageChanged());
        imageView.fitWidthProperty().addListener((observableValue, oldValue, newValue) -> sizeChanged());
        imageView.fitHeightProperty().addListener((observableValue, oldValue, newValue) -> sizeChanged());
    }

    /**
     * Invoked when the image of the contained {@link ImageView} has changed (this will occur each time new media is
     * played).
     */
    private void imageChanged() {
        log.trace("imageChanged()");
        lastWidth = lastHeight = -1;
    }

    /**
     * Invoked when the size of the contained {@link ImageView} has changed.
     */
    private void sizeChanged() {
        log.trace("sizeChanged()");
        Image image = imageView.getImage();
        if (image != null) {
            Bounds bounds = imageView.getBoundsInParent();
            this.scaleX = image.getWidth() / bounds.getWidth();
            this.scaleY = image.getHeight() / bounds.getHeight();
            log.trace("scaleX={}, scaleY={}", scaleX, scaleY);
            onNewSize();
        }
    }

    /**
     * Add a child to the view.
     * <p>
     * This method is used rather than adding via {@link #getChildren()} so that the origin can have the correct offset
     * applied <em>before</em> the child is added rather than the first time it is laid out.
     * <p>
     * Adding directly via {@link #getChildren()} can cause a momentary visual glitch while the position is adjusted
     * during the first layout.
     *
     * @param node node to add
     */
    public final void add(Node node) {
        node.setTranslateX(imageView.getLayoutX());
        node.setTranslateY(imageView.getLayoutY());
        getChildren().add(node);
    }

    @Override
    protected final void layoutChildren() {
        // This method is called every "tick" whether the size has changed or not, Only perform new layout if the size
        // changed since last time
        double width = getWidth();
        double height = getHeight();
        if (lastWidth == width && lastHeight == height) {
            return;
        }
        lastWidth = width;
        lastHeight = height;

        // Fit the image view to match this container, the aspect ratio will be preserved
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        // Adjust the image view origin to center within this container
        layoutInArea(this.imageView, 0, 0, width, height, 0, HPos.CENTER, VPos.CENTER);
        // Offset the other children so they are positioned relative to the image view rather than this container
        getChildren().stream().filter(child -> child != imageView).forEach(child -> {
            child.setTranslateX(imageView.getLayoutX());
            child.setTranslateY(imageView.getLayoutY());
        });
        // With the current implementation there is no need to invoke super.layoutChildren() here
    }

    /**
     * Convert a bounding box in absolute (unscaled) coordinates to a corresponding bounding box in dsiplay (unscaled)
     * coordinates.
     *
     * @param absoluteBounds bounding box in absolute coordinates
     * @return bounding box in display coordinates
     */
    protected final BoundingBox absoluteToDisplayBounds(BoundingBox absoluteBounds) {
        return new BoundingBox(
            absoluteToDisplayX(absoluteBounds.getMinX()),
            absoluteToDisplayY(absoluteBounds.getMinY()),
            absoluteToDisplayX(absoluteBounds.getWidth()),
            absoluteToDisplayY(absoluteBounds.getHeight())
        );
    }

    /**
     * Convert a rectangle in display (scaled) coordinates to a corresponding bounding box in absolute (unscaled)
     * coordinates.
     *
     * @param displayBounds rectangle in display coordinates
     * @return bounding box in absolute coordinates
     */
    protected final BoundingBox displayToAbsoluteBounds(Rectangle displayBounds) {
        return new BoundingBox(
            displayToAbsoluteX(displayBounds.getX()),
            displayToAbsoluteY(displayBounds.getY()),
            displayToAbsoluteX(displayBounds.getWidth()),
            displayToAbsoluteY(displayBounds.getHeight())
        );
    }

    /**
     * Translate an absolute (unscaled) image X value to a corresponding display (scaled) value.
     * <p>
     * Used to translate X position and width.
     *
     * @param value absolute image X value
     * @return translated display X value
     */
    private double absoluteToDisplayX(double value) {
        return value / this.scaleX;
    }

    /**
     * Translate an absolute (unscaled) image Y value to a corresponding display (scaled) value.
     * <p>
     * Used to translate Y position and height.
     *
     * @param value absolute image Y value
     * @return translated display Y value
     */
    private double absoluteToDisplayY(double value) {
        return value / this.scaleY;
    }

    /**
     * Translate a display (unscaled) image X value to a corresponding absolute image (unscaled) value.
     * <p>
     * Used to translate X position and width.
     *
     * @param value display X value
     * @return translated absolute image X value
     */
    private double displayToAbsoluteX(double value) {
        return value * this.scaleX;
    }

    /**
     * Translate a display (unscaled) image Y value to a corresponding absolute image (unscaled) value.
     * <p>
     * Used to translate Y position and height.
     *
     * @param value display Y value
     * @return translated absolute image Y value
     */
    private double displayToAbsoluteY(double value) {
        return value * this.scaleY;
    }

    /**
     * Return the bounds of the video view within this container.
     * <p>
     * These bounds reflect the size of the video itself, ignoring any black bars.
     *
     * @return video view bounds
     */
    public final Bounds videoViewBounds() {
        return imageView.getBoundsInParent();
    }

    /**
     * Invoked for sub-classes to provide their own behaviours after a resize.
     */
    protected void onNewSize() {
    }
}
