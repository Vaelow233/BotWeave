package org.vaelow233.botweave.connector.qq.ob11.capability;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.vaelow233.botweave.api.bot.BotId;
import org.vaelow233.botweave.api.bot.BotNetwork;
import org.vaelow233.botweave.api.capability.GroupQuery;
import org.vaelow233.botweave.api.conversation.ConversationKind;
import org.vaelow233.botweave.api.conversation.ConversationRef;
import org.vaelow233.botweave.api.conversation.group.GroupProfile;
import org.vaelow233.botweave.connector.qq.ob11.codec.OneBotCodec;
import org.vaelow233.botweave.connector.qq.ob11.session.OneBotSession;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

public class OneBotGroupQuery implements GroupQuery {
    private final BotId botId;
    /**
     * Accept the action and params (body), returns a completion stage
     */
    private final BiFunction<String, ObjectNode, CompletionStage<OneBotSession.Reply>> caller;

    public OneBotGroupQuery(BotId botId, BiFunction<String, ObjectNode, CompletionStage<OneBotSession.Reply>> caller) {
        this.botId = botId;
        this.caller = caller;
    }

    @Override
    public CompletionStage<GroupProfile> getGroup(ConversationRef conversation) {
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
            return caller.apply("get_group_info", params).thenApply(reply -> OneBotCodec.decodeGroupProfile(reply.data()));
        } catch (RuntimeException e) {
            CompletableFuture<GroupProfile> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }

    @Override
    public CompletionStage<Set<GroupProfile>> getGroupList() {
        try {
            ObjectNode params = JsonNodeFactory.instance.objectNode();
            return caller.apply("get_group_list", params).thenApply(reply -> OneBotCodec.decodeGroupProfiles(reply.data()));
        } catch (RuntimeException e) {
            CompletableFuture<Set<GroupProfile>> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }
}
