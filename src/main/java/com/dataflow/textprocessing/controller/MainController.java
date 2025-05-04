package com.dataflow.textprocessing.controller;

import com.dataflow.textprocessing.model.TextDocument;
import com.dataflow.textprocessing.service.TextProcessingService;
import com.dataflow.textprocessing.service.TextFormattingService;
import com.dataflow.textprocessing.service.BatchProcessingService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.util.Pair;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @FXML
    private TextArea inputTextArea;
    
    @FXML
    private TextArea outputTextArea;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private VBox regexPanelContainer;
    
    private RegexPanelController regexPanelController;
    private TextProcessingService textProcessingService;
    private TextFormattingService textFormattingService;
    private BatchProcessingService batchProcessingService;

    @FXML
    public void initialize() {
        logger.info("Initializing MainController");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/regex-panel.fxml"));
            VBox regexPanel = (VBox) regexPanelContainer.getChildren().get(0);
            regexPanelController = loader.getController();
            if (regexPanelController == null) {
                logger.error("Failed to get regex panel controller");
                showAlert("Error", "Failed to initialize regex panel");
            }
        } catch (Exception e) {
            logger.error("Failed to initialize regex panel", e);
            showAlert("Error", "Failed to initialize regex panel: " + e.getMessage());
        }
    }

    @FXML
    private void handleNew() {
        inputTextArea.clear();
        outputTextArea.clear();
        updateStatus("New document created");
    }

    @FXML
    private void handleOpen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Text File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        
        File file = fileChooser.showOpenDialog(inputTextArea.getScene().getWindow());
        if (file != null) {
            try {
                String content = Files.readString(file.toPath());
                inputTextArea.setText(content);
                updateStatus("File opened: " + file.getName());
            } catch (IOException e) {
                logger.error("Failed to open file", e);
                showAlert("Error", "Failed to open file: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSave() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Text File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        
        File file = fileChooser.showSaveDialog(inputTextArea.getScene().getWindow());
        if (file != null) {
            try {
                Files.writeString(file.toPath(), inputTextArea.getText());
                updateStatus("File saved: " + file.getName());
            } catch (IOException e) {
                logger.error("Failed to save file", e);
                showAlert("Error", "Failed to save file: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }

    @FXML
    private void handleProcess() {
        TextDocument document = createDocumentFromInput();
        if (document != null) {
            try {
                List<TextDocument> results = textProcessingService.processBatch(List.of(document));
                if (!results.isEmpty()) {
                    outputTextArea.setText(results.get(0).getContent());
                    updateStatus("Text processed successfully");
                }
            } catch (Exception e) {
                logger.error("Failed to process text", e);
                showAlert("Error", "Failed to process text: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear() {
        inputTextArea.clear();
        outputTextArea.clear();
        updateStatus("Cleared");
    }

    @FXML
    private void handleBatchProcessing() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Files for Batch Processing");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        
        List<File> files = fileChooser.showOpenMultipleDialog(inputTextArea.getScene().getWindow());
        if (files != null && !files.isEmpty()) {
            try {
                List<TextDocument> documents = files.stream()
                    .map(file -> {
                        try {
                            return new TextDocument(
                                file.getName(),
                                Files.readString(file.toPath())
                            );
                        } catch (IOException e) {
                            logger.error("Failed to read file: " + file.getName(), e);
                            return null;
                        }
                    })
                    .filter(doc -> doc != null)
                    .collect(Collectors.toList());

                batchProcessingService.processBatchWithProgress(documents, new BatchProcessingService.BatchProcessingCallback() {
                    @Override
                    public void onProgress(int current, int total) {
                        updateStatus(String.format("Processing %d/%d files", current, total));
                    }

                    @Override
                    public void onComplete(List<TextDocument> results) {
                        StringBuilder output = new StringBuilder();
                        for (TextDocument doc : results) {
                            output.append("File: ").append(doc.getName()).append("\n");
                            output.append(doc.getContent()).append("\n\n");
                        }
                        outputTextArea.setText(output.toString());
                        updateStatus("Batch processing completed");
                    }

                    @Override
                    public void onError(TextDocument document, Exception e) {
                        showAlert("Error", "Failed to process " + document.getName() + ": " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                logger.error("Failed to process batch", e);
                showAlert("Error", "Failed to process batch: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleFormatJSON() {
        TextDocument document = createDocumentFromInput();
        if (document != null) {
            try {
                TextDocument formatted = textFormattingService.formatJSON(document);
                outputTextArea.setText(formatted.getContent());
                updateStatus("JSON formatted");
            } catch (Exception e) {
                logger.error("Failed to format JSON", e);
                showAlert("Error", "Failed to format JSON: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleFormatXML() {
        TextDocument document = createDocumentFromInput();
        if (document != null) {
            try {
                TextDocument formatted = textFormattingService.formatXML(document);
                outputTextArea.setText(formatted.getContent());
                updateStatus("XML formatted");
            } catch (Exception e) {
                logger.error("Failed to format XML", e);
                showAlert("Error", "Failed to format XML: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleFormatSQL() {
        TextDocument document = createDocumentFromInput();
        if (document != null) {
            try {
                TextDocument formatted = textFormattingService.formatSQL(document);
                outputTextArea.setText(formatted.getContent());
                updateStatus("SQL formatted");
            } catch (Exception e) {
                logger.error("Failed to format SQL", e);
                showAlert("Error", "Failed to format SQL: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleFormatCode() {
        TextInputDialog dialog = new TextInputDialog("java");
        dialog.setTitle("Format Code");
        dialog.setHeaderText("Enter programming language");
        dialog.setContentText("Language:");

        dialog.showAndWait().ifPresent(language -> {
            TextDocument document = createDocumentFromInput();
            if (document != null) {
                try {
                    TextDocument formatted = textFormattingService.formatCode(document, language);
                    outputTextArea.setText(formatted.getContent());
                    updateStatus(language + " code formatted");
                } catch (Exception e) {
                    logger.error("Failed to format code", e);
                    showAlert("Error", "Failed to format code: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleFind() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Find Text");
        dialog.setHeaderText("Enter text to find");
        dialog.setContentText("Text:");

        dialog.showAndWait().ifPresent(text -> {
            String input = inputTextArea.getText();
            int index = input.indexOf(text);
            if (index >= 0) {
                outputTextArea.setText("Found at position: " + index);
                inputTextArea.selectRange(index, index + text.length());
            } else {
                outputTextArea.setText("Text not found");
            }
        });
    }

    @FXML
    private void handleReplace() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Replace Text");
        dialog.setHeaderText("Enter text to find and replace");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField findField = new TextField();
        TextField replaceField = new TextField();

        grid.add(new Label("Find:"), 0, 0);
        grid.add(findField, 1, 0);
        grid.add(new Label("Replace with:"), 0, 1);
        grid.add(replaceField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        ButtonType replaceButtonType = new ButtonType("Replace", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(replaceButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == replaceButtonType) {
                return new Pair<>(findField.getText(), replaceField.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            String find = result.getKey();
            String replace = result.getValue();
            String input = inputTextArea.getText();
            String output = input.replace(find, replace);
            inputTextArea.setText(output);
            updateStatus("Text replaced");
        });
    }

    @FXML
    private void handleCaseConversion() {
        Dialog<TextFormattingService.CaseMode> dialog = new Dialog<>();
        dialog.setTitle("Convert Case");
        dialog.setHeaderText("Select case conversion mode");

        ButtonType upperButton = new ButtonType("UPPER CASE", ButtonBar.ButtonData.OK_DONE);
        ButtonType lowerButton = new ButtonType("lower case", ButtonBar.ButtonData.OK_DONE);
        ButtonType titleButton = new ButtonType("Title Case", ButtonBar.ButtonData.OK_DONE);
        ButtonType sentenceButton = new ButtonType("Sentence case", ButtonBar.ButtonData.OK_DONE);
        ButtonType camelButton = new ButtonType("camelCase", ButtonBar.ButtonData.OK_DONE);
        ButtonType snakeButton = new ButtonType("snake_case", ButtonBar.ButtonData.OK_DONE);
        ButtonType kebabButton = new ButtonType("kebab-case", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(
            upperButton, lowerButton, titleButton, sentenceButton,
            camelButton, snakeButton, kebabButton, cancelButton
        );

        dialog.setResultConverter(buttonType -> {
            if (buttonType == upperButton) return TextFormattingService.CaseMode.UPPER;
            if (buttonType == lowerButton) return TextFormattingService.CaseMode.LOWER;
            if (buttonType == titleButton) return TextFormattingService.CaseMode.TITLE;
            if (buttonType == sentenceButton) return TextFormattingService.CaseMode.SENTENCE;
            if (buttonType == camelButton) return TextFormattingService.CaseMode.CAMEL;
            if (buttonType == snakeButton) return TextFormattingService.CaseMode.SNAKE;
            if (buttonType == kebabButton) return TextFormattingService.CaseMode.KEBAB;
            return null;
        });

        dialog.showAndWait().ifPresent(mode -> {
            TextDocument document = createDocumentFromInput();
            if (document != null) {
                try {
                    TextDocument converted = textFormattingService.convertCase(document, mode);
                    outputTextArea.setText(converted.getContent());
                    updateStatus("Case converted to " + mode);
                } catch (Exception e) {
                    logger.error("Failed to convert case", e);
                    showAlert("Error", "Failed to convert case: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleWordFrequency() {
        TextDocument document = createDocumentFromInput();
        if (document != null) {
            try {
                Map<String, Long> frequencies = textProcessingService.analyzeWordFrequency(document);
                StringBuilder output = new StringBuilder("Word Frequency Analysis:\n\n");
                frequencies.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .forEach(entry -> output.append(entry.getKey())
                        .append(": ")
                        .append(entry.getValue())
                        .append("\n"));
                outputTextArea.setText(output.toString());
                updateStatus("Word frequency analysis completed");
            } catch (Exception e) {
                logger.error("Failed to analyze word frequency", e);
                showAlert("Error", "Failed to analyze word frequency: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handlePatternExtraction() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Extract Pattern");
        dialog.setHeaderText("Enter start and end patterns");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField startField = new TextField();
        TextField endField = new TextField();

        grid.add(new Label("Start pattern:"), 0, 0);
        grid.add(startField, 1, 0);
        grid.add(new Label("End pattern:"), 0, 1);
        grid.add(endField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        ButtonType extractButtonType = new ButtonType("Extract", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(extractButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == extractButtonType) {
                return new Pair<>(startField.getText(), endField.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            TextDocument document = createDocumentFromInput();
            if (document != null) {
                try {
                    String startPattern = result.getKey();
                    String endPattern = result.getValue();
                    boolean isValid = textProcessingService.isValidRegexPattern(startPattern) && 
                                   textProcessingService.isValidRegexPattern(endPattern);
                    if (isValid) {
                        List<String> extracted = textProcessingService.extractBetweenPatterns(document, startPattern, endPattern);
                        outputTextArea.setText(String.join("\n\n", extracted));
                        updateStatus("Pattern extracted successfully");
                    } else {
                        showAlert("Error", "Invalid pattern");
                    }
                } catch (Exception e) {
                    logger.error("Failed to extract pattern", e);
                    showAlert("Error", "Failed to extract pattern: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleTextStatistics() {
        TextDocument document = createDocumentFromInput();
        if (document != null) {
            try {
                Map<String, Object> stats = textProcessingService.getTextStatistics(document);
                outputTextArea.setText(formatStatistics(stats));
                updateStatus("Text statistics generated");
            } catch (Exception e) {
                logger.error("Failed to generate text statistics", e);
                showAlert("Error", "Failed to generate text statistics: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleGenerateSummary() {
        TextInputDialog dialog = new TextInputDialog("3");
        dialog.setTitle("Generate Summary");
        dialog.setHeaderText("Enter maximum number of sentences");
        dialog.setContentText("Sentences:");

        dialog.showAndWait().ifPresent(sentences -> {
            try {
                int maxSentences = Integer.parseInt(sentences);
                TextDocument document = createDocumentFromInput();
                if (document != null) {
                    String summary = textProcessingService.generateSummary(document, maxSentences);
                    outputTextArea.setText(summary);
                    updateStatus("Summary generated");
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Please enter a valid number");
            } catch (Exception e) {
                logger.error("Failed to generate summary", e);
                showAlert("Error", "Failed to generate summary: " + e.getMessage());
            }
        });
    }

    private String formatStatistics(Map<String, Object> stats) {
        StringBuilder output = new StringBuilder("Text Statistics:\n\n");
        stats.forEach((key, value) -> output.append(key).append(": ").append(value).append("\n"));
        return output.toString();
    }

    private TextDocument createDocumentFromInput() {
        String text = inputTextArea.getText();
        if (text.isEmpty()) {
            showAlert("Error", "Please enter some text to process");
            return null;
        }
        return new TextDocument("input", text);
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
        logger.info(message);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 