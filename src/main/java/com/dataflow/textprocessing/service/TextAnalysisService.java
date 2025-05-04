package com.dataflow.textprocessing.service;

import com.dataflow.textprocessing.model.TextDocument;
import java.util.Map;

public interface TextAnalysisService {
    /**
     * Analyze word frequency in the document
     */
    Map<String, Long> analyzeWordFrequency(TextDocument document);

    /**
     * Get text statistics for the document
     */
    Map<String, Object> getTextStatistics(TextDocument document);

    /**
     * Generate a summary of the text document
     */
    String generateSummary(TextDocument document, int maxSentences);

    /**
     * Analyze sentence structure and complexity
     */
    Map<String, Object> analyzeSentenceStructure(TextDocument document);

    /**
     * Analyze readability metrics (Flesch-Kincaid, etc.)
     */
    Map<String, Double> analyzeReadability(TextDocument document);

    /**
     * Analyze language patterns and style
     */
    Map<String, Object> analyzeLanguagePatterns(TextDocument document);
} 