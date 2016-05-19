package com.dashngo.android.tools;

import java.util.Locale;

public class ExtTextUtils {

    public static String formatPrice(float value) {
        return String.format(Locale.US, "$%.2f", value);
    }
}
