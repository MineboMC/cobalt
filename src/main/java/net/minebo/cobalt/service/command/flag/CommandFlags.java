package net.minebo.cobalt.service.command.flag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Pulls {@code -x} style flags out of free text such as a punishment reason.
 * <p>
 * When allowed flags are given, only those are treated as flags and any other {@code -word} stays
 * in the text. With none given, every {@code -letters} token counts as a flag. As a command
 * parameter it consumes the rest of the arguments; restrict it with ACF's {@code @Flags("s,c,i")}.
 */
public final class CommandFlags {

    private static final CommandFlags EMPTY = new CommandFlags(Set.of(), "");

    private final Set<String> flags;
    private final String text;

    private CommandFlags(Set<String> flags, String text) {
        this.flags = flags;
        this.text = text;
    }

    public static CommandFlags empty() {
        return EMPTY;
    }

    public static CommandFlags parse(String raw, String... allowed) {
        return parse(raw, Arrays.stream(allowed).map(String::toLowerCase).collect(Collectors.toSet()));
    }

    public static CommandFlags parse(String raw, Set<String> allowed) {
        if (raw == null || raw.isBlank()) {
            return EMPTY;
        }

        Set<String> found = new HashSet<>();
        List<String> words = new ArrayList<>();
        for (String word : raw.trim().split("\\s+")) {
            String flag = asFlag(word, allowed);
            if (flag != null) {
                found.add(flag);
            } else {
                words.add(word);
            }
        }
        return new CommandFlags(Collections.unmodifiableSet(found), String.join(" ", words));
    }

    private static String asFlag(String word, Set<String> allowed) {
        if (word.length() < 2 || word.charAt(0) != '-') {
            return null;
        }
        String name = word.substring(1).toLowerCase();
        if (allowed.isEmpty()) {
            return name.chars().allMatch(Character::isLetter) ? name : null;
        }
        return allowed.contains(name) ? name : null;
    }

    public boolean has(String flag) {
        return flags.contains(flag.toLowerCase());
    }

    public Set<String> flags() {
        return flags;
    }

    public String text() {
        return text;
    }

    public String textOr(String fallback) {
        return text.isBlank() ? fallback : text;
    }
}
