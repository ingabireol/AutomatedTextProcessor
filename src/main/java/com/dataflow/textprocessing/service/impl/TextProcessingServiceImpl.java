package com.dataflow.textprocessing.service.impl;

import com.dataflow.textprocessing.model.TextDocument;
import com.dataflow.textprocessing.service.TextProcessingService;
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
    private static final Pattern SENTENCE_PATTERN = Pattern.compile("[^.!?]+[.!?]+");
    private static final Pattern WORD_PATTERN = Pattern.compile("\\b\\w+\\b");

    @Override
    public List<String> searchPattern(TextDocument document, String pattern) {
        logger.debug("Searching for pattern: {} in document: {}", pattern, document.getName());
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(document.getContent());
        List<String> matches = new ArrayList<>();
        
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        
        return matches;
    }

    @Override
    public TextDocument replacePattern(TextDocument document, String pattern, String replacement) {
        logger.debug("Replacing pattern: {} with: {} in document: {}", pattern, replacement, document.getName());
        String newContent = document.getContent().replaceAll(pattern, replacement);
        document.setContent(newContent);
        return document;
    }

    @Override
    public Map<String, Long> analyzeWordFrequency(TextDocument document) {
        logger.debug("Analyzing word frequency in document: {}", document.getName());
        return Arrays.stream(document.getContent().toLowerCase().split("\\W+"))
                .filter(word -> !word.isEmpty())
                .collect(Collectors.groupingBy(
                        word -> word,
                        Collectors.counting()
                ));
    }

    @Override
    public List<String> extractBetweenPatterns(TextDocument document, String startPattern, String endPattern) {
        logger.debug("Extracting text between patterns: {} and {} in document: {}", 
                startPattern, endPattern, document.getName());
        
        Pattern start = Pattern.compile(startPattern);
        Pattern end = Pattern.compile(endPattern);
        String content = document.getContent();
        List<String> extracted = new ArrayList<>();
        
        Matcher startMatcher = start.matcher(content);
        while (startMatcher.find()) {
            int startIndex = startMatcher.end();
            String remainingText = content.substring(startIndex);
            Matcher endMatcher = end.matcher(remainingText);
            
            if (endMatcher.find()) {
                extracted.add(remainingText.substring(0, endMatcher.start()));
            }
        }
        
        return extracted;
    }

    @Override
    public String generateSummary(TextDocument document, int maxSentences) {
        logger.debug("Generating summary for document: {} with max {} sentences", 
                document.getName(), maxSentences);
        
        String content = document.getContent();
        List<String> sentences = new ArrayList<>();
        Matcher matcher = SENTENCE_PATTERN.matcher(content);
        
        while (matcher.find()) {
            sentences.add(matcher.group().trim());
        }
        
        // Simple summary: take first N sentences
        return sentences.stream()
                .limit(maxSentences)
                .collect(Collectors.joining(" "));
    }

    @Override
    public TextDocument convertCase(TextDocument document, CaseMode mode) {
        logger.debug("Converting case for document: {} to mode: {}", document.getName(), mode);
        
        String content = document.getContent();
        String convertedContent;
        
        switch (mode) {
            case UPPER:
                convertedContent = content.toUpperCase();
                break;
            case LOWER:
                convertedContent = content.toLowerCase();
                break;
            case TITLE:
                convertedContent = Arrays.stream(content.split("\\s+"))
                        .map(word -> word.isEmpty() ? "" : 
                            Character.toUpperCase(word.charAt(0)) + 
                            word.substring(1).toLowerCase())
                        .collect(Collectors.joining(" "));
                break;
            case SENTENCE:
                convertedContent = SENTENCE_PATTERN.matcher(content)
                        .replaceAll(match -> {
                            String sentence = match.group();
                            return Character.toUpperCase(sentence.charAt(0)) + 
                                   sentence.substring(1).toLowerCase();
                        });
                break;
            default:
                convertedContent = content;
        }
        
        document.setContent(convertedContent);
        return document;
    }

    @Override
    public Map<String, Object> getTextStatistics(TextDocument document) {
        logger.debug("Getting text statistics for document: {}", document.getName());
        
        String content = document.getContent();
        Map<String, Object> stats = new HashMap<>();
        
        // Basic statistics
        stats.put("totalCharacters", content.length());
        stats.put("totalWords", countWords(content));
        stats.put("totalSentences", countSentences(content));
        stats.put("totalParagraphs", countParagraphs(content));
        
        // Word length statistics
        Map<Integer, Long> wordLengths = Arrays.stream(content.split("\\W+"))
                .filter(word -> !word.isEmpty())
                .collect(Collectors.groupingBy(
                        String::length,
                        Collectors.counting()
                ));
        stats.put("wordLengthDistribution", wordLengths);
        
        // Character frequency
        Map<Character, Long> charFreq = content.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.groupingBy(
                        Character::toLowerCase,
                        Collectors.counting()
                ));
        stats.put("characterFrequency", charFreq);
        
        return stats;
    }

    @Override
    public boolean validatePattern(TextDocument document, String pattern) {
        logger.debug("Validating pattern: {} for document: {}", pattern, document.getName());
        try {
            Pattern compiledPattern = Pattern.compile(pattern);
            return compiledPattern.matcher(document.getContent()).matches();
        } catch (Exception e) {
            logger.error("Invalid pattern: {}", pattern, e);
            return false;
        }
    }

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
            
            TextDocument newDoc = new TextDocument(document.getName() + " (deduplicated)", result);
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
            
            TextDocument newDoc = new TextDocument(document.getName() + " (sorted)", String.join("\n", sortedLines));
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
            
            TextDocument newDoc = new TextDocument(document.getName() + " (formatted JSON)", formatted.toString());
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
            
            TextDocument newDoc = new TextDocument(document.getName() + " (formatted XML)", formatted.toString());
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

    private TextDocument processText(TextDocument document) {
        // Basic text processing - can be extended with more operations
        String content = document.getContent();
        content = content.trim()
                        .replaceAll("\\s+", " ")
                        .replaceAll("\\n\\s*\\n", "\n\n");
        
        TextDocument processed = new TextDocument(document.getName() + " (processed)", content);
        logger.info("Processed document {}", document.getId());
        return processed;
    }

    private int countWords(String text) {
        return (int) WORD_PATTERN.matcher(text).results().count();
    }

    private int countSentences(String text) {
        return (int) SENTENCE_PATTERN.matcher(text).results().count();
    }

    private int countParagraphs(String text) {
        return (int) Arrays.stream(text.split("\\n\\s*\\n"))
                .filter(para -> !para.trim().isEmpty())
                .count();
    }
} 