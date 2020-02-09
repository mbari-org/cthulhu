package org.mbari.cthulu.ui.components.layout;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;

/**
 * A support component that makes sure a {@link Node} is properly laid out <em>before</em> adding it to the
 * {@link Scene}.
 * <p>
 * Ordinarily this does not matter, but it is useful where it is necessary to have the correct node size (including
 * applied CSS) before the node is rendered for the first time - for example when laying out a component that depends on
 * measuring the precise width and height of some text, including borders and padding.
 */
final public class LayoutSupport {

    private static final class Holder {
        private static final LayoutSupport INSTANCE = new LayoutSupport();
    }

    /**
     * Root {@link Node} for the {@link Scene}.
     * <p>
     * To layout a component, it is temporarily added to a "dummy" scene as a child of this group. CSS is then applied
     * to the group and a layout performed.
     * <p>
     * The component will then have the correct width and height taking into account the metrics of the string as well
     * as all borders, padding and so on applied by the CSS.
     * <p>
     * When the component is subsequently added to its "real" scene, it automatically removes itself from this group.
     */
    private final Group group = new Group();

    /**
     * Dummy scene used only to perform temporary layouts.
     * <p>
     * The scene is cached and re-used rather than being recreated each time.
     */
    private final Scene scene = new Scene(group);

    /**
     * Layout a component.
     *
     * @param node component to layout
     * @param <T> type of component
     * @return the component
     */
    public static <T extends Node> T layoutNode(T node) {
        Group group = Holder.INSTANCE.group;
        group.getChildren().add(node);
        group.applyCss();
        group.layout();
        return node;
    }

    private LayoutSupport() {
    }
}
