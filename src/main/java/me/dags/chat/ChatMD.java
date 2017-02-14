package me.dags.chat;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import me.dags.spongemd.MarkdownSpec;
import me.dags.spongemd.MarkdownTemplate;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author dags <dags@dags.me>
 */
@Plugin(id = "chatmd", name = "ChatMD", version = "1.0", description = ".")
public class ChatMD {

    private static final String DEFAULT_NAME = "guest";
    private static final String DEFAULT_HEADER = "[gray](`[Guest]` {name}): ";
    private static final String DEFAULT_BODY = "{message}";

    private final Path configDir;
    private ChatListener chatListener;

    @Inject
    public ChatMD(@ConfigDir(sharedRoot = false) Path configDir) {
        this.configDir = configDir;
        this.chatListener = loadChatListener();
        ChatMD.write(chatListener.defaultFormat, configDir.resolve("default.conf"));
    }

    @Listener
    public void init(GameInitializationEvent event) {
        CommandSpec reload = CommandSpec.builder().permission("chat.command.reload").executor((src, args) -> {
            src.sendMessage(Text.of("Reloading chat formats..."));
            Sponge.getEventManager().unregisterListeners(chatListener);
            chatListener = loadChatListener();
            Sponge.getEventManager().registerListeners(ChatMD.this, chatListener);
            return CommandResult.success();
        }).build();

        CommandSpec chat = CommandSpec.builder().child(reload, "reload").permission("chat.command").build();

        Sponge.getCommandManager().register(this, chat, "format");
        Sponge.getEventManager().registerListeners(this, chatListener);
    }

    public final static class ChatListener {

        private final ChatFormat defaultFormat;
        private final List<ChatFormat> formats;

        private ChatListener(ChatFormat defaultFormat, Iterable<ChatFormat> formats) {
            this.defaultFormat = defaultFormat;
            this.formats = ImmutableList.copyOf(formats);
        }

        @Listener(order = Order.FIRST)
        public void onChat(MessageChannelEvent.Chat event, @Root Player player) {
            ChatFormat format = defaultFormat;
            for (ChatFormat chatFormat : formats) {
                if (chatFormat.applicableTo(player)) {
                    format = format.highestPriority(chatFormat);
                }
            }
            format.apply(event, player);
        }
    }

    private final static class ChatFormat {

        private static final ChatFormat DEFAULT = new ChatFormat(DEFAULT_HEADER, DEFAULT_BODY, "guest", -1);

        private final MarkdownTemplate header;
        private final MarkdownTemplate body;
        private final String permission;
        private final int priority;
        private final String name;

        private ChatFormat(String header, String body, String name, int priority) {
            this.header = MarkdownSpec.create().template(header);
            this.body = MarkdownSpec.create().template(body);
            this.permission = "chat.format." + name.toLowerCase();
            this.priority = priority;
            this.name = name;
        }

        private void apply(MessageChannelEvent.Chat event, Player player) {
            Optional<Text> displayName = player.get(Keys.DISPLAY_NAME);
            Object name = displayName.isPresent() ? displayName.get() : player.getName();
            Text message = MarkdownSpec.create(player).render(event.getRawMessage().toPlain());
            Text header = this.header.with("name", name).render();
            Text body = this.body.with("message", message).render();
            event.setMessage(header, body);
        }

        private ChatFormat highestPriority(ChatFormat other) {
            return other.priority > this.priority ? other : this;
        }

        private boolean applicableTo(Subject subject) {
            return subject.hasPermission(permission);
        }
    }

    private ChatListener loadChatListener() {
        ChatFormat defaultFormat = loadFormats(configDir.resolve("default.conf"));
        List<ChatFormat> formats = ImmutableList.copyOf(loadAll(configDir));
        return new ChatListener(defaultFormat, formats);
    }

    private static List<ChatFormat> loadAll(Path dir) {
        try {
            Iterator<Path> iterator = Files.newDirectoryStream(dir).iterator();
            List<ChatFormat> list = new ArrayList<>();
            while (iterator.hasNext()) {
                ChatFormat format = loadFormats(iterator.next());
                list.add(format);
            }
            return list;
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    private static ChatFormat loadFormats(Path path) {
        if (Files.exists(path)) {
            try {
                ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(path).build();
                ConfigurationNode node = loader.load();
                String name = node.getNode("name").getString(DEFAULT_NAME);
                String header = node.getNode("header").getString(DEFAULT_HEADER);
                String body = node.getNode("body").getString(DEFAULT_BODY);
                int priority = node.getNode("priority").getInt(-1);
                return new ChatFormat(header, body, name, priority);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ChatFormat.DEFAULT;
    }

    private static void write(ChatFormat format, Path path) {
        try {
            Files.createDirectories(path.getParent());
            ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(path).build();
            ConfigurationNode node = loader.createEmptyNode();
            node.getNode("name").setValue(format.name);
            node.getNode("header").setValue(format.header.toString());
            node.getNode("body").setValue(format.body.toString());
            node.getNode("priority").setValue(format.priority);
            loader.save(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
