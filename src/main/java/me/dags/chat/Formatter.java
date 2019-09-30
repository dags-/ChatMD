package me.dags.chat;

import me.dags.text.MUSpec;
import me.dags.text.template.MUApplier;
import me.dags.text.template.MUTemplate;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.text.Text;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Formatter {

    private final Format defaultFormat;
    private final List<Format> formats;
    private final MUTemplate header;
    private final MUTemplate body;

    public Formatter(String header, String body, Format def, List<Format> formats) {
        this.formats = formats.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        this.header = MUSpec.global().template(header);
        this.body = MUSpec.global().template(body);
        this.defaultFormat = def;
    }

    public void applyChatFormatting(MessageChannelEvent event, CommandSource subject) {
        Format format = getFormat(subject);

        String message = MUSpec.global().write(event.getFormatter().getBody().toText());

        MUApplier header = this.header.with(event.getFormatter().getHeader())
                .with(subject.getSubjectData().getOptions(SubjectData.GLOBAL_CONTEXT))
                .with("$prefix", format.getPrefix())
                .with("$name", format.getName())
                .with("name", subject.getName());

        MUApplier body = this.body.with(event.getFormatter().getBody())
                .with(subject.getSubjectData().getOptions(SubjectData.GLOBAL_CONTEXT))
                .with("$message", format.getMessage())
                .with("message", MUSpec.global().render(subject, message));

        setHeader(event.getFormatter(), header);

        setBody(event.getFormatter(), body);
    }

    public void applyTabListFormatting() {
        Map<Player, Text> names = Sponge.getServer().getOnlinePlayers().stream().collect(Collectors.toMap(
                Function.identity(),
                p -> getFormat(p).getTab().with("name", p.getName()).render()
        ));
        for (Player player : names.keySet()) {
            for (Map.Entry<Player, Text> entry : names.entrySet()) {
                Player other = entry.getKey();
                Text name = entry.getValue();
                player.getTabList().getEntry(other.getUniqueId()).ifPresent(e -> e.setDisplayName(name));
            }
        }
    }

    private Format getFormat(Subject subject) {
        for (Format format : formats) {
            if (subject.hasPermission(format.getPerm())) {
                return format;
            }
        }
        return defaultFormat;
    }

    private void setBody(MessageEvent.MessageFormatter formatter, MUApplier body) {
        if (formatter.getHeader().isEmpty()) {
            formatter.setBody(body);
        } else {
            formatter.getBody().set(0, body);
        }
    }

    private void setHeader(MessageEvent.MessageFormatter formatter, MUApplier header) {
        if (formatter.getHeader().isEmpty()) {
            formatter.setHeader(header);
        } else {
            formatter.getHeader().set(0, header);
        }
    }
}
