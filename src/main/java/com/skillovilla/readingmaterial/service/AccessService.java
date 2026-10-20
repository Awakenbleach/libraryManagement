package com.skillovilla.readingmaterial.service;

import com.skillovilla.readingmaterial.dto.request.GrantAccessRequest;
import com.skillovilla.readingmaterial.dto.response.AccessResponse;
import com.skillovilla.readingmaterial.entity.*;
import com.skillovilla.readingmaterial.exception.InvalidOperationException;
import com.skillovilla.readingmaterial.exception.ResourceNotFoundException;
import com.skillovilla.readingmaterial.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AccessService {

    private final ReaderRepository readerRepository;
    private final MaterialRepository materialRepository;
    private final CollectionRepository collectionRepository;
    private final ReaderMaterialAccessRepository readerMaterialAccessRepository;
    private final ReaderCollectionAccessRepository readerCollectionAccessRepository;

    public AccessService(ReaderRepository readerRepository,
                          MaterialRepository materialRepository,
                          CollectionRepository collectionRepository,
                          ReaderMaterialAccessRepository readerMaterialAccessRepository,
                          ReaderCollectionAccessRepository readerCollectionAccessRepository) {
        this.readerRepository = readerRepository;
        this.materialRepository = materialRepository;
        this.collectionRepository = collectionRepository;
        this.readerMaterialAccessRepository = readerMaterialAccessRepository;
        this.readerCollectionAccessRepository = readerCollectionAccessRepository;
    }

    @Transactional
    public AccessResponse grantMaterialAccess(UUID readerId, UUID materialId, GrantAccessRequest request) {
        Reader reader = readerRepository.findById(readerId)
                .orElseThrow(() -> new ResourceNotFoundException("Reader", readerId.toString()));
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material", materialId.toString()));
        
        if (readerMaterialAccessRepository.existsByReaderIdAndMaterialId(readerId, materialId)) {
            throw new InvalidOperationException("Reader already has direct access to this material");
        }
        
        ReaderMaterialAccess access = new ReaderMaterialAccess();
        access.setReader(reader);
        access.setMaterial(material);
        if (request != null) {
            access.setAccessEndDate(request.accessEndDate());
        }
        
        access = readerMaterialAccessRepository.save(access);
        
        return new AccessResponse(
                access.getId(),
                "MATERIAL",
                materialId,
                readerId,
                access.getCreatedAt(),
                access.getAccessEndDate()
        );
    }

    @Transactional
    public void revokeMaterialAccess(UUID readerId, UUID materialId) {
        ReaderMaterialAccess access = readerMaterialAccessRepository.findByReaderIdAndMaterialId(readerId, materialId)
                .orElseThrow(() -> new ResourceNotFoundException("MaterialAccess", readerId + ":" + materialId));
        readerMaterialAccessRepository.delete(access);
    }

    @Transactional
    public AccessResponse grantCollectionAccess(UUID readerId, UUID collectionId, GrantAccessRequest request) {
        Reader reader = readerRepository.findById(readerId)
                .orElseThrow(() -> new ResourceNotFoundException("Reader", readerId.toString()));
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection", collectionId.toString()));
        
        if (readerCollectionAccessRepository.existsByReaderIdAndCollectionId(readerId, collectionId)) {
            throw new InvalidOperationException("Reader already has access to this collection");
        }
        
        ReaderCollectionAccess access = new ReaderCollectionAccess();
        access.setReader(reader);
        access.setCollection(collection);
        if (request != null) {
            access.setAccessEndDate(request.accessEndDate());
        }
        
        access = readerCollectionAccessRepository.save(access);
        
        return new AccessResponse(
                access.getId(),
                "COLLECTION",
                collectionId,
                readerId,
                access.getCreatedAt(),
                access.getAccessEndDate()
        );
    }

    @Transactional
    public void revokeCollectionAccess(UUID readerId, UUID collectionId) {
        ReaderCollectionAccess access = readerCollectionAccessRepository.findByReaderIdAndCollectionId(readerId, collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("CollectionAccess", readerId + ":" + collectionId));
        readerCollectionAccessRepository.delete(access);
    }
}

