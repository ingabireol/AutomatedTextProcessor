package com.dataflow.textprocessing.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexPanelController {
    private static final Logger logger = LoggerFactory.getLogger(RegexPanelController.class);

    @FXML
    private TextField patternField;
    
    @FXML
    private TextField replacementField;
    
    @FXML
    private TextArea resultArea;
    
    @FXML
    private VBox regexPanel;

    @FXML
    public void initialize() {
        logger.info("Initializing RegexPanelController");
    }

    @FXML
    private void handleValidate() {
        String pattern = patternField.getText();
        if (pattern.isEmpty()) {
            showAlert("Error", "Please enter a pattern to validate.");
            return;
        }

        try {
            Pattern.compile(pattern);
            showAlert("Success", "Pattern is valid!");
        } catch (PatternSyntaxException e) {
            showAlert("Error", "Invalid pattern: " + e.getMessage());
        }
    }

    @FXML
    private void handleFind() {
        String pattern = patternField.getText();
        if (pattern.isEmpty()) {
            showAlert("Error", "Please enter a pattern to find.");
            return;
        }

        try {
            Pattern compiledPattern = Pattern.compile(pattern);
            String text = resultArea.getText();
            java.util.regex.Matcher matcher = compiledPattern.matcher(text);
            
            StringBuilder results = new StringBuilder();
            int count = 0;
            while (matcher.find()) {
                count++;
                results.append("Match ").append(count).append(": ")
                      .append(matcher.group())
                      .append(" (at position ").append(matcher.start())
                      .append(")\n");
            }
            
            if (count == 0) {
                results.append("No matches found.");
            }
            
            resultArea.setText(results.toString());
        } catch (PatternSyntaxException e) {
            showAlert("Error", "Invalid pattern: " + e.getMessage());
        }
    }

    @FXML
    private void handleReplace() {
        String pattern = patternField.getText();
        String replacement = replacementField.getText();
        
        if (pattern.isEmpty()) {
            showAlert("Error", "Please enter a pattern to replace.");
            return;
        }

        try {
            Pattern compiledPattern = Pattern.compile(pattern);
            String text = resultArea.getText();
            String result = compiledPattern.matcher(text).replaceAll(replacement);
            resultArea.setText(result);
        } catch (PatternSyntaxException e) {
            showAlert("Error", "Invalid pattern: " + e.getMessage());
        }
    }

    @FXML
    private void handleExtract() {
        String pattern = patternField.getText();
        if (pattern.isEmpty()) {
            showAlert("Error", "Please enter a pattern to extract.");
            return;
        }

        try {
            Pattern compiledPattern = Pattern.compile(pattern);
            String text = resultArea.getText();
            java.util.regex.Matcher matcher = compiledPattern.matcher(text);
            
            StringBuilder results = new StringBuilder();
            int count = 0;
            while (matcher.find()) {
                count++;
                results.append("Extracted ").append(count).append(": ")
                      .append(matcher.group())
                      .append("\n");
            }
            
            if (count == 0) {
                results.append("No matches found.");
            }
            
            resultArea.setText(results.toString());
        } catch (PatternSyntaxException e) {
            showAlert("Error", "Invalid pattern: " + e.getMessage());
        }
    }

    @FXML
    private void handleEmailPattern() {
        patternField.setText("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b");
    }

    @FXML
    private void handlePhonePattern() {
        patternField.setText("\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b");
    }

    @FXML
    private void handleUrlPattern() {
        patternField.setText("\\bhttps?://[\\w\\d\\-._~:/?#\\[\\]@!$&'()*+,;=]+\\b");
    }

    @FXML
    private void handleDatePattern() {
        patternField.setText("\\b\\d{4}-\\d{2}-\\d{2}\\b");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setText(String text) {
        resultArea.setText(text);
    }

    public String getText() {
        return resultArea.getText();
    }
} 