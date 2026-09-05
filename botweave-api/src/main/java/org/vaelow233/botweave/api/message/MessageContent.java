package org.vaelow233.botweave.api.message;

import org.vaelow233.botweave.api.message.element.MessageElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MessageContent {
    private final List<MessageElement> elements;
    public MessageContent(List<MessageElement> elements) {
        List<MessageElement> copy = new ArrayList<>(elements.size());
        copy.addAll(elements);
        this.elements = Collections.unmodifiableList(copy);
    }

    public List<MessageElement> elements() {
        return elements;
    }

    public static MessageContent of(MessageElement... elements) {
        return new MessageContent(Arrays.asList(elements));
    }
}
