package com.skillovilla.readingmaterial.controller;

import com.skillovilla.readingmaterial.dto.response.SeriesResponse;
import com.skillovilla.readingmaterial.service.SeriesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reader/series")
public class ReaderSeriesController {

    private final SeriesService seriesService;

    public ReaderSeriesController(SeriesService seriesService) {
        this.seriesService = seriesService;
    }

    @GetMapping("/{seriesId}")
    public ResponseEntity<SeriesResponse> getSeries(
            @RequestHeader("X-Reader-Id") UUID readerId,
            @PathVariable UUID seriesId) {
        return ResponseEntity.ok(seriesService.getSeriesWithProgress(seriesId, readerId));
    }
}
