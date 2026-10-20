package com.skillovilla.readingmaterial.dto.response;

public record PdfDetailsResponse(
    int pageCount,
    String fileUrl
) {}
