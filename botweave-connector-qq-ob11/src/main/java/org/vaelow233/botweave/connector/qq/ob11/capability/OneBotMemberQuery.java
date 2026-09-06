package org.vaelow233.botweave.connector.qq.ob11.capability;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.vaelow233.botweave.api.bot.BotId;
import org.vaelow233.botweave.api.bot.BotNetwork;
import org.vaelow233.botweave.api.capability.MemberQuery;
import org.vaelow233.botweave.api.conversation.ConversationKind;
import org.vaelow233.botweave.api.conversation.ConversationRef;
import org.vaelow233.botweave.api.conversation.group.MemberProfile;
import org.vaelow233.botweave.api.user.UserRef;
import org.vaelow233.botweave.connector.qq.ob11.codec.OneBotCodec;
import org.vaelow233.botweave.connector.qq.ob11.session.OneBotSession;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

public class OneBotMemberQuery implements MemberQuery {
    private final BotId botId;
    /**
     * Accept the action and params (body), returns a completion stage
     */
    private final BiFunction<String, ObjectNode, CompletionStage<OneBotSession.Reply>> caller;

    public OneBotMemberQuery(BotId botId, BiFunction<String, ObjectNode, CompletionStage<OneBotSession.Reply>> caller) {
        this.botId = botId;
        this.caller = caller;
    }

    @Override
    public CompletionStage<MemberProfile> getMember(ConversationRef conversation, UserRef user) {
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
            return caller.apply("get_group_member_info", params).thenApply(reply -> OneBotCodec.decodeMemberProfile(reply.data()));
        } catch (RuntimeException e) {
            CompletableFuture<MemberProfile> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }

    @Override
    public CompletionStage<Set<MemberProfile>> getMemberList(ConversationRef conversation) {
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
            return caller.apply("get_group_member_list", params).thenApply(reply -> OneBotCodec.decodeMemberProfiles(reply.data()));
        } catch (RuntimeException e) {
            CompletableFuture<Set<MemberProfile>> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }
}
