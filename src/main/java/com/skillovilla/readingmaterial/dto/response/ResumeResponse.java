package com.skillovilla.readingmaterial.dto.response;

import java.util.UUID;

public record ResumeResponse(
    UUID materialId,
    String title,
    MaterialVersionResponse liveVersion,
    ProgressResponse currentProgress,
    boolean progressOnDifferentVersion
) {}
