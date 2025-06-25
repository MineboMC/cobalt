package net.minebo.cobalt.util.format;

public class StringFormatting {

    public static String fixCapitalization(String input) {
        if (input == null || input.isEmpty()) return input;

        String[] words = input.toLowerCase().split("\\s+");
        StringBuilder capitalized = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                capitalized.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return capitalized.toString().trim();
    }

}
