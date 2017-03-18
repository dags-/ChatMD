package me.dags.chat;

import com.google.common.collect.ImmutableList;
import me.dags.spongemd.MarkdownSpec;
import me.dags.spongemd.MarkdownTemplate;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectData;

import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public final class MessageListener {

    private final MarkdownTemplate bodyFormat;
    private final MarkdownTemplate headerFormat;
    private final ChatOptions defaultOptions;
    private final ImmutableList<ChatOptions> options;

    MessageListener(ChatOptions defaultOptions, List<ChatOptions> options, String header, String body) {
        MarkdownSpec spec = MarkdownSpec.create();
        this.options = ImmutableList.copyOf(options);
        this.defaultOptions = defaultOptions;
        this.headerFormat = spec.template(header);
        this.bodyFormat = spec.template(body);
    }

    @Listener(order = Order.FIRST)
    public void onChat(MessageChannelEvent.Chat event, @Root Player player) {
        ChatOptions options = getOptions(player);

        MarkdownTemplate.Applier header = options.apply(headerFormat).withOptions(player, SubjectData.GLOBAL_CONTEXT).inherit(event.getFormatter().getHeader());
        MarkdownTemplate.Applier body = options.apply(bodyFormat).withOptions(player, SubjectData.GLOBAL_CONTEXT).inherit(event.getFormatter().getBody());

        event.getFormatter().getHeader().set(0, header);
        event.getFormatter().getBody().set(0, body);
    }

    private ChatOptions getOptions(Subject subject) {
        ChatOptions applicable = defaultOptions;
        for (ChatOptions option : options) {
            if (option.applicableTo(subject)) {
                applicable = option.highestPriority(applicable);
            }
        }
        return applicable;
    }
}
