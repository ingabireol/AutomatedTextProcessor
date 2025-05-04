package com.dataflow.textprocessing.service;

import com.dataflow.textprocessing.model.TextDocument;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface BatchProcessingService {
    /**
     * Process a batch of documents
     */
    List<TextDocument> processBatch(List<TextDocument> documents);

    /**
     * Process a batch of documents asynchronously
     */
    CompletableFuture<List<TextDocument>> processBatchAsync(List<TextDocument> documents);

    /**
     * Process a batch of documents with progress tracking
     */
    void processBatchWithProgress(List<TextDocument> documents, BatchProcessingCallback callback);

    /**
     * Process a batch of documents with specific operation
     */
    <T> List<T> processBatchWithOperation(List<TextDocument> documents, BatchOperation<T> operation);

    /**
     * Interface for batch processing callback
     */
    interface BatchProcessingCallback {
        void onProgress(int current, int total);
        void onComplete(List<TextDocument> results);
        void onError(TextDocument document, Exception error);
    }

    /**
     * Interface for batch operations
     */
    interface BatchOperation<T> {
        T process(TextDocument document) throws Exception;
    }
} 