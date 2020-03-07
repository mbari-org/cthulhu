package org.mbari.cthulhu.ui.components.settings.controls;

import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import org.tbee.javafx.scene.layout.MigPane;

/**
 * A named section-divider component.
 */
final public class SectionDivider extends MigPane {

    private static final String LAYOUT_CONSTRAINTS = "ins 0, fill";

    private static final String COLUMN_CONSTRAINTS = "[shrink]8[fill, grow]";

    private static final String STYLE_CLASS_NAME = "section";

    /**
     * Create a section divider component.
     *
     * @param name
     */
    public SectionDivider(String name) {
        super(LAYOUT_CONSTRAINTS, COLUMN_CONSTRAINTS);
        getStyleClass().add(STYLE_CLASS_NAME);
        Label nameLabel = new Label(name);
        add(nameLabel);
        add(new Separator());
    }
}
