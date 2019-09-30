package me.dags.chat;

import com.google.inject.Inject;
import me.dags.config.Config;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Plugin(id = "muchat")
public class MUChat {

    private static final Object lock = new Object();

    private final Path path;
    private Formatter formatter = new Formatter("", "", Format.EMPTY, Collections.emptyList());

    @Inject
    public MUChat(@ConfigDir(sharedRoot = false) Path dir) {
        this.path = dir.resolve("config.conf");
    }

    @Listener
    public void chat(MessageChannelEvent.Chat event, @Root Player player) {
        synchronized (lock) {
            formatter.applyChatFormatting(event, player);
        }
    }

    @Listener
    public void init(GameInitializationEvent event) {
        reload(null);
    }

    @Listener(order = Order.POST)
    public void join(ClientConnectionEvent.Join event, @Root Player player) {
        synchronized (lock) {
            formatter.applyTabListFormatting();
        }
    }

    @Listener
    public void reload(GameReloadEvent event) {
        Config config = Config.must(path);

        String header = config.node("format", "header").get("{$prefix} {$name}: ");
        String body = config.node("format", "body").get("{$message}");

        Format defaultFormat = new Format(
                config.node("formats", "default", "id").get("guest"),
                config.node("formats", "default", "prefix").get("[Guest](gray)"),
                config.node("formats", "default", "name").get("[{name}](gray)"),
                config.node("formats", "default", "tab").get("[{name}](gray)"),
                config.node("formats", "default", "message").get("{message}"),
                config.node("formats", "default", "priority").get(-1)
        );

        List<Format> formats = config.node("formats").childMap().values().stream()
                .map(node -> new Format(
                        node.get("id", ""),
                        node.get("prefix", ""),
                        node.get("name", ""),
                        node.get("tab", ""),
                        node.get("message", ""),
                        node.get("priority", 0)
                ))
                .collect(Collectors.toList());

        synchronized (lock) {
            formatter = new Formatter(header, body, defaultFormat, formats);
            formatter.applyTabListFormatting();
        }

        config.save();
    }
}
