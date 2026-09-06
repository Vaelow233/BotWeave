package org.vaelow233.botweave.connector.qq.ob11.capability;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.vaelow233.botweave.api.bot.BotId;
import org.vaelow233.botweave.api.bot.BotNetwork;
import org.vaelow233.botweave.api.capability.GroupModeration;
import org.vaelow233.botweave.api.conversation.ConversationKind;
import org.vaelow233.botweave.api.conversation.ConversationRef;
import org.vaelow233.botweave.api.user.UserRef;
import org.vaelow233.botweave.connector.qq.ob11.session.OneBotSession;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

public class OneBotGroupModeration implements GroupModeration {
    private final BotId botId;
    /**
     * Accept the action and params (body), returns a completion stage
     */
    private final BiFunction<String, ObjectNode, CompletionStage<OneBotSession.Reply>> caller;

    public OneBotGroupModeration(BotId botId, BiFunction<String, ObjectNode, CompletionStage<OneBotSession.Reply>> caller) {
        this.botId = botId;
        this.caller = caller;
    }

    @Override
    public CompletionStage<Void> kick(ConversationRef conversation, UserRef user) {
        try {
            if (!BotNetwork.QQ_ONEBOT_11.equals(conversation.network()) || !BotNetwork.QQ_ONEBOT_11.equals(user.network())) {
                throw new IllegalArgumentException("Conversation or user belongs to another network");
            }
            if (!ConversationKind.GROUP.equals(conversation.kind())) {
                throw new IllegalArgumentException("The capability is supported only in group conversations");
            }
            long conversationId = Long.parseLong(conversation.id().value());
            if (conversationId <= 0) {
                throw new IllegalArgumentException("Invalid conversation ID");
            }
            long userId = Long.parseLong(user.id().value());
            if (userId <= 0) {
                throw new IllegalArgumentException("Invalid user ID");
            }
            ObjectNode params = JsonNodeFactory.instance.objectNode();
            params.put("group_id", conversationId);
            params.put("user_id", userId);
            return caller.apply("set_group_kick", params).thenApply(reply -> null);
        } catch (RuntimeException e) {
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }

    @Override
    public CompletionStage<Void> mute(ConversationRef conversation, UserRef user, Duration duration) {
        try {
            if (!BotNetwork.QQ_ONEBOT_11.equals(conversation.network()) || !BotNetwork.QQ_ONEBOT_11.equals(user.network())) {
                throw new IllegalArgumentException("Conversation or user belongs to another network");
            }
            if (!ConversationKind.GROUP.equals(conversation.kind())) {
                throw new IllegalArgumentException("The capability is supported only in group conversations");
            }
            long conversationId = Long.parseLong(conversation.id().value());
            if (conversationId <= 0) {
                throw new IllegalArgumentException("Invalid conversation ID");
            }
            long userId = Long.parseLong(user.id().value());
            if (userId <= 0) {
                throw new IllegalArgumentException("Invalid user ID");
            }
            if (duration.isNegative() || duration.isZero() || duration.getNano() != 0) {
                throw new IllegalArgumentException("Duration must be positive and non-zero");
            }
            ObjectNode params = JsonNodeFactory.instance.objectNode();
            params.put("group_id", conversationId);
            params.put("user_id", userId);
            params.put("duration", duration.getSeconds());
            return caller.apply("set_group_ban", params).thenApply(reply -> null);
        } catch (RuntimeException e) {
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }

    @Override
    public CompletionStage<Void> unmute(ConversationRef conversation, UserRef user) {
        try {
            if (!BotNetwork.QQ_ONEBOT_11.equals(conversation.network()) || !BotNetwork.QQ_ONEBOT_11.equals(user.network())) {
                throw new IllegalArgumentException("Conversation or user belongs to another network");
            }
            if (!ConversationKind.GROUP.equals(conversation.kind())) {
                throw new IllegalArgumentException("The capability is supported only in group conversations");
            }
            long conversationId = Long.parseLong(conversation.id().value());
            if (conversationId <= 0) {
                throw new IllegalArgumentException("Invalid conversation ID");
            }
            long userId = Long.parseLong(user.id().value());
            if (userId <= 0) {
                throw new IllegalArgumentException("Invalid user ID");
            }
            ObjectNode params = JsonNodeFactory.instance.objectNode();
            params.put("group_id", conversationId);
            params.put("user_id", userId);
            params.put("duration", 0);
            return caller.apply("set_group_ban", params).thenApply(reply -> null);
        } catch (RuntimeException e) {
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }

    @Override
    public CompletionStage<Void> muteGlobally(ConversationRef conversation) {
        try {
            if (!BotNetwork.QQ_ONEBOT_11.equals(conversation.network())) {
                throw new IllegalArgumentException("Conversation belongs to another network");
            }
            if (!ConversationKind.GROUP.equals(conversation.kind())) {
                throw new IllegalArgumentException("The capability is supported only in group conversations");
            }
            long conversationId = Long.parseLong(conversation.id().value());
            if (conversationId <= 0) {
                throw new IllegalArgumentException("Invalid conversation ID");
            }
            ObjectNode params = JsonNodeFactory.instance.objectNode();
            params.put("group_id", conversationId);
            params.put("enable", true);
            return caller.apply("set_group_whole_ban", params).thenApply(reply -> null);
        } catch (RuntimeException e) {
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }

    @Override
    public CompletionStage<Void> unmuteGlobally(ConversationRef conversation) {
        try {
            if (!BotNetwork.QQ_ONEBOT_11.equals(conversation.network())) {
                throw new IllegalArgumentException("Conversation belongs to another network");
            }
            if (!ConversationKind.GROUP.equals(conversation.kind())) {
                throw new IllegalArgumentException("The capability is supported only in group conversations");
            }
            long conversationId = Long.parseLong(conversation.id().value());
            if (conversationId <= 0) {
                throw new IllegalArgumentException("Invalid conversation ID");
            }
            ObjectNode params = JsonNodeFactory.instance.objectNode();
            params.put("group_id", conversationId);
            params.put("enable", false);
            return caller.apply("set_group_whole_ban", params).thenApply(reply -> null);
        } catch (RuntimeException e) {
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }

    @Override
    public CompletionStage<Void> rename(ConversationRef conversation, String name) {
        try {
            if (!BotNetwork.QQ_ONEBOT_11.equals(conversation.network())) {
                throw new IllegalArgumentException("Conversation belongs to another network");
            }
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Cannot set an empty group name");
            }
            if (!ConversationKind.GROUP.equals(conversation.kind())) {
                throw new IllegalArgumentException("The capability is supported only in group conversations");
            }
            long conversationId = Long.parseLong(conversation.id().value());
            if (conversationId <= 0) {
                throw new IllegalArgumentException("Invalid conversation ID");
            }
            ObjectNode params = JsonNodeFactory.instance.objectNode();
            params.put("group_id", conversationId);
            params.put("group_name", name);
            return caller.apply("set_group_name", params).thenApply(reply -> null);
        } catch (RuntimeException e) {
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }
}
