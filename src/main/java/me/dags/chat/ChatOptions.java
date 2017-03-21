package me.dags.chat;

import me.dags.spongemd.MarkdownSpec;
import me.dags.spongemd.MarkdownTemplate;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.service.permission.Subject;

/**
 * @author dags <dags@dags.me>
 */
class ChatOptions {

    static final String PREFIX = "PREFIX";
    static final String NAME = "NAME";
    static final String MESSAGE = "CHAT";

    private final MarkdownTemplate prefix;
    private final MarkdownTemplate name;
    private final MarkdownTemplate chat;
    private final String permission;
    private final int priority;

    ChatOptions(String id, ConfigurationNode node) {
        MarkdownSpec spec = MarkdownSpec.create();
        this.permission = "chatmd.format." + id.toLowerCase();
        this.priority = ChatMD.getOrInsert(node, "priority", -1);
        this.prefix = spec.template(ChatMD.getOrInsert(node, "prefix", "[gray](`[Guest]`)"));
        this.name = spec.template(ChatMD.getOrInsert(node, "name", "[gray]({.})"));
        this.chat = spec.template(ChatMD.getOrInsert(node, "chat", "{.}"));
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
