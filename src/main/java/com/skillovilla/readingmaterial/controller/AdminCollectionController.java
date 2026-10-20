package com.skillovilla.readingmaterial.controller;

import com.skillovilla.readingmaterial.dto.request.AddMaterialToCollectionRequest;
import com.skillovilla.readingmaterial.dto.request.CreateCollectionRequest;
import com.skillovilla.readingmaterial.dto.response.CollectionResponse;
import com.skillovilla.readingmaterial.service.CollectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/collections")
public class AdminCollectionController {

    private final CollectionService collectionService;

    public AdminCollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @PostMapping
    public ResponseEntity<CollectionResponse> createCollection(
            @Valid @RequestBody CreateCollectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(collectionService.createCollection(request));
    }

    @PostMapping("/{collectionId}/materials")
    public ResponseEntity<Void> addMaterial(
            @PathVariable UUID collectionId,
            @Valid @RequestBody AddMaterialToCollectionRequest request) {
        collectionService.addMaterialToCollection(collectionId, request.materialId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{collectionId}/materials/{materialId}")
    public ResponseEntity<Void> removeMaterial(
            @PathVariable UUID collectionId,
            @PathVariable UUID materialId) {
        collectionService.removeMaterialFromCollection(collectionId, materialId);
        return ResponseEntity.noContent().build();
    }
}
