# Automated Text Processing for DataFlow Solutions

A Java-based text processing application that provides advanced text manipulation capabilities using regular expressions, Java Streams API, and efficient file handling.

## Features

- Advanced Text Search & Extraction using Regular Expressions
- Automated File Processing with efficient I/O operations
- Word Frequency Analysis using Java Streams API
- Pattern-based Text Extraction
- Modern JavaFX User Interface
- Robust Error Handling and Logging

## Project Structure

The project follows the MVC (Model-View-Controller) design pattern:

```
src/main/java/com/dataflow/textprocessing/
├── controller/         # UI controllers
├── model/             # Data models
├── service/           # Business logic
│   └── impl/         # Service implementations
├── exception/         # Custom exceptions
└── util/             # Utility classes
```

## Requirements

- Java 17 or higher
- Maven 3.6 or higher

## Building and Running

1. Clone the repository
2. Build the project:
   ```bash
   mvn clean install
   ```
3. Run the application:
   ```bash
   mvn javafx:run
   ```

## Usage

1. Open a text file using File > Open
2. Use the various tools to process the text:
   - Find and Replace using regular expressions
   - Analyze word frequency
   - Extract text between patterns
3. Save the processed text using File > Save

## Development

The project uses:
- JavaFX for the user interface
- SLF4J for logging
- JUnit for testing
- Maven for dependency management and building