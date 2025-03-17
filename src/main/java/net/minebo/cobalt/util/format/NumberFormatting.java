package net.minebo.cobalt.util.format;

import java.text.NumberFormat;
import java.util.Locale;

public class NumberFormatting {

    public static String addCommas(int number) {
        return NumberFormat.getNumberInstance(Locale.US).format(number);
    }

    public static String addSuffix(int number) {
        if(number == 1) return number + "st";
        if(number % 10 == 0) return number + "th";
        return number + "nd";
    }

}
