package com.dataflow.textprocessing.service;

import com.dataflow.textprocessing.model.TextDocument;
import java.util.List;

public interface RegexProcessingService {
    /**
     * Search for text patterns using regular expressions
     */
    List<String> searchPattern(TextDocument document, String pattern);

    /**
     * Replace text patterns using regular expressions
     */
    TextDocument replacePattern(TextDocument document, String pattern, String replacement);

    /**
     * Extract text between specified patterns
     */
    List<String> extractBetweenPatterns(TextDocument document, String startPattern, String endPattern);

    /**
     * Validate if the text matches a specific pattern
     */
    boolean validatePattern(TextDocument document, String pattern);

    /**
     * Extract all matches of a pattern
     */
    List<String> extractMatches(TextDocument document, String pattern);

    /**
     * Replace all matches of a pattern
     */
    String replaceAllMatches(TextDocument document, String pattern, String replacement);

    /**
     * Check if a regex pattern is valid
     */
    boolean isValidRegexPattern(String pattern);
} 