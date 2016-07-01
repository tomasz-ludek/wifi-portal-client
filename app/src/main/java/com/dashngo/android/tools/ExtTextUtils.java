package com.dashngo.android.tools;

import java.util.Locale;

public class ExtTextUtils {

    public static String formatPrice(String prefix, float value) {
        return String.format(Locale.US, prefix + "%.2f", value);
    }
}
