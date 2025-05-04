package com.dataflow.textprocessing.controller;

import com.dataflow.textprocessing.model.TextDocument;
import com.dataflow.textprocessing.service.TextProcessingService;
import com.dataflow.textprocessing.service.impl.TextProcessingServiceImpl;
import com.dataflow.textprocessing.util.TextProcessingUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    private final TextProcessingService textProcessingService;
    private TextDocument currentDocument;
    private Stage stage;

    @FXML
    private TextArea inputTextArea;
    
    @FXML
    private TextArea outputTextArea;
    
    @FXML
    private Label statusLabel;

    public MainController() {
        this.textProcessingService = new TextProcessingServiceImpl();
    }

    @FXML
    public void initialize() {
        logger.info("Initializing MainController");
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleOpenFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Text File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );

        File file = fileChooser.showOpenDialog(inputTextArea.getScene().getWindow());
        if (file != null) {
            try {
                String content = Files.readString(file.toPath());
                currentDocument = new TextDocument(file.getName(), content);
                inputTextArea.setText(content);
                statusLabel.setText("File loaded: " + file.getName());
            } catch (IOException e) {
                logger.error("Error reading file", e);
                showError("Error", "Could not read file: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSaveFile() {
        if (currentDocument == null) {
            showError("Error", "No document to save");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Text File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );

        File file = fileChooser.showSaveDialog(inputTextArea.getScene().getWindow());
        if (file != null) {
            try {
                Files.writeString(file.toPath(), currentDocument.getContent());
                statusLabel.setText("File saved: " + file.getName());
            } catch (IOException e) {
                logger.error("Error saving file", e);
                showError("Error", "Could not save file: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleProcess() {
        String input = inputTextArea.getText();
        if (input.isEmpty()) {
            showError("Error", "Please enter some text to process");
            return;
        }

        currentDocument = new TextDocument("Untitled", input);
        // Process the text and show results
        Map<String, Long> wordFreq = textProcessingService.analyzeWordFrequency(currentDocument);
        outputTextArea.setText(formatWordFrequency(wordFreq));
        statusLabel.setText("Text processed successfully");
    }

    @FXML
    private void handleClear() {
        inputTextArea.clear();
        outputTextArea.clear();
        currentDocument = null;
        statusLabel.setText("Ready");
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }

    @FXML
    private void handleWordFrequency() {
        if (currentDocument == null) {
            createDocumentFromInput();
        }
        Map<String, Long> wordFreq = textProcessingService.analyzeWordFrequency(currentDocument);
        outputTextArea.setText(formatWordFrequency(wordFreq));
        statusLabel.setText("Word frequency analysis completed");
    }

    @FXML
    private void handlePatternExtraction() {
        if (currentDocument == null) {
            createDocumentFromInput();
        }

        TextInputDialog startDialog = new TextInputDialog();
        startDialog.setTitle("Pattern Extraction");
        startDialog.setHeaderText("Enter Start Pattern");
        startDialog.setContentText("Pattern:");

        TextInputDialog endDialog = new TextInputDialog();
        endDialog.setTitle("Pattern Extraction");
        endDialog.setHeaderText("Enter End Pattern");
        endDialog.setContentText("Pattern:");

        startDialog.showAndWait().ifPresent(startPattern -> {
            if (TextProcessingUtils.isValidRegexPattern(startPattern)) {
                endDialog.showAndWait().ifPresent(endPattern -> {
                    if (TextProcessingUtils.isValidRegexPattern(endPattern)) {
                        List<String> extracted = textProcessingService.extractBetweenPatterns(
                            currentDocument, startPattern, endPattern);
                        outputTextArea.setText(String.join("\n\n", extracted));
                        statusLabel.setText("Pattern extraction completed");
                    } else {
                        showError("Invalid Pattern", "The end pattern is not a valid regular expression");
                    }
                });
            } else {
                showError("Invalid Pattern", "The start pattern is not a valid regular expression");
            }
        });
    }

    @FXML
    private void handleTextStatistics() {
        if (currentDocument == null) {
            createDocumentFromInput();
        }
        Map<String, Object> stats = textProcessingService.getTextStatistics(currentDocument);
        outputTextArea.setText(formatStatistics(stats));
        statusLabel.setText("Text statistics generated");
    }

    @FXML
    private void handleCaseConversion() {
        if (currentDocument == null) {
            createDocumentFromInput();
        }

        ChoiceDialog<TextProcessingService.CaseMode> dialog = new ChoiceDialog<>(
            TextProcessingService.CaseMode.UPPER,
            TextProcessingService.CaseMode.values()
        );
        dialog.setTitle("Case Conversion");
        dialog.setHeaderText("Select Case Conversion Mode");
        dialog.setContentText("Mode:");

        dialog.showAndWait().ifPresent(mode -> {
            currentDocument = textProcessingService.convertCase(currentDocument, mode);
            inputTextArea.setText(currentDocument.getContent());
            statusLabel.setText("Case conversion completed: " + mode);
        });
    }

    @FXML
    private void handleGenerateSummary() {
        if (currentDocument == null) {
            createDocumentFromInput();
        }

        TextInputDialog dialog = new TextInputDialog("3");
        dialog.setTitle("Generate Summary");
        dialog.setHeaderText("Enter Maximum Number of Sentences");
        dialog.setContentText("Sentences:");

        dialog.showAndWait().ifPresent(sentences -> {
            try {
                int maxSentences = Integer.parseInt(sentences);
                String summary = textProcessingService.generateSummary(currentDocument, maxSentences);
                outputTextArea.setText(summary);
                statusLabel.setText("Summary generated with " + maxSentences + " sentences");
            } catch (NumberFormatException e) {
                showError("Invalid Input", "Please enter a valid number of sentences");
            }
        });
    }

    @FXML
    private void handleFind() {
        if (currentDocument == null) {
            createDocumentFromInput();
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Find Text");
        dialog.setHeaderText("Enter text to find");
        dialog.setContentText("Text:");

        dialog.showAndWait().ifPresent(searchText -> {
            if (!searchText.isEmpty()) {
                List<String> matches = textProcessingService.searchPattern(currentDocument, searchText);
                if (matches.isEmpty()) {
                    outputTextArea.setText("No matches found.");
                } else {
                    outputTextArea.setText(String.format("Found %d matches:\n\n%s", 
                        matches.size(), String.join("\n", matches)));
                }
                statusLabel.setText("Find operation completed");
            } else {
                showError("Invalid Input", "Please enter text to find");
            }
        });
    }

    @FXML
    private void handleReplace() {
        if (currentDocument == null) {
            createDocumentFromInput();
        }

        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Replace Text");
        dialog.setHeaderText("Enter text to find and replace");

        // Set the button types
        ButtonType replaceButtonType = new ButtonType("Replace", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(replaceButtonType, ButtonType.CANCEL);

        // Create the find and replace fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField findText = new TextField();
        TextField replaceText = new TextField();

        grid.add(new Label("Find:"), 0, 0);
        grid.add(findText, 1, 0);
        grid.add(new Label("Replace with:"), 0, 1);
        grid.add(replaceText, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Convert the result to a pair when the replace button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == replaceButtonType) {
                return new Pair<>(findText.getText(), replaceText.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (!result.getKey().isEmpty()) {
                currentDocument = textProcessingService.replacePattern(
                    currentDocument, result.getKey(), result.getValue());
                inputTextArea.setText(currentDocument.getContent());
                statusLabel.setText("Replace operation completed");
            } else {
                showError("Invalid Input", "Please enter text to find");
            }
        });
    }

    @FXML
    private void handleValidatePattern() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Validate Pattern");
        dialog.setHeaderText("Enter Regular Expression Pattern");
        dialog.setContentText("Pattern:");

        dialog.showAndWait().ifPresent(pattern -> {
            if (textProcessingService.isValidRegexPattern(pattern)) {
                showAlert(Alert.AlertType.INFORMATION, "Valid Pattern", "The pattern is valid.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Invalid Pattern", "The pattern is not valid.");
            }
        });
    }

    @FXML
    private void handleExtractMatches() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Extract Matches");
        dialog.setHeaderText("Enter Regular Expression Pattern");
        dialog.setContentText("Pattern:");

        dialog.showAndWait().ifPresent(pattern -> {
            TextDocument document = createDocumentFromInput();
            List<String> matches = textProcessingService.extractMatches(document, pattern);
            outputTextArea.setText(String.join("\n", matches));
            updateStatus("Extracted " + matches.size() + " matches");
        });
    }

    @FXML
    private void handleReplaceAllMatches() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Replace All Matches");
        dialog.setHeaderText("Enter Pattern and Replacement");

        ButtonType replaceButtonType = new ButtonType("Replace", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(replaceButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField patternField = new TextField();
        TextField replacementField = new TextField();

        grid.add(new Label("Pattern:"), 0, 0);
        grid.add(patternField, 1, 0);
        grid.add(new Label("Replacement:"), 0, 1);
        grid.add(replacementField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == replaceButtonType) {
                return new Pair<>(patternField.getText(), replacementField.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            TextDocument document = createDocumentFromInput();
            String result = textProcessingService.replaceAllMatches(document, result.getKey(), result.getValue());
            outputTextArea.setText(result);
            updateStatus("Replaced all matches");
        });
    }

    @FXML
    private void handleRemoveDuplicates() {
        TextDocument document = createDocumentFromInput();
        TextDocument result = textProcessingService.removeDuplicates(document);
        outputTextArea.setText(result.getContent());
        updateStatus("Removed duplicates");
    }

    @FXML
    private void handleSortLines() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Sort Lines");
        dialog.setHeaderText("Choose Sort Order");

        ButtonType ascendingButton = new ButtonType("Ascending", ButtonBar.ButtonData.OK_DONE);
        ButtonType descendingButton = new ButtonType("Descending", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ascendingButton, descendingButton, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ascendingButton) return true;
            if (buttonType == descendingButton) return false;
            return null;
        });

        dialog.showAndWait().ifPresent(ascending -> {
            TextDocument document = createDocumentFromInput();
            TextDocument result = textProcessingService.sortLines(document, ascending);
            outputTextArea.setText(result.getContent());
            updateStatus("Sorted lines " + (ascending ? "ascending" : "descending"));
        });
    }

    @FXML
    private void handleFormatJSON() {
        TextDocument document = createDocumentFromInput();
        TextDocument result = textProcessingService.formatJSON(document);
        outputTextArea.setText(result.getContent());
        updateStatus("Formatted JSON");
    }

    @FXML
    private void handleFormatXML() {
        TextDocument document = createDocumentFromInput();
        TextDocument result = textProcessingService.formatXML(document);
        outputTextArea.setText(result.getContent());
        updateStatus("Formatted XML");
    }

    @FXML
    private void handleBatchProcess() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Files for Batch Processing");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        List<File> files = fileChooser.showOpenMultipleDialog(stage);

        if (files != null && !files.isEmpty()) {
            List<TextDocument> documents = files.stream()
                .map(file -> {
                    try {
                        TextDocument doc = new TextDocument();
                        doc.setName(file.getName());
                        doc.setContent(Files.readString(file.toPath()));
                        return doc;
                    } catch (IOException e) {
                        logger.error("Error reading file {}: {}", file.getName(), e.getMessage());
                        return null;
                    }
                })
                .filter(doc -> doc != null)
                .collect(Collectors.toList());

            ProgressDialog progressDialog = new ProgressDialog();
            progressDialog.setTitle("Batch Processing");
            progressDialog.setHeaderText("Processing " + documents.size() + " files");
            progressDialog.setContentText("Please wait...");

            textProcessingService.processBatchAsync(documents, new TextProcessingService.BatchProcessingCallback() {
                @Override
                public void onProgress(int current, int total) {
                    progressDialog.setProgress((double) current / total);
                }

                @Override
                public void onComplete(List<TextDocument> results) {
                    progressDialog.close();
                    StringBuilder output = new StringBuilder();
                    for (TextDocument doc : results) {
                        output.append("=== ").append(doc.getName()).append(" ===\n");
                        output.append(doc.getContent()).append("\n\n");
                    }
                    outputTextArea.setText(output.toString());
                    updateStatus("Batch processing completed");
                }

                @Override
                public void onError(TextDocument document, Exception error) {
                    logger.error("Error processing {}: {}", document.getName(), error.getMessage());
                }
            });

            progressDialog.show();
        }
    }

    private TextDocument createDocumentFromInput() {
        TextDocument document = new TextDocument();
        document.setContent(inputTextArea.getText());
        return document;
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
        logger.info(message);
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String formatWordFrequency(Map<String, Long> wordFreq) {
        StringBuilder sb = new StringBuilder("Word Frequency Analysis:\n\n");
        wordFreq.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry -> sb.append(String.format("%s: %d\n", entry.getKey(), entry.getValue())));
        return sb.toString();
    }

    private String formatStatistics(Map<String, Object> stats) {
        StringBuilder sb = new StringBuilder("Text Statistics:\n\n");
        
        // Basic statistics
        sb.append("Basic Statistics:\n");
        sb.append(String.format("Total Characters: %d\n", stats.get("totalCharacters")));
        sb.append(String.format("Total Words: %d\n", stats.get("totalWords")));
        sb.append(String.format("Total Sentences: %d\n", stats.get("totalSentences")));
        sb.append(String.format("Total Paragraphs: %d\n\n", stats.get("totalParagraphs")));
        
        // Word length distribution
        @SuppressWarnings("unchecked")
        Map<Integer, Long> wordLengths = (Map<Integer, Long>) stats.get("wordLengthDistribution");
        sb.append("Word Length Distribution:\n");
        wordLengths.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> sb.append(String.format("%d letters: %d words\n", 
                    entry.getKey(), entry.getValue())));
        
        return sb.toString();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 