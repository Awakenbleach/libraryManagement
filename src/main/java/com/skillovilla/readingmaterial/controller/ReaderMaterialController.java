package com.skillovilla.readingmaterial.controller;

import com.skillovilla.readingmaterial.dto.request.UpdateProgressRequest;
import com.skillovilla.readingmaterial.dto.response.*;
import com.skillovilla.readingmaterial.entity.MaterialType;
import com.skillovilla.readingmaterial.service.ProgressService;
import com.skillovilla.readingmaterial.service.ReaderMaterialService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reader/materials")
public class ReaderMaterialController {

    private final ReaderMaterialService readerMaterialService;
    private final ProgressService progressService;

    public ReaderMaterialController(ReaderMaterialService readerMaterialService,
                                     ProgressService progressService) {
        this.readerMaterialService = readerMaterialService;
        this.progressService = progressService;
    }

    /**
     * List all materials the reader has access to.
     * Only materials with a LIVE version are shown.
     * Paginated with deterministic ordering.
     */
    @GetMapping
    public ResponseEntity<PageResponse<MaterialResponse>> listMaterials(
            @RequestHeader("X-Reader-Id") UUID readerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(readerMaterialService.listAccessibleMaterials(readerId, page, size));
    }

    /**
     * Search materials with type-specific filters.
     * Access filtering and type-specific filtering happen in PostgreSQL.
     */
    @GetMapping("/search")
    public ResponseEntity<PageResponse<MaterialResponse>> searchMaterials(
            @RequestHeader("X-Reader-Id") UUID readerId,
            @RequestParam(required = false) MaterialType type,
            @RequestParam(required = false) Integer minPageCount,
            @RequestParam(required = false) Integer maxPageCount,
            @RequestParam(required = false) Integer minWordCount,
            @RequestParam(required = false) Integer maxWordCount,
            @RequestParam(required = false) Integer minSlideCount,
            @RequestParam(required = false) Integer maxSlideCount,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(readerMaterialService.searchMaterials(
                readerId, type, minPageCount, maxPageCount,
                minWordCount, maxWordCount, minSlideCount, maxSlideCount,
                author, title, page, size));
    }

    /**
     * Open a material. Returns the LIVE version.
     * Checks access.
     */
    @GetMapping("/{materialId}")
    public ResponseEntity<MaterialResponse> getMaterial(
            @RequestHeader("X-Reader-Id") UUID readerId,
            @PathVariable UUID materialId) {
        return ResponseEntity.ok(readerMaterialService.getAccessibleMaterial(readerId, materialId));
    }

    /**
     * Resume reading. Returns LIVE version + progress info.
     * If progress is on a different version, flags it.
     */
    @GetMapping("/{materialId}/resume")
    public ResponseEntity<ResumeResponse> resumeMaterial(
            @RequestHeader("X-Reader-Id") UUID readerId,
            @PathVariable UUID materialId) {
        return ResponseEntity.ok(readerMaterialService.resumeMaterial(readerId, materialId));
    }

    /**
     * Update reading progress with optimistic concurrency.
     * Client must send expectedRevision.
     */
    @PutMapping("/{materialId}/progress")
    public ResponseEntity<ProgressResponse> updateProgress(
            @RequestHeader("X-Reader-Id") UUID readerId,
            @PathVariable UUID materialId,
            @Valid @RequestBody UpdateProgressRequest request) {
        return ResponseEntity.ok(progressService.updateProgress(readerId, materialId, request));
    }
}
