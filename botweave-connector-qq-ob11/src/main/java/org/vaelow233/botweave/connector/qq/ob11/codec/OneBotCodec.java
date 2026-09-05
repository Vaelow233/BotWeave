package org.vaelow233.botweave.connector.qq.ob11.codec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.vaelow233.botweave.api.bot.Bot;
import org.vaelow233.botweave.api.bot.BotNetwork;
import org.vaelow233.botweave.api.conversation.ConversationId;
import org.vaelow233.botweave.api.conversation.ConversationKind;
import org.vaelow233.botweave.api.conversation.ConversationRef;
import org.vaelow233.botweave.api.event.MessageReceivedEvent;
import org.vaelow233.botweave.api.exception.UnsupportedMessageElementException;
import org.vaelow233.botweave.api.message.MessageContent;
import org.vaelow233.botweave.api.message.MessageId;
import org.vaelow233.botweave.api.message.MessageRef;
import org.vaelow233.botweave.api.message.element.MentionElement;
import org.vaelow233.botweave.api.message.element.MessageElement;
import org.vaelow233.botweave.api.message.element.TextElement;
import org.vaelow233.botweave.api.user.UserId;
import org.vaelow233.botweave.api.user.UserRef;
import org.vaelow233.botweave.connector.qq.ob11.impl.OneBotConversation;
import org.vaelow233.botweave.connector.qq.ob11.impl.OneBotMessage;
import org.vaelow233.botweave.connector.qq.ob11.impl.OneBotUser;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OneBotCodec {
    private static final JsonNodeFactory JSON = JsonNodeFactory.instance;

    private OneBotCodec() {

    }

    public static ArrayNode encode(ConversationRef conversation, MessageContent content) {
        ArrayNode result = JSON.arrayNode();
        for (MessageElement element : content.elements()) {
            ObjectNode segment = JSON.objectNode();
            ObjectNode data = JSON.objectNode();
            if (element instanceof TextElement) {
                segment.put("type", "text");
                data.put("text", ((TextElement) element).text());
            } else if (element instanceof MentionElement) {
                if (!ConversationKind.GROUP.equals(conversation.kind())) {
                    throw new UnsupportedMessageElementException("Mention is only supported in group conversations");
                }
                UserRef user = ((MentionElement) element).user();
                if (!BotNetwork.QQ_ONEBOT_11.equals(user.network())) {
                    throw new UnsupportedMessageElementException("Cannot mention a user from another network");
                }
                long qq = Long.parseLong(user.id().value());
                if (qq <= 0) {
                    throw new IllegalArgumentException("Invalid QQ number");
                }
                segment.put("type", "at");
                data.put("qq", Long.toString(qq));
            } else {
                throw new UnsupportedMessageElementException("Unsupported element: " + element.type());
            }
            segment.set("data", data);
            result.add(segment);
        }
        return result;
    }

    public static MessageContent decodeContent(JsonNode message) {
        if (!message.isArray()) {
            throw new IllegalArgumentException("Expected message array; configure messagePostFormat=array");
        }
        List<MessageElement> elements = new ArrayList<>();
        for (JsonNode segment : message) {
            String type = segment.path("type").asText();
            JsonNode data = segment.path("data");
            if ("text".equals(type)) {
                JsonNode text = data.get("text");
                if (text == null || !text.isTextual()) {
                    throw new IllegalArgumentException("Invalid text segment");
                }
                elements.add(new TextElement(text.textValue()));
            } else if ("at".equals(type) && !"all".equals(data.path("qq").asText())) {
                elements.add(new MentionElement(new OneBotUser(new UserId(id(data, "qq")))));
            } else {
                elements.add(new RawElement(segment));
            }
        }
        return new MessageContent(elements);
    }

    public static Optional<MessageReceivedEvent> decodeEvent(Bot bot, JsonNode event) {
        if (!"message".equals(event.path("post_type").asText())) {
            return Optional.empty();
        }
        String messageType = event.path("message_type").asText();
        String subType = event.path("sub_type").asText();
        ConversationRef conversation;
        if ("private".equals(messageType)) {
            if (!"friend".equals(subType)) {
                return Optional.empty();
            }
            conversation = new OneBotConversation(new ConversationId(id(event, "user_id")), ConversationKind.PRIVATE);
        } else if ("group".equals(messageType)) {
            if (!"normal".equals(subType)) {
                return Optional.empty();
            }
            conversation = new OneBotConversation(new ConversationId(id(event, "group_id")), ConversationKind.GROUP);
        } else {
            return Optional.empty();
        }
        Instant timestamp = Instant.ofEpochSecond(Long.parseLong(id(event, "time")));
        MessageRef message = new OneBotMessage(
                new MessageId(id(event, "message_id")),
                new OneBotUser(new UserId(id(event, "user_id"))),
                conversation,
                decodeContent(event.path("message")),
                timestamp
        );
        return Optional.of(new MessageReceivedEvent(bot, message, timestamp));
    }

    public static String id(JsonNode object, String field) {
        JsonNode value = object.get(field);
        if (value == null || !(value.isIntegralNumber() || value.isTextual())) {
            throw new IllegalArgumentException("Invalid field: " + field);
        }
        return Long.toString(Long.parseLong(value.asText()));
    }

    /**
     * Unsupported element type will be represented by this class
     */
    public static final class RawElement implements MessageElement {
        private final JsonNode segment;

        public RawElement(JsonNode segment) {
            this.segment = segment.deepCopy();
        }

        public JsonNode segment() {
            return segment.deepCopy();
        }

        @Override
        public String type() {
            return "onebot11:" + segment.path("type").asText("unknown");
        }

        @Override
        public String plainString() {
            return "[" + type() + "]";
        }
    }
}
