package com.example.newsapi.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTimeUtils {

    private final static String DATE_FORMATTER = "yyyy-MM-dd";
    public static boolean isValidDate(String dateStr) {
        if (dateStr == null) return false;
        try {
            LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(DATE_FORMATTER));
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }
}
