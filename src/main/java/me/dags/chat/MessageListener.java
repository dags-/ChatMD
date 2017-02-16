package me.dags.chat;

import me.dags.spongemd.MarkdownSpec;
import me.dags.spongemd.MarkdownTemplate;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.text.TextElement;
import org.spongepowered.api.util.Tuple;

import java.util.HashMap;
import java.util.Map;

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
    public void onMessage(MessageChannelEvent event, @Root Player source) {
        if (MessageChannelEvent.Chat.class.isInstance(event)) {
            // Set formatting if the message hasn't already been modified
            if (event.getOriginalMessage().equals(event.getMessage())) {
                MarkdownTemplate.Applier header = global.template(headerFormat).applier();
                MarkdownTemplate.Applier body = MarkdownSpec.create(source).template(bodyFormat).applier();

                header.inherit(event.getFormatter().getHeader()).withOptions(source, SubjectData.GLOBAL_CONTEXT);
                body.inherit(event.getFormatter().getBody()).withOptions(source, SubjectData.GLOBAL_CONTEXT);

                event.getFormatter().getHeader().set(0, header);
                event.getFormatter().getBody().set(0, body);
            } else {
                transformParams(event, source, Tuple.of(ChatOptions.NAME, "header"), Tuple.of(ChatOptions.MESSAGE, "body"));
            }
        } else {
            // Apply ChatOption if event contains 'name' element
            transformParams(event, source, Tuple.of(ChatOptions.NAME, "name"));
        }
    }

    private void transformParams(MessageChannelEvent event, CommandSource source, Tuple<?, ?>... paramMappings) {
        Map<String, String> options = source.getSubjectData().getOptions(SubjectData.GLOBAL_CONTEXT);
        Map<String, MarkdownTemplate> templates = new HashMap<>();

        for (Tuple<?, ?> tuple : paramMappings) {
            String templateName = tuple.getFirst().toString();
            String paramName = tuple.getSecond().toString();

            String template = options.get(templateName);
            if (template != null) {
                templates.put(paramName, global.template(template));
            }
        }

        if (!templates.isEmpty()) {
            event.getFormatter().getAll().forEach(formatter -> formatter.getAll().forEach(applier -> {
                for (Map.Entry<String, MarkdownTemplate> template : templates.entrySet()) {
                    String paramName = template.getKey();
                    TextElement element = applier.getParameter(paramName);
                    if (element != null) {
                        TextElement transformed = template.getValue().with(element).with(paramName, element).render();
                        applier.setParameter(paramName, transformed);
                    }
                }
            }));
        }
    }
}
