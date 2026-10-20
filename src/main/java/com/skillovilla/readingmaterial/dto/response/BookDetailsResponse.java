package com.skillovilla.readingmaterial.dto.response;

import java.util.List;

public record BookDetailsResponse(
    String author,
    List<ChapterResponse> chapters
) {}
