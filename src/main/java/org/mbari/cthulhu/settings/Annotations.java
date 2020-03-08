package org.mbari.cthulhu.settings;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Settings for video annotations.
 */
final public class Annotations {

    private Creation creation;

    private Display display;

    private Selection selection;

    private Captions captions;

    /**
     * Create settings with default values.
     */
    public Annotations() {
        this.creation = new Creation();
        this.display = new Display();
        this.selection = new Selection();
        this.captions = new Captions();
    }

    /**
     * Copy settings.
     *
     * @param from settings to copy
     */
    public Annotations(Annotations from) {
        this.creation = new Creation(from.creation);
        this.display = new Display(from.display);
        this.selection = new Selection(from.selection);
        this.captions = new Captions(from.captions);
    }

    public Creation creation() {
        return creation;
    }

    public Display display() {
        return display;
    }

    public Selection selection() {
        return selection;
    }

    public Captions captions() {
        return captions;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
            .add("creation", creation)
            .add("display", display)
            .add("selection", selection)
            .add("captions", captions)
            .toString();
    }
}
