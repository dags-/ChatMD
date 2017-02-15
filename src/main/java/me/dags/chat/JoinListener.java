package me.dags.chat;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;

/**
 * @author dags <dags@dags.me>
 */
public final class JoinListener {

    private final ChatOptions defaultOptions;
    private final ImmutableList<ChatOptions> options;

    JoinListener(ChatOptions defaultOptions, Iterable<ChatOptions> options) {
        this.defaultOptions = defaultOptions;
        this.options = ImmutableList.copyOf(options);
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join event, @Root CommandSource source) {
        ChatOptions applicable = defaultOptions;
        for (ChatOptions options : this.options) {
            if (options.applicableTo(source)) {
                applicable = options.highestPriority(applicable);
            }
        }
        applicable.apply(source);
    }
}