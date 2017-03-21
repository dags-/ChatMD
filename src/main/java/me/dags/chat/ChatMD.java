package me.dags.chat;

import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
@SuppressWarnings("WeakerAccess")
@Plugin(id = "chatmd", name = "ChatMD", version = "1.0", description = ".")
public class ChatMD {

    private static final String HEADER_FORMAT = String.format("{:%s} {header:%s}: ", ChatOptions.PREFIX, ChatOptions.NAME);
    private static final String BODY_FORMAT = String.format("{body:%s}", ChatOptions.CHAT);

    private final ConfigurationLoader<CommentedConfigurationNode> loader;
    private MessageListener messageListener;

    @Inject
    public ChatMD(@DefaultConfig(sharedRoot = false) ConfigurationLoader<CommentedConfigurationNode> loader) {
        this.loader = loader;
    }

    @Listener
    public void init(GameInitializationEvent event) {
        reload(null);
    }

    @Listener
    public void reload(GameReloadEvent event) {
        CommentedConfigurationNode config = loadConfig();

        ConfigurationNode formatNode = config.getNode("format");
        String header = getOrInsert(formatNode, "header", HEADER_FORMAT);
        String body = getOrInsert(formatNode, "body", BODY_FORMAT);

        ConfigurationNode optionsNode = config.getNode("options");
        ChatOptions defaultOptions = new ChatOptions("default", optionsNode.getNode("default"));
        List<ChatOptions> options = new ArrayList<>();

        for (Map.Entry<?, ? extends ConfigurationNode> child : optionsNode.getChildrenMap().entrySet()) {
            String id = child.getKey().toString();
            ConfigurationNode node = child.getValue();
            options.add(new ChatOptions(id, node));
        }

        if (messageListener != null) {
            Sponge.getEventManager().unregisterListeners(messageListener);
        }

        messageListener = new MessageListener(defaultOptions, options, header, body);
        Sponge.getEventManager().registerListeners(this, messageListener);

        saveConfig(config);
    }

    private CommentedConfigurationNode loadConfig() {
        try {
            return loader.load();
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

    @SuppressWarnings("unchecked")
    static <T> T getOrInsert(ConfigurationNode parent, String key, T def) {
        ConfigurationNode node = parent.getNode(key);
        if (node.isVirtual()) {
            node.setValue(def);
            return def;
        }
        return (T) node.getValue();
    }
}
