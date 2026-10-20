package com.skillovilla.readingmaterial.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChapterRequest(
    @NotNull(message = "Chapter number is required")
    @Min(value = 1, message = "Chapter number must be positive")
    Integer chapterNumber,
    
    @NotBlank(message = "Chapter title is required")
    String title
) {}
