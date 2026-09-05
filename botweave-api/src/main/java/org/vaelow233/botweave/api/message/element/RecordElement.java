package org.vaelow233.botweave.api.message.element;

import org.vaelow233.botweave.api.message.resource.MediaSource;

public class RecordElement implements MessageElement {
    private final String name;
    private final MediaSource source;
    public RecordElement(String name, MediaSource source) {
        this.name = name;
        this.source = source;
    }

    @Override
    public String type() {
        return "record";
    }

    @Override
    public String plainString() {
        return name == null ? "[Record]" : "[Record " + name + "]";
    }

    public String name() {
        return name;
    }

    public MediaSource source() {
        return source;
    }
}
