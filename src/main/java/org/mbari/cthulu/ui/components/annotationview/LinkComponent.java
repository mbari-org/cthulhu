package org.mbari.cthulu.ui.components.annotationview;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;

import static org.mbari.cthulu.app.CthulhuApplication.application;

/**
 * A component used to render a link from a rectangle to an associated caption.
 */
class LinkComponent extends Line {

    /**
     * Create a link component.
     */
    LinkComponent() {
        setManaged(false);
        setStroke(Color.web(application().settings().annotations().display().borderColour()));
        setStrokeWidth(application().settings().annotations().display().borderSize());
        setStrokeType(StrokeType.CENTERED);
        setStrokeLineJoin(StrokeLineJoin.ROUND);
    }
}
