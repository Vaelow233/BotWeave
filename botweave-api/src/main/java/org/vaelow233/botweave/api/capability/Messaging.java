package org.vaelow233.botweave.api.capability;

import org.vaelow233.botweave.api.conversation.ConversationRef;
import org.vaelow233.botweave.api.message.MessageContent;
import org.vaelow233.botweave.api.message.MessageId;
import org.vaelow233.botweave.api.message.MessageRef;
import org.vaelow233.botweave.api.message.SentMessage;
import org.vaelow233.botweave.api.message.element.MessageElement;

import java.util.Set;
import java.util.concurrent.CompletionStage;

public interface Messaging extends Capability {
    CompletionStage<SentMessage> send(ConversationRef conversation, MessageContent content);
    CompletionStage<Void> recall(MessageId messageId);
    Set<String> supportedElementTypes(ConversationRef conversation);

    default String type() {
        return "messaging";
    }
}
