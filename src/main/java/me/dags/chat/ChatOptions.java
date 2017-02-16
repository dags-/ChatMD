package me.dags.chat;

import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectData;

/**
 * @author dags <dags@dags.me>
 */
class ChatOptions {

    static final String PREFIX = "md_prefix";
    static final String NAME = "md_name";
    static final String MESSAGE = "md_message";

    private final String permission;
    private final String prefix;
    private final String name;
    private final String chat;
    private final int priority;

    ChatOptions(String id, ConfigurationNode node) {
        this.permission = "chatmd.format." + id.toLowerCase();
        this.priority = ChatMD.getOrInsert(node, "priority", -1);
        this.prefix = ChatMD.getOrInsert(node, "prefix", "[gray](`[Guest]`)");
        this.name = ChatMD.getOrInsert(node, "name", "[gray]({.})");
        this.chat = ChatMD.getOrInsert(node, "chat", "{.}");
    }

    ChatOptions highestPriority(ChatOptions other) {
        return other.priority > this.priority ? other : this;
    }

    boolean applicableTo(Subject subject) {
        return subject.hasPermission(permission);
    }

    void apply(Subject subject) {
        SubjectData data = subject.getTransientSubjectData();
        data.setOption(SubjectData.GLOBAL_CONTEXT, ChatOptions.PREFIX, prefix);
        data.setOption(SubjectData.GLOBAL_CONTEXT, ChatOptions.NAME, name);
        data.setOption(SubjectData.GLOBAL_CONTEXT, ChatOptions.MESSAGE, chat);
    }
}
