package org.vaelow233.botweave.connector.qq.ob11.capability;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.vaelow233.botweave.api.bot.BotId;
import org.vaelow233.botweave.api.bot.BotNetwork;
import org.vaelow233.botweave.api.capability.UserQuery;
import org.vaelow233.botweave.api.user.UserProfile;
import org.vaelow233.botweave.api.user.UserRef;
import org.vaelow233.botweave.connector.qq.ob11.codec.OneBotCodec;
import org.vaelow233.botweave.connector.qq.ob11.session.OneBotSession;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

public class OneBotUserQuery implements UserQuery {
    private final BotId botId;
    /**
     * Accept the action and params (body), returns a completion stage
     */
    private final BiFunction<String, ObjectNode, CompletionStage<OneBotSession.Reply>> caller;

    public OneBotUserQuery(BotId botId, BiFunction<String, ObjectNode, CompletionStage<OneBotSession.Reply>> caller) {
        this.botId = botId;
        this.caller = caller;
    }

    @Override
    public CompletionStage<UserProfile> getUser(UserRef user) {
        try {
            if (!BotNetwork.QQ_ONEBOT_11.equals(user.network())) {
                throw new IllegalArgumentException("User belongs to another network");
            }
            long userId = Long.parseLong(user.id().value());
            if (userId <= 0) {
                throw new IllegalArgumentException("Invalid user ID");
            }
            ObjectNode params = JsonNodeFactory.instance.objectNode();
            params.put("user_id", userId);
            return caller.apply("get_stranger_info", params).thenApply(reply -> OneBotCodec.decodeUserProfile(reply.data()));
        } catch (RuntimeException e) {
            CompletableFuture<UserProfile> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }
}
