package me.dags.chat;

import me.dags.spongemd.MarkdownSpec;
import me.dags.spongemd.MarkdownTemplate;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.service.permission.SubjectData;

/**
 * @author dags <dags@dags.me>
 */
public final class MessageListener {

    private final String headerFormat;
    private final String bodyFormat;
    private final MarkdownSpec global;

    MessageListener(String header, String body) {
        this.headerFormat = header;
        this.bodyFormat = body;
        this.global = MarkdownSpec.create();
    }

    @Listener(order = Order.FIRST)
    public void onChat(MessageChannelEvent.Chat event, @Root CommandSource source) {
        MarkdownTemplate.Applier header = global.template(headerFormat).applier();
        MarkdownTemplate.Applier body = MarkdownSpec.create(source).template(bodyFormat).applier();

        header.inherit(event.getFormatter().getHeader()).withOptions(source, SubjectData.GLOBAL_CONTEXT);
        body.inherit(event.getFormatter().getBody()).withOptions(source, SubjectData.GLOBAL_CONTEXT);

        event.getFormatter().getHeader().set(0, header);
        event.getFormatter().getBody().set(0, body);
    }
}
