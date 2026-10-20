package com.skillovilla.readingmaterial.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BookDetailsRequest(
    @NotBlank(message = "Author is required")
    String author,
    
    @NotEmpty(message = "At least one chapter is required")
    @Valid
    List<ChapterRequest> chapters
) {}
