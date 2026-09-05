package org.vaelow233.botweave.api.message.element;

public class TextElement implements MessageElement {
    private final String text;
    public TextElement(String text) {
        this.text = text;
    }

    @Override
    public String type() {
        return "text";
    }

    @Override
    public String plainString() {
        return text;
    }

    public String text() {
        return text;
    }
}
