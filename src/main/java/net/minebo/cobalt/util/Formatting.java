package net.minebo.cobalt.util;

import java.text.NumberFormat;
import java.util.Locale;

public class Formatting {

    //Strings
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

    //Numbers
    public static String addCommas(int number) {
        return NumberFormat.getNumberInstance(Locale.US).format(number);
    }

    public static String addSuffix(int number) {
        if(number == 1) return number + "st";
        if(number % 10 == 0) return number + "th";
        return number + "nd";
    }
}
