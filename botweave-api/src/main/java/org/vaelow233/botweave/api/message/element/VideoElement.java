package org.vaelow233.botweave.api.message.element;

import org.vaelow233.botweave.api.message.resource.MediaSource;

public class VideoElement implements MessageElement {
    private final String name;
    private final MediaSource source;
    public VideoElement(String name, MediaSource source) {
        this.name = name;
        this.source = source;
    }

    @Override
    public String type() {
        return "video";
    }

    @Override
    public String plainString() {
        return name == null ? "[Video]" :"[Video " + name + "]";
    }

    public String name() {
        return name;
    }

    public MediaSource source() {
        return source;
    }
}
