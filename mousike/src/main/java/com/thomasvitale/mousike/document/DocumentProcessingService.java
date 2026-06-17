package com.thomasvitale.mousike.document;

import java.io.IOException;
import java.util.Base64;

import org.jspecify.annotations.Nullable;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import ai.docling.serve.api.DoclingServeApi;
import ai.docling.serve.api.convert.request.ConvertDocumentRequest;
import ai.docling.serve.api.convert.request.source.FileSource;
import ai.docling.serve.api.convert.response.ConvertDocumentResponse;
import ai.docling.serve.api.convert.response.InBodyConvertDocumentResponse;

@Service
public class DocumentProcessingService {

    private final DoclingServeApi doclingServeApi;

    public DocumentProcessingService(DoclingServeApi doclingServeApi) {
        this.doclingServeApi = doclingServeApi;
    }

    @Nullable
    public String process(Resource file) {
        Assert.notNull(file, "file cannot be null");
        Assert.hasText(file.getFilename(), "file must have a filename");

        String base64File;
        try {
            base64File = Base64.getEncoder().encodeToString(file.getContentAsByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ConvertDocumentResponse response = doclingServeApi
                .convertSource(ConvertDocumentRequest.builder()
                        .source(FileSource.builder()
                                .filename(file.getFilename())
                                .base64String(base64File)
                                .build())
                        .build());

        return ((InBodyConvertDocumentResponse) response).getDocument().getMarkdownContent();
    }

}
