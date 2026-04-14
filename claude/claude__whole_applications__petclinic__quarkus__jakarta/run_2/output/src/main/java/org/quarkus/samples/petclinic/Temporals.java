package org.quarkus.samples.petclinic;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for date formatting.
 */
public class Temporals {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String format(LocalDate localDate) {
        if (localDate == null) return "";
        return localDate.format(FORMATTER);
    }
}
