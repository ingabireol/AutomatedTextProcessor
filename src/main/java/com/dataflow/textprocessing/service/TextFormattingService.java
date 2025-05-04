package com.dataflow.textprocessing.service;

import com.dataflow.textprocessing.model.TextDocument;

public interface TextFormattingService {
    /**
     * Convert text case based on the specified mode
     */
    TextDocument convertCase(TextDocument document, CaseMode mode);

    /**
     * Format JSON text
     */
    TextDocument formatJSON(TextDocument document);

    /**
     * Format XML text
     */
    TextDocument formatXML(TextDocument document);

    /**
     * Format SQL queries
     */
    TextDocument formatSQL(TextDocument document);

    /**
     * Format code (supports multiple languages)
     */
    TextDocument formatCode(TextDocument document, String language);

    /**
     * Remove duplicate lines
     */
    TextDocument removeDuplicates(TextDocument document);

    /**
     * Sort lines
     */
    TextDocument sortLines(TextDocument document, boolean ascending);

    /**
     * Enum for case conversion modes
     */
    enum CaseMode {
        UPPER,
        LOWER,
        TITLE,
        SENTENCE,
        CAMEL,
        SNAKE,
        KEBAB
    }
} 