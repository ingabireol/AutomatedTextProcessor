package com.dataflow.textprocessing.service;

import com.dataflow.textprocessing.model.TextDocument;
import com.dataflow.textprocessing.exception.TextProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TextProcessingServiceImpl implements TextProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(TextProcessingServiceImpl.class);

    // ... existing methods ...

    @Override
    public List<String> extractMatches(TextDocument document, String pattern) {
        try {
            Pattern regex = Pattern.compile(pattern);
            Matcher matcher = regex.matcher(document.getContent());
            List<String> matches = new ArrayList<>();
            
            while (matcher.find()) {
                matches.add(matcher.group());
            }
            
            logger.info("Extracted {} matches from document {}", matches.size(), document.getId());
            return matches;
        } catch (Exception e) {
            logger.error("Error extracting matches from document {}: {}", document.getId(), e.getMessage());
            throw new TextProcessingException("Failed to extract matches", e);
        }
    }

    @Override
    public String replaceAllMatches(TextDocument document, String pattern, String replacement) {
        try {
            Pattern regex = Pattern.compile(pattern);
            String result = regex.matcher(document.getContent()).replaceAll(replacement);
            logger.info("Replaced matches in document {}", document.getId());
            return result;
        } catch (Exception e) {
            logger.error("Error replacing matches in document {}: {}", document.getId(), e.getMessage());
            throw new TextProcessingException("Failed to replace matches", e);
        }
    }

    @Override
    public boolean isValidRegexPattern(String pattern) {
        try {
            Pattern.compile(pattern);
            return true;
        } catch (Exception e) {
            logger.warn("Invalid regex pattern: {}", pattern);
            return false;
        }
    }

    @Override
    public TextDocument removeDuplicates(TextDocument document) {
        try {
            String[] lines = document.getContent().split("\n");
            Set<String> uniqueLines = new LinkedHashSet<>(Arrays.asList(lines));
            String result = String.join("\n", uniqueLines);
            
            TextDocument newDoc = new TextDocument();
            newDoc.setContent(result);
            newDoc.setName(document.getName() + " (deduplicated)");
            
            logger.info("Removed duplicates from document {}", document.getId());
            return newDoc;
        } catch (Exception e) {
            logger.error("Error removing duplicates from document {}: {}", document.getId(), e.getMessage());
            throw new TextProcessingException("Failed to remove duplicates", e);
        }
    }

    @Override
    public TextDocument sortLines(TextDocument document, boolean ascending) {
        try {
            String[] lines = document.getContent().split("\n");
            List<String> sortedLines = Arrays.asList(lines);
            Collections.sort(sortedLines, (a, b) -> ascending ? a.compareTo(b) : b.compareTo(a));
            
            TextDocument newDoc = new TextDocument();
            newDoc.setContent(String.join("\n", sortedLines));
            newDoc.setName(document.getName() + " (sorted)");
            
            logger.info("Sorted lines in document {}", document.getId());
            return newDoc;
        } catch (Exception e) {
            logger.error("Error sorting lines in document {}: {}", document.getId(), e.getMessage());
            throw new TextProcessingException("Failed to sort lines", e);
        }
    }

    @Override
    public TextDocument formatJSON(TextDocument document) {
        try {
            // Basic JSON formatting - in a real implementation, use a proper JSON library
            String content = document.getContent().trim();
            StringBuilder formatted = new StringBuilder();
            int indent = 0;
            
            for (char c : content.toCharArray()) {
                if (c == '{' || c == '[') {
                    formatted.append(c).append("\n").append("  ".repeat(++indent));
                } else if (c == '}' || c == ']') {
                    formatted.append("\n").append("  ".repeat(--indent)).append(c);
                } else if (c == ',') {
                    formatted.append(c).append("\n").append("  ".repeat(indent));
                } else {
                    formatted.append(c);
                }
            }
            
            TextDocument newDoc = new TextDocument();
            newDoc.setContent(formatted.toString());
            newDoc.setName(document.getName() + " (formatted JSON)");
            
            logger.info("Formatted JSON in document {}", document.getId());
            return newDoc;
        } catch (Exception e) {
            logger.error("Error formatting JSON in document {}: {}", document.getId(), e.getMessage());
            throw new TextProcessingException("Failed to format JSON", e);
        }
    }

    @Override
    public TextDocument formatXML(TextDocument document) {
        try {
            // Basic XML formatting - in a real implementation, use a proper XML library
            String content = document.getContent().trim();
            StringBuilder formatted = new StringBuilder();
            int indent = 0;
            
            for (char c : content.toCharArray()) {
                if (c == '<' && content.indexOf('>', content.indexOf(c)) > 0) {
                    if (content.charAt(content.indexOf(c) + 1) == '/') {
                        formatted.append("\n").append("  ".repeat(--indent));
                    } else {
                        formatted.append("\n").append("  ".repeat(indent));
                        indent++;
                    }
                }
                formatted.append(c);
            }
            
            TextDocument newDoc = new TextDocument();
            newDoc.setContent(formatted.toString());
            newDoc.setName(document.getName() + " (formatted XML)");
            
            logger.info("Formatted XML in document {}", document.getId());
            return newDoc;
        } catch (Exception e) {
            logger.error("Error formatting XML in document {}: {}", document.getId(), e.getMessage());
            throw new TextProcessingException("Failed to format XML", e);
        }
    }

    @Override
    public List<TextDocument> processBatch(List<TextDocument> documents) {
        logger.info("Starting batch processing of {} documents", documents.size());
        return documents.stream()
                .map(doc -> {
                    try {
                        return processText(doc);
                    } catch (Exception e) {
                        logger.error("Error processing document {}: {}", doc.getId(), e.getMessage());
                        throw new TextProcessingException("Batch processing failed", e);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public void processBatchAsync(List<TextDocument> documents, BatchProcessingCallback callback) {
        CompletableFuture.runAsync(() -> {
            List<TextDocument> results = new ArrayList<>();
            int total = documents.size();
            
            for (int i = 0; i < total; i++) {
                try {
                    TextDocument doc = documents.get(i);
                    TextDocument processed = processText(doc);
                    results.add(processed);
                    callback.onProgress(i + 1, total);
                } catch (Exception e) {
                    logger.error("Error in batch processing: {}", e.getMessage());
                    callback.onError(documents.get(i), e);
                }
            }
            
            callback.onComplete(results);
        });
    }
} 