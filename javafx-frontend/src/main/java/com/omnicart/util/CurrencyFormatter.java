package com.omnicart.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CurrencyFormatter {
    private static final DecimalFormat FORMATTER;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        FORMATTER = new DecimalFormat("#,##0.00", symbols);
    }

    public static String format(double amount) {
        return "₹" + FORMATTER.format(amount);
    }
}
