package com.skillovilla.readingmaterial.controller;

import com.skillovilla.readingmaterial.dto.request.CreateReaderRequest;
import com.skillovilla.readingmaterial.dto.request.GrantAccessRequest;
import com.skillovilla.readingmaterial.dto.response.AccessResponse;
import com.skillovilla.readingmaterial.dto.response.ReaderResponse;
import com.skillovilla.readingmaterial.service.AccessService;
import com.skillovilla.readingmaterial.service.ReaderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminAccessController {

    private final AccessService accessService;
    private final ReaderService readerService;

    public AdminAccessController(AccessService accessService, ReaderService readerService) {
        this.accessService = accessService;
        this.readerService = readerService;
    }

    @PostMapping("/readers")
    public ResponseEntity<ReaderResponse> createReader(@Valid @RequestBody CreateReaderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(readerService.createReader(request));
    }

    @PostMapping("/readers/{readerId}/materials/{materialId}/access")
    public ResponseEntity<AccessResponse> grantMaterialAccess(
            @PathVariable UUID readerId,
            @PathVariable UUID materialId,
            @RequestBody(required = false) GrantAccessRequest request) {
        AccessResponse response = accessService.grantMaterialAccess(readerId, materialId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/readers/{readerId}/materials/{materialId}/access")
    public ResponseEntity<Void> revokeMaterialAccess(
            @PathVariable UUID readerId,
            @PathVariable UUID materialId) {
        accessService.revokeMaterialAccess(readerId, materialId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/readers/{readerId}/collections/{collectionId}/access")
    public ResponseEntity<AccessResponse> grantCollectionAccess(
            @PathVariable UUID readerId,
            @PathVariable UUID collectionId,
            @RequestBody(required = false) GrantAccessRequest request) {
        AccessResponse response = accessService.grantCollectionAccess(readerId, collectionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/readers/{readerId}/collections/{collectionId}/access")
    public ResponseEntity<Void> revokeCollectionAccess(
            @PathVariable UUID readerId,
            @PathVariable UUID collectionId) {
        accessService.revokeCollectionAccess(readerId, collectionId);
        return ResponseEntity.noContent().build();
    }
    /* TODO create api for getting readers by their id */
}
