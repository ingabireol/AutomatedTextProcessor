package com.dataflow.textprocessing.util;

import java.util.regex.Pattern;

public final class TextProcessingUtils {
    private TextProcessingUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Validates if a given string is a valid regular expression pattern
     * @param pattern The pattern to validate
     * @return true if the pattern is valid, false otherwise
     */
    public static boolean isValidRegexPattern(String pattern) {
        try {
            Pattern.compile(pattern);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Sanitizes input text by removing potentially harmful characters
     * @param input The input text to sanitize
     * @return Sanitized text
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll("[<>\"'&]", "");
    }

    /**
     * Normalizes whitespace in the input text
     * @param input The input text to normalize
     * @return Normalized text
     */
    public static String normalizeWhitespace(String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll("\\s+", " ").trim();
    }
} 