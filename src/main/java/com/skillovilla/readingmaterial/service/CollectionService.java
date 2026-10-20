package com.skillovilla.readingmaterial.service;

import com.skillovilla.readingmaterial.dto.request.CreateCollectionRequest;
import com.skillovilla.readingmaterial.dto.response.CollectionResponse;
import com.skillovilla.readingmaterial.entity.Collection;
import com.skillovilla.readingmaterial.entity.CollectionMaterial;
import com.skillovilla.readingmaterial.entity.Material;
import com.skillovilla.readingmaterial.exception.InvalidOperationException;
import com.skillovilla.readingmaterial.exception.ResourceNotFoundException;
import com.skillovilla.readingmaterial.repository.CollectionMaterialRepository;
import com.skillovilla.readingmaterial.repository.CollectionRepository;
import com.skillovilla.readingmaterial.repository.MaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final MaterialRepository materialRepository;
    private final CollectionMaterialRepository collectionMaterialRepository;

    public CollectionService(CollectionRepository collectionRepository,
                              MaterialRepository materialRepository,
                              CollectionMaterialRepository collectionMaterialRepository) {
        this.collectionRepository = collectionRepository;
        this.materialRepository = materialRepository;
        this.collectionMaterialRepository = collectionMaterialRepository;
    }

    @Transactional
    public CollectionResponse createCollection(CreateCollectionRequest request) {
        Collection collection = new Collection();
        collection.setName(request.name());
        collection = collectionRepository.save(collection);
        return toResponse(collection);
    }

    @Transactional
    public void addMaterialToCollection(UUID collectionId, UUID materialId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection", collectionId.toString()));
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material", materialId.toString()));
        
        if (collectionMaterialRepository.existsByCollectionIdAndMaterialId(collectionId, materialId)) {
            throw new InvalidOperationException("Material is already in this collection");
        }
        
        CollectionMaterial cm = new CollectionMaterial(collection, material);
        collectionMaterialRepository.save(cm);
    }

    @Transactional
    public void removeMaterialFromCollection(UUID collectionId, UUID materialId) {
        if (!collectionMaterialRepository.existsByCollectionIdAndMaterialId(collectionId, materialId)) {
            throw new ResourceNotFoundException("CollectionMaterial", collectionId + "/" + materialId);
        }
        collectionMaterialRepository.deleteByCollectionIdAndMaterialId(collectionId, materialId);
    }

    private CollectionResponse toResponse(Collection collection) {
        return new CollectionResponse(collection.getId(), collection.getName(), collection.getCreatedAt());
    }
}
