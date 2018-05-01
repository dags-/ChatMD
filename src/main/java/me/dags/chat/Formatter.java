package me.dags.chat;

import com.google.common.collect.ImmutableList;
import java.util.List;
import me.dags.textmu.MarkupSpec;
import me.dags.textmu.MarkupTemplate;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.text.Text;

/**
 * @author dags <dags@dags.me>
 */
public final class Formatter {

    private final MarkupTemplate bodyFormat;
    private final MarkupTemplate headerFormat;
    private final Options defaultOptions;
    private final ImmutableList<Options> options;

    Formatter(Options defaultOptions, List<Options> options, String header, String body) {
        MarkupSpec spec = MarkupSpec.create();
        this.options = ImmutableList.copyOf(options);
        this.defaultOptions = defaultOptions;
        this.headerFormat = spec.template(header);
        this.bodyFormat = spec.template(body);
    }

    @Listener(order = Order.FIRST)
    public void onChat(MessageChannelEvent.Chat event, @Root Player player) {
        Options options = getOptions(player);
        MarkupTemplate chatTemplate = MarkupSpec.create(player).template(options.getChat());

        MarkupTemplate.Applier header = options.apply(headerFormat)
                .with(player.getSubjectData().getOptions(SubjectData.GLOBAL_CONTEXT))
                .with(event.getFormatter().getHeader())
                .with(Options.CHAT, chatTemplate);

        MarkupTemplate.Applier body = options.apply(bodyFormat)
                .with(player.getSubjectData().getOptions(SubjectData.GLOBAL_CONTEXT))
                .with(event.getFormatter().getBody())
                .with(Options.CHAT, chatTemplate);

        event.getFormatter().getHeader().set(0, header);
        event.getFormatter().getBody().set(0, body);
    }

    @Listener (order = Order.POST)
    public void onJoin(ClientConnectionEvent.Join event, @Root Player player) {
        syncTabs(player);
    }

    public void syncTabs(Player player) {
        for (Player online : Sponge.getServer().getOnlinePlayers()) {
            syncTabNames(player, online);
        }
    }

    private void syncTabNames(Player one, Player two) {
        one.getTabList().getEntry(two.getUniqueId()).ifPresent(entry -> entry.setDisplayName(getTabName(two)));
        two.getTabList().getEntry(one.getUniqueId()).ifPresent(entry -> entry.setDisplayName(getTabName(one)));
    }

    private Text getTabName(Player player) {
        return getOptions(player).formatTabName(player);
    }

    private Options getOptions(Subject subject) {
        Options applicable = defaultOptions;
        for (Options option : options) {
            if (option.applicableTo(subject)) {
                applicable = option.highestPriority(applicable);
            }
        }
        return applicable;
    }
}
