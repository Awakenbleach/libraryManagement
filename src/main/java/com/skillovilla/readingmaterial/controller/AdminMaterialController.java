package com.skillovilla.readingmaterial.controller;

import com.skillovilla.readingmaterial.dto.request.CreateMaterialRequest;
import com.skillovilla.readingmaterial.dto.request.CreateVersionRequest;
import com.skillovilla.readingmaterial.dto.response.MaterialDetailResponse;
import com.skillovilla.readingmaterial.dto.response.MaterialVersionResponse;
import com.skillovilla.readingmaterial.service.MaterialService;
import com.skillovilla.readingmaterial.service.PublishService;
import com.skillovilla.readingmaterial.service.VersionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/materials")
public class AdminMaterialController {

    private final MaterialService materialService;
    private final VersionService versionService;
    private final PublishService publishService;

    public AdminMaterialController(MaterialService materialService,
                                    VersionService versionService,
                                    PublishService publishService) {
        this.materialService = materialService;
        this.versionService = versionService;
        this.publishService = publishService;
    }

    @PostMapping
    public ResponseEntity<MaterialDetailResponse> createMaterial(
            @RequestHeader(value = "X-Admin-Id", required = false) UUID adminId,
            @Valid @RequestBody CreateMaterialRequest request) {
        MaterialDetailResponse response = materialService.createMaterial(request, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{materialId}")
    public ResponseEntity<MaterialDetailResponse> getMaterial(@PathVariable UUID materialId) {
        MaterialDetailResponse response = materialService.getMaterialAdmin(materialId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{materialId}/versions")
    public ResponseEntity<MaterialVersionResponse> createVersion(
            @PathVariable UUID materialId,
            @RequestHeader(value = "X-Admin-Id", required = false) UUID adminId,
            @Valid @RequestBody CreateVersionRequest request) {
        MaterialVersionResponse response = versionService.createVersionFromCopy(materialId, request, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{materialId}/versions/{versionId}/publish")
    public ResponseEntity<MaterialVersionResponse> publishVersion(
            @PathVariable UUID materialId,
            @PathVariable UUID versionId) {
        MaterialVersionResponse response = publishService.publishVersion(materialId, versionId);
        return ResponseEntity.ok(response);
    }
}
