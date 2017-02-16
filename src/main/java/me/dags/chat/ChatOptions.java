package me.dags.chat;

import me.dags.spongemd.MarkdownTemplate;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.service.permission.Subject;

/**
 * @author dags <dags@dags.me>
 */
class ChatOptions {

    static final String PREFIX = "prefix";
    static final String NAME = "name";
    static final String MESSAGE = "message";

    private final String permission;
    private final int priority;
    private final String prefix;
    private final String name;
    private final String chat;

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

    MarkdownTemplate.Applier apply(MarkdownTemplate template) {
        return template.with(ChatOptions.PREFIX, prefix).with(ChatOptions.NAME, name).with(ChatOptions.MESSAGE, chat);
    }
}
