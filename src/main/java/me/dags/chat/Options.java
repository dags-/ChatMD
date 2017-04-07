package me.dags.chat;

import me.dags.textmu.MarkupSpec;
import me.dags.textmu.MarkupTemplate;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;

/**
 * @author dags <dags@dags.me>
 */
class Options {

    static final String PREFIX = "PREFIX";
    static final String NAME = "NAME";
    static final String CHAT = "CHAT";

    private final MarkupTemplate prefix;
    private final MarkupTemplate name;
    private final MarkupTemplate tab;
    private final String chat;
    private final String permission;
    private final int priority;

    Options(String id, ConfigurationNode node) {
        MarkupSpec spec = MarkupSpec.create();
        this.permission = "muchat.format." + id.toLowerCase();
        this.priority = node.getNode("priority").getInt(-1);
        this.prefix = spec.template(node.getNode("prefix").getString("[gray](`[Guest]`)"));
        this.name = spec.template(node.getNode("name").getString("[gray]({.})"));
        this.tab = spec.template(node.getNode("tab").getString("[gray]({name})"));
        this.chat = node.getNode("chat").getString("{.}");
    }

    String getChat() {
        return chat;
    }

    boolean applicableTo(Subject subject) {
        return subject.hasPermission(permission);
    }

    Text formatTabName(Player player) {
        return tab.with("name", player.getName()).render();
    }

    Options highestPriority(Options other) {
        return other.priority > this.priority ? other : this;
    }

    MarkupTemplate.Applier apply(MarkupTemplate template) {
        return template.with(Options.PREFIX, prefix).with(Options.NAME, name);
    }
}
