package org.vaelow233.botweave.api.capability;

import org.vaelow233.botweave.api.conversation.ConversationRef;
import org.vaelow233.botweave.api.conversation.group.GroupProfile;

import java.util.Set;
import java.util.concurrent.CompletionStage;

public interface GroupQuery extends Capability {
    CompletionStage<GroupProfile> getGroup(ConversationRef conversation);
    CompletionStage<Set<GroupProfile>> getGroupList();

    default String type() {
        return "group-query";
    }
}
