package com.skillovilla.readingmaterial.dto.request;

import java.time.LocalDate;

public record GrantAccessRequest(
    LocalDate accessEndDate
) {}
