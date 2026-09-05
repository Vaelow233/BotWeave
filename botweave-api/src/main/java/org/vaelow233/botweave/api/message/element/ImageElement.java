package org.vaelow233.botweave.api.message.element;

import org.vaelow233.botweave.api.message.resource.MediaSource;

public class ImageElement implements MessageElement {
    private final String name;
    private final MediaSource source;
    public ImageElement(MediaSource source) {
        this.name = null;
        this.source = source;
    }
    public ImageElement(String name, MediaSource source) {
        this.name = name;
        this.source = source;
    }

    @Override
    public String type() {
        return "image";
    }

    @Override
    public String plainString() {
        return name == null ? "[Image]" : "[Image " + name + "]";
    }

    public String name() {
        return name;
    }

    public MediaSource source() {
        return source;
    }
}
