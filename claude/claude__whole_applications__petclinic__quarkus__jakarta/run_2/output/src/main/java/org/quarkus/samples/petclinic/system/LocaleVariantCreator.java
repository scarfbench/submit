package org.quarkus.samples.petclinic.system;

import java.util.Locale;

/**
 * Locale utility class.
 * Replaces Quarkus Qute Variant-based locale handling.
 */
public class LocaleVariantCreator {

    public static Locale getDefaultLocale() {
        return Locale.getDefault();
    }
}
