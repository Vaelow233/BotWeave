package org.vaelow233.botweave.api.capability;

import org.vaelow233.botweave.api.conversation.ConversationRef;
import org.vaelow233.botweave.api.user.UserRef;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

public interface GroupModeration extends Capability {
    CompletionStage<Void> kick(ConversationRef conversation, UserRef user);
    CompletionStage<Void> mute(ConversationRef conversation, UserRef user, Duration duration);
    CompletionStage<Void> unmute(ConversationRef conversation, UserRef user);
    CompletionStage<Void> muteGlobally(ConversationRef conversation);
    CompletionStage<Void> unmuteGlobally(ConversationRef conversation);
    CompletionStage<Void> rename(ConversationRef conversation, String name);

    @Override
    default String type() {
        return "group-moderation";
    }
}
