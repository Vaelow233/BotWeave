package org.vaelow233.botweave.api.capability;

import org.vaelow233.botweave.api.conversation.ConversationRef;
import org.vaelow233.botweave.api.conversation.group.MemberProfile;
import org.vaelow233.botweave.api.user.UserRef;

import java.util.Set;
import java.util.concurrent.CompletionStage;

public interface MemberQuery extends Capability {
    CompletionStage<MemberProfile> getMember(ConversationRef conversation, UserRef user);
    CompletionStage<Set<MemberProfile>> getMemberList(ConversationRef conversation);

    default String type() {
        return "member-query";
    }
}
