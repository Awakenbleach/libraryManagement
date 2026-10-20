package com.skillovilla.readingmaterial.controller;

import com.skillovilla.readingmaterial.dto.request.AddSeriesPartRequest;
import com.skillovilla.readingmaterial.dto.request.CreateSeriesRequest;
import com.skillovilla.readingmaterial.dto.response.SeriesResponse;
import com.skillovilla.readingmaterial.service.SeriesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/series")
public class AdminSeriesController {

    private final SeriesService seriesService;

    public AdminSeriesController(SeriesService seriesService) {
        this.seriesService = seriesService;
    }

    @PostMapping
    public ResponseEntity<SeriesResponse> createSeries(
            @Valid @RequestBody CreateSeriesRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(seriesService.createSeries(request));
    }

    @PostMapping("/{seriesId}/parts")
    public ResponseEntity<Void> addPart(
            @PathVariable UUID seriesId,
            @Valid @RequestBody AddSeriesPartRequest request) {
        seriesService.addPart(seriesId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
