package me.dags.chat;

import me.dags.textmu.MarkupSpec;
import me.dags.textmu.MarkupTemplate;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.service.permission.Subject;

/**
 * @author dags <dags@dags.me>
 */
class ChatOptions {

    static final String PREFIX = "PREFIX";
    static final String NAME = "NAME";
    static final String CHAT = "CHAT";

    private final MarkupTemplate prefix;
    private final MarkupTemplate name;
    private final String chat;
    private final String permission;
    private final int priority;

    ChatOptions(String id, ConfigurationNode node) {
        MarkupSpec spec = MarkupSpec.create();
        this.permission = "chatmd.format." + id.toLowerCase();
        this.priority = node.getNode("priority").getInt(-1);
        this.prefix = spec.template(node.getNode("prefix").getString("[gray](`[Guest]`)"));
        this.name = spec.template(node.getNode("name").getString("[gray]({.})"));
        this.chat = node.getNode("chat").getString("{.}");
    }

    String getChat() {
        return chat;
    }

    ChatOptions highestPriority(ChatOptions other) {
        return other.priority > this.priority ? other : this;
    }

    boolean applicableTo(Subject subject) {
        return subject.hasPermission(permission);
    }

    MarkupTemplate.Applier apply(MarkupTemplate template) {
        return template.with(ChatOptions.PREFIX, prefix).with(ChatOptions.NAME, name);
    }
}
