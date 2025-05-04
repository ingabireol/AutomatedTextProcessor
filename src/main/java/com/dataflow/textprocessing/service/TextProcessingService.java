package com.dataflow.textprocessing.service;

import com.dataflow.textprocessing.model.TextDocument;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public interface TextProcessingService {
    /**
     * Search for text patterns using regular expressions
     * @param document The text document to search in
     * @param pattern The regex pattern to search for
     * @return List of matched strings
     */
    List<String> searchPattern(TextDocument document, String pattern);

    /**
     * Replace text patterns using regular expressions
     * @param document The text document to perform replacement in
     * @param pattern The regex pattern to match
     * @param replacement The replacement string
     * @return The modified text document
     */
    TextDocument replacePattern(TextDocument document, String pattern, String replacement);

    /**
     * Analyze word frequency in the document
     * @param document The text document to analyze
     * @return Map of words and their frequencies
     */
    Map<String, Long> analyzeWordFrequency(TextDocument document);

    /**
     * Extract text between specified patterns
     * @param document The text document to extract from
     * @param startPattern The starting pattern
     * @param endPattern The ending pattern
     * @return List of extracted text segments
     */
    List<String> extractBetweenPatterns(TextDocument document, String startPattern, String endPattern);

    /**
     * Generate a summary of the text document
     * @param document The text document to summarize
     * @param maxSentences Maximum number of sentences in the summary
     * @return Summary text
     */
    String generateSummary(TextDocument document, int maxSentences);

    /**
     * Convert text case based on the specified mode
     * @param document The text document to convert
     * @param mode The case conversion mode (UPPER, LOWER, TITLE, SENTENCE)
     * @return The modified text document
     */
    TextDocument convertCase(TextDocument document, CaseMode mode);

    /**
     * Get text statistics for the document
     * @param document The text document to analyze
     * @return Map containing various text statistics
     */
    Map<String, Object> getTextStatistics(TextDocument document);

    /**
     * Validate if the text matches a specific pattern
     * @param document The text document to validate
     * @param pattern The pattern to validate against
     * @return true if the text matches the pattern, false otherwise
     */
    boolean validatePattern(TextDocument document, String pattern);

    /**
     * Enum for case conversion modes
     */
    enum CaseMode {
        UPPER,
        LOWER,
        TITLE,
        SENTENCE
    }

    // New regex operations
    List<String> extractMatches(TextDocument document, String pattern);
    String replaceAllMatches(TextDocument document, String pattern, String replacement);
    boolean isValidRegexPattern(String pattern);

    // Advanced processing
    TextDocument removeDuplicates(TextDocument document);
    TextDocument sortLines(TextDocument document, boolean ascending);
    TextDocument formatJSON(TextDocument document);
    TextDocument formatXML(TextDocument document);

    // Batch processing
    List<TextDocument> processBatch(List<TextDocument> documents);
    void processBatchAsync(List<TextDocument> documents, BatchProcessingCallback callback);

    // Callback interface for batch processing
    interface BatchProcessingCallback {
        void onProgress(int current, int total);
        void onComplete(List<TextDocument> results);
        void onError(TextDocument document, Exception error);
    }
} 