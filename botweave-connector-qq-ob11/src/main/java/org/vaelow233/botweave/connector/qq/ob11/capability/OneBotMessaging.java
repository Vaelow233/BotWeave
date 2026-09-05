package org.vaelow233.botweave.connector.qq.ob11.capability;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.vaelow233.botweave.api.bot.BotId;
import org.vaelow233.botweave.api.bot.BotNetwork;
import org.vaelow233.botweave.api.capability.Messaging;
import org.vaelow233.botweave.api.conversation.ConversationKind;
import org.vaelow233.botweave.api.conversation.ConversationRef;
import org.vaelow233.botweave.api.exception.UnsupportedCapabilityException;
import org.vaelow233.botweave.api.message.MessageContent;
import org.vaelow233.botweave.api.message.MessageId;
import org.vaelow233.botweave.api.message.SentMessage;
import org.vaelow233.botweave.connector.qq.ob11.codec.OneBotCodec;
import org.vaelow233.botweave.connector.qq.ob11.session.OneBotSession;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

public class OneBotMessaging implements Messaging {
    private static final Set<String> PRIVATE_TYPES = Collections.singleton("text");
    private static final Set<String> GROUP_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("text", "mention")));
    private final BotId botId;
    /**
     * Accept the action and params (body), returns a completion stage
     */
    private final BiFunction<String, ObjectNode, CompletionStage<OneBotSession.Reply>> caller;

    public OneBotMessaging(BotId botId, BiFunction<String, ObjectNode, CompletionStage<OneBotSession.Reply>> caller) {
        this.botId = botId;
        this.caller = caller;
    }

    @Override
    public CompletionStage<SentMessage> send(ConversationRef conversation, MessageContent content) {
        try {
            // Check whether the conversation belongs to QQ_ONEBOT_11
            if (!BotNetwork.QQ_ONEBOT_11.equals(conversation.network())) {
                throw new IllegalArgumentException("Conversation belongs to another network");
            }
            // Check whether the message is empty
            if (content.elements().isEmpty()) {
                throw new IllegalArgumentException("Cannot send empty message");
            }
            // Check whether the conversation id is valid
            long target = Long.parseLong(conversation.id().value());
            if (target <= 0) {
                throw new IllegalArgumentException("Invalid target ID");
            }
            // Construct the request
            ObjectNode params = JsonNodeFactory.instance.objectNode();
            String action;
            if (ConversationKind.PRIVATE.equals(conversation.kind())) {
                action = "send_private_msg";
                params.put("user_id", target);
            } else if (ConversationKind.GROUP.equals(conversation.kind())) {
                action = "send_group_msg";
                params.put("group_id", target);
            } else {
                throw new UnsupportedCapabilityException("Unsupported conversation kind: " + conversation.kind());
            }
            params.set("message", OneBotCodec.encode(conversation, content));
            // Send the request
            return caller.apply(action, params).thenApply(reply -> new SentMessage(
                    botId,
                    conversation,
                    new MessageId(OneBotCodec.id(reply.data(), "message_id")),
                    reply.acknowledgedAt()
            ));
        } catch (RuntimeException e) {
            // Return the stage with the exception
            CompletableFuture<SentMessage> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }

    @Override
    public Set<String> supportedElementTypes(ConversationRef conversation) {
        if (!BotNetwork.QQ_ONEBOT_11.equals(conversation.network())) {
            return Collections.emptySet();
        }
        if (ConversationKind.PRIVATE.equals(conversation.kind())) {
            return PRIVATE_TYPES;
        }
        if (ConversationKind.GROUP.equals(conversation.kind())) {
            return GROUP_TYPES;
        }
        return Collections.emptySet();
    }
}
