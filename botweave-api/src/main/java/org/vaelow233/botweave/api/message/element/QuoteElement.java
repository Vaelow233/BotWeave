package org.vaelow233.botweave.api.message.element;

import org.vaelow233.botweave.api.message.MessageId;

public class QuoteElement implements MessageElement {
    private final MessageId messageId;
    public QuoteElement(MessageId messageId) {
        this.messageId = messageId;
    }

    @Override
    public String type() {
        return "quote";
    }

    @Override
    public String plainString() {
        return "[Quote: " + messageId + "]";
    }

    public MessageId messageId() {
        return messageId;
    }
}
