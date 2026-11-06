package com.thomasvitale.mousike.notebook;

import ai.docling.serve.api.DoclingServeApi;
import ai.docling.serve.api.convert.request.ConvertDocumentRequest;
import ai.docling.serve.api.convert.request.options.ConvertDocumentOptions;
import ai.docling.serve.api.convert.request.options.OutputFormat;
import ai.docling.serve.api.convert.request.source.FileSource;
import ai.docling.serve.api.convert.response.ConvertDocumentResponse;
import ai.docling.serve.api.convert.response.InBodyConvertDocumentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Base64;

@Service
public class NoteImporter {

    private static final Logger logger = LoggerFactory.getLogger(NoteImporter.class);

    private static final ConvertDocumentOptions TEXT_WITH_OCR = ConvertDocumentOptions.builder()
            .doOcr(true)
            .toFormat(OutputFormat.TEXT)
            .build();

    private final DoclingServeApi doclingServeApi;

    NoteImporter(DoclingServeApi doclingServeApi) {
        this.doclingServeApi = doclingServeApi;
    }

    public String extractText(String filename, byte[] bytes) {
        ConvertDocumentRequest request = ConvertDocumentRequest.builder()
                .source(FileSource.builder()
                        .filename(filename)
                        .base64String(Base64.getEncoder().encodeToString(bytes))
                        .build())
                .options(TEXT_WITH_OCR)
                .build();
        long start = System.nanoTime();
        ConvertDocumentResponse response = doclingServeApi.convertSource(request);
        if (!(response instanceof InBodyConvertDocumentResponse inBody)
                || inBody.getDocument().getTextContent() == null) {
            throw new IllegalStateException("Docling returned no text for " + filename);
        }
        String text = inBody.getDocument().getTextContent().strip();
        logger.info("Docling read {} characters from '{}' in {}", text.length(), filename,
                Duration.ofNanos(System.nanoTime() - start));
        return text;
    }

}
