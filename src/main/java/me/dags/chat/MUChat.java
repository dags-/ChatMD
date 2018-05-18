package me.dags.chat;

import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author dags <dags@dags.me>
 */
@SuppressWarnings("WeakerAccess")
@Plugin(id = "muchat", name = "MUChat", version = "1.1", description = ".")
public class MUChat {

    private static final String HEADER_FORMAT = String.format("{prefix:%s} {header:%s}: ", Options.PREFIX, Options.NAME);
    private static final String BODY_FORMAT = String.format("{body:%s}", Options.CHAT);

    private final ConfigurationLoader<CommentedConfigurationNode> loader;
    private volatile Formatter formatter;

    @Inject
    public MUChat(@DefaultConfig(sharedRoot = false) ConfigurationLoader<CommentedConfigurationNode> loader) {
        this.loader = loader;
    }

    @Listener
    public void init(GameInitializationEvent event) {
        reload(null);
        Task.builder().execute(this::syncAll).delay(30, TimeUnit.MINUTES).interval(30, TimeUnit.MINUTES).submit(this);
    }

    @Listener
    public void reload(GameReloadEvent event) {
        CommentedConfigurationNode config = loadConfig();
        ConfigurationNode formatNode = config.getNode("format");
        ConfigurationNode optionsNode = config.getNode("options");

        String body = formatNode.getNode("body").getString(BODY_FORMAT);
        String header = formatNode.getNode("header").getString(HEADER_FORMAT);
        Options defaultOptions = new Options("default", optionsNode.getNode("default"));
        List<Options> options = new ArrayList<>();

        for (Map.Entry<?, ? extends ConfigurationNode> child : optionsNode.getChildrenMap().entrySet()) {
            String id = child.getKey().toString();
            ConfigurationNode node = child.getValue();
            options.add(new Options(id, node));
        }

        if (formatter != null) {
            Sponge.getEventManager().unregisterListeners(formatter);
        }

        formatter = new Formatter(defaultOptions, options, header, body);
        Sponge.getEventManager().registerListeners(this, formatter);

        saveConfig(config);

        syncAll();
    }

    private void syncAll() {
        if (this.formatter != null) {
            final Formatter formatter = this.formatter;
            for (Player player : Sponge.getServer().getOnlinePlayers()) {
                formatter.syncTabs(player);
            }
        }
    }

    private CommentedConfigurationNode loadConfig() {
        try {
            return loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
        } catch (IOException e) {
            return loader.createEmptyNode();
        }
    }

    private void saveConfig(CommentedConfigurationNode config) {
        try {
            loader.save(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
