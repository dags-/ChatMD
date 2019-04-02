package me.dags.chat;

import me.dags.text.MUSpec;
import me.dags.text.MUTemplate;

public class Format implements Comparable<Format> {

    public static final Format EMPTY = new Format();

    private final String id;
    private final String perm;
    private final int priority;
    private final MUTemplate tab;
    private final MUTemplate name;
    private final MUTemplate prefix;
    private final MUTemplate message;

    private Format() {
        this.id = "empty";
        this.perm = "";
        this.priority = Integer.MIN_VALUE;
        this.tab = MUTemplate.EMPTY;
        this.name = MUTemplate.EMPTY;
        this.prefix = MUTemplate.EMPTY;
        this.message = MUTemplate.EMPTY;
    }

    public Format(String id, String prefix, String name, String tab, String message, int priority) {
        this.id = id;
        this.priority = priority;
        this.perm = "muchat.format." + id;
        this.tab = MUSpec.global().template(tab);
        this.name = MUSpec.global().template(name);
        this.prefix = MUSpec.global().template(prefix);
        this.message = MUSpec.global().template(message);
    }

    @Override
    public int compareTo(Format o) {
        return Integer.compare(priority, o.priority);
    }

    public MUTemplate getMessage() {
        return message;
    }

    public MUTemplate getName() {
        return name;
    }

    public String getPerm() {
        return perm;
    }

    public MUTemplate getPrefix() {
        return prefix;
    }

    public MUTemplate getTab() {
        return tab;
    }

    @Override
    public String toString() {
        return id + "(" + priority + ")";
    }
}
