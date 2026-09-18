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
import org.vaelow233.botweave.api.conversation.group.GroupProfile;
import org.vaelow233.botweave.api.conversation.group.MemberProfile;
import org.vaelow233.botweave.api.conversation.group.MemberRole;
import org.vaelow233.botweave.api.event.*;
import org.vaelow233.botweave.api.exception.UnsupportedMessageElementException;
import org.vaelow233.botweave.api.message.MessageContent;
import org.vaelow233.botweave.api.message.MessageId;
import org.vaelow233.botweave.api.message.MessageRef;
import org.vaelow233.botweave.api.message.element.*;
import org.vaelow233.botweave.api.message.resource.MediaSource;
import org.vaelow233.botweave.api.user.UserId;
import org.vaelow233.botweave.api.user.UserRef;
import org.vaelow233.botweave.connector.qq.ob11.impl.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

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
            } else if (element instanceof ImageElement) {
                ImageElement image = (ImageElement) element;
                segment.put("type", "image");
                data.put("file", encodeMediaSource(image.source()));
            } else if (element instanceof RecordElement) {
                RecordElement record = (RecordElement) element;
                segment.put("type", "record");
                data.put("file", encodeMediaSource(record.source()));
            } else if (element instanceof VideoElement) {
                VideoElement video = (VideoElement) element;
                segment.put("type", "video");
                data.put("file", encodeMediaSource(video.source()));
            } else if (element instanceof MentionAllElement) {
                if (!ConversationKind.GROUP.equals(conversation.kind())) {
                    throw new UnsupportedMessageElementException("Mention-all is only supported in group conversations");
                }
                segment.put("type", "at");
                data.put("qq", "all");
            } else if (element instanceof QuoteElement) {
                QuoteElement quote = (QuoteElement) element;
                int quotedId = Integer.parseInt(quote.messageId().value());
                segment.put("type", "reply");
                data.put("id", Integer.toString(quotedId));
            } else {
                throw new UnsupportedMessageElementException("Unsupported element: " + element.type());
            }
            segment.set("data", data);
            result.add(segment);
        }
        return result;
    }

    private static String encodeMediaSource(MediaSource source) {
        if (source instanceof MediaSource.Url) {
            MediaSource.Url url = (MediaSource.Url) source;
            return url.uri().toString();
        }
        if (source instanceof MediaSource.Bytes) {
            MediaSource.Bytes bytes = (MediaSource.Bytes) source;
            return "base64://" + Base64.getEncoder().encodeToString(bytes.value());
        }
        if (source instanceof MediaSource.File) {
            MediaSource.File file = (MediaSource.File) source;
            return file.uri().toString();
        }
        throw new UnsupportedMessageElementException("Unsupported media source: " + source.getClass().getName());
    }

    public static OneBotGroupProfile decodeGroupProfile(JsonNode node) {
        String groupId = id(node, "group_id");
        String groupName = node.get("group_name").asText();
        long memberCount = node.get("member_count").asLong();
        return new OneBotGroupProfile(
                new OneBotConversation(new ConversationId(groupId), ConversationKind.GROUP),
                groupName,
                memberCount
        );
    }

    public static Set<GroupProfile> decodeGroupProfiles(JsonNode node) {
        if (!node.isArray()) {
            throw new IllegalArgumentException("Expecting an array node of group profiles");
        }
        Set<GroupProfile> result = new HashSet<>();
        for (int i = 0; i < node.size(); i++) {
            result.add(decodeGroupProfile(node.get(i)));
        }
        return result;
    }

    private static Optional<MemberRole> decodeMemberRole(JsonNode node) {
        switch (node.path("role").asText("")) {
            case "owner": return Optional.of(MemberRole.OWNER);
            case "admin": return Optional.of(MemberRole.ADMIN);
            case "member": return Optional.of(MemberRole.MEMBER);
            default: return Optional.empty();
        }
    }

    private static Optional<MemberIncreaseEvent.SubType> decodeMemberIncreaseType(JsonNode node) {
        switch (node.path("sub_type").asText()) {
            case "approve": return Optional.of(MemberIncreaseEvent.SubType.APPROVE);
            case "invite": return Optional.of(MemberIncreaseEvent.SubType.INVITE);
            default: return Optional.empty();
        }
    }

    private static Optional<MemberDecreaseEvent.SubType> decodeMemberDecreaseType(JsonNode node) {
        switch (node.path("sub_type").asText()) {
            case "leave": return Optional.of(MemberDecreaseEvent.SubType.LEAVE);
            case "kick": return Optional.of(MemberDecreaseEvent.SubType.KICK);
            case "kick_me": return Optional.of(MemberDecreaseEvent.SubType.KICK_ME);
            default: return Optional.empty();
        }
    }

    public static OneBotMemberProfile decodeMemberProfile(JsonNode node) {
        String groupId = id(node, "group_id");
        String userId = id(node, "user_id");
        String name = node.get("nickname").asText();
        String card = node.get("card").asText();
        String displayName = card.isEmpty() ? name : card;
        return new OneBotMemberProfile(
                new OneBotConversation(new ConversationId(groupId), ConversationKind.GROUP),
                new OneBotUser(new UserId(userId)),
                name,
                displayName,
                decodeMemberRole(node)
        );
    }

    public static Set<MemberProfile> decodeMemberProfiles(JsonNode node) {
        if (!node.isArray()) {
            throw new IllegalArgumentException("Expecting an array node of group profiles");
        }
        Set<MemberProfile> result = new HashSet<>();
        for (int i = 0; i < node.size(); i++) {
            result.add(decodeMemberProfile(node.get(i)));
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
            } else if ("at".equals(type)) {
                elements.add(new MentionAllElement());
            } else if ("image".equals(type)) {
                JsonNode file = data.get("file");
                if (file == null || !file.isTextual()) {
                    throw new IllegalArgumentException("Invalid file name for image");
                }
                JsonNode url = data.get("url");
                if (url == null || !url.isTextual()) {
                    throw new IllegalArgumentException("Invalid file url for image");
                }
                elements.add(new ImageElement(file.textValue(), MediaSource.url(url.textValue())));
            } else if ("record".equals(type)) {
                JsonNode file = data.get("file");
                if (file == null || !file.isTextual()) {
                    throw new IllegalArgumentException("Invalid file name for record");
                }
                JsonNode url = data.get("url");
                if (url == null || !url.isTextual()) {
                    throw new IllegalArgumentException("Invalid file url for record");
                }
                elements.add(new RecordElement(file.textValue(), MediaSource.url(url.textValue())));
            } else if ("video".equals(type)) {
                JsonNode file = data.get("file");
                if (file == null || !file.isTextual()) {
                    throw new IllegalArgumentException("Invalid file name for video");
                }
                JsonNode url = data.get("url");
                if (url == null || !url.isTextual()) {
                    throw new IllegalArgumentException("Invalid file url for video");
                }
                elements.add(new VideoElement(file.textValue(), MediaSource.url(url.textValue())));
            } else if ("reply".equals(type)) {
                JsonNode id = data.get("id");
                if (id == null || !id.isTextual()) {
                    throw new IllegalArgumentException("Invalid quote id");
                }
                elements.add(new QuoteElement(new MessageId(id.textValue())));
            } else {
                elements.add(new RawElement(segment));
            }
        }
        return new MessageContent(elements);
    }

    public static Optional<? extends BotEvent> decodeEvent(Bot bot, JsonNode event) {
        if ("message".equals(event.path("post_type").asText())) {
            return decodeMessageEvent(bot, event);
        } else if ("notice".equals(event.path("post_type").asText())) {
            return decodeNoticeEvent(bot, event);
        }
        return Optional.empty();
    }

    public static Optional<? extends BotEvent> decodeNoticeEvent(Bot bot, JsonNode event) {
        String noticeType = event.path("notice_type").asText();
        Instant timestamp = Instant.ofEpochSecond(Long.parseLong(id(event, "time")));
        if ("group_decrease".equals(noticeType)) {
            return Optional.of(new MemberDecreaseEvent(
                    bot,
                    new OneBotConversation(
                            new ConversationId(id(event, "group_id")),
                            ConversationKind.GROUP
                    ),
                    new OneBotUser(new UserId(id(event, "operator_id"))),
                    new OneBotUser(new UserId(id(event, "user_id"))),
                    decodeMemberDecreaseType(event),
                    timestamp
            ));
        } else if ("group_increase".equals(noticeType)) {
            return Optional.of(new MemberIncreaseEvent(
                    bot,
                    new OneBotConversation(
                            new ConversationId(id(event, "group_id")),
                            ConversationKind.GROUP
                    ),
                    new OneBotUser(new UserId(id(event, "operator_id"))),
                    new OneBotUser(new UserId(id(event, "user_id"))),
                    decodeMemberIncreaseType(event),
                    timestamp
            ));
        } else if ("group_ban".equals(noticeType)) {
            return decodeMuteEvent(bot, event);
        } else if ("group_recall".equals(noticeType)) {
            return Optional.of(new MessageRecalledEvent(
                    bot,
                    new OneBotConversation(
                            new ConversationId(id(event, "group_id")),
                            ConversationKind.GROUP
                    ),
                    new MessageId(id(event, "message_id")),
                    new OneBotUser(new UserId(id(event, "operator_id"))),
                    new OneBotUser(new UserId(id(event, "user_id"))),
                    timestamp
            ));
        } else if ("friend_recall".equals(noticeType)) {
            return Optional.of(new MessageRecalledEvent(
                    bot,
                    new OneBotConversation(
                            new ConversationId(id(event, "user_id")),
                            ConversationKind.PRIVATE
                    ),
                    new MessageId(id(event, "message_id")),
                    new OneBotUser(new UserId(id(event, "user_id"))),
                    new OneBotUser(new UserId(id(event, "user_id"))),
                    timestamp
            ));
        }
        return Optional.empty();
    }

    public static Optional<? extends BotEvent> decodeMuteEvent(Bot bot, JsonNode event) {
        Instant timestamp = Instant.ofEpochSecond(Long.parseLong(id(event, "time")));
        if ("ban".equals(event.path("sub_type").asText())) {
            if ("0".equals(id(event, "user_id"))) {
                return Optional.of(new GroupGlobalMuteEvent(
                        bot,
                        new OneBotConversation(
                                new ConversationId(id(event, "group_id")),
                                ConversationKind.GROUP
                        ),
                        new OneBotUser(new UserId(id(event, "operator_id"))),
                        Duration.ZERO,
                        timestamp
                ));
            }
            return Optional.of(new MemberMuteEvent(
                    bot,
                    new OneBotConversation(
                            new ConversationId(id(event, "group_id")),
                            ConversationKind.GROUP
                    ),
                    new OneBotUser(new UserId(id(event, "operator_id"))),
                    new OneBotUser(new UserId(id(event, "user_id"))),
                    Duration.ofSeconds(event.get("duration").asLong()),
                    timestamp
            ));
        } else if ("lift_ban".equals(event.path("sub_type").asText())) {
            if ("0".equals(id(event, "user_id"))) {
                return Optional.of(new GroupGlobalUnmuteEvent(
                        bot,
                        new OneBotConversation(
                                new ConversationId(id(event, "group_id")),
                                ConversationKind.GROUP
                        ),
                        new OneBotUser(new UserId(id(event, "operator_id"))),
                        timestamp
                ));
            }
            return Optional.of(new MemberUnmuteEvent(
                    bot,
                    new OneBotConversation(
                            new ConversationId(id(event, "group_id")),
                            ConversationKind.GROUP
                    ),
                    new OneBotUser(new UserId(id(event, "operator_id"))),
                    new OneBotUser(new UserId(id(event, "user_id"))),
                    timestamp
            ));
        }
        return Optional.empty();
    }

    public static Optional<MessageReceivedEvent> decodeMessageEvent(Bot bot, JsonNode event) {
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
