package com.skillovilla.readingmaterial.service;

import com.skillovilla.readingmaterial.entity.Reader;
import com.skillovilla.readingmaterial.exception.AccessDeniedException;
import com.skillovilla.readingmaterial.exception.ResourceNotFoundException;
import com.skillovilla.readingmaterial.repository.ReaderCollectionAccessRepository;
import com.skillovilla.readingmaterial.repository.ReaderMaterialAccessRepository;
import com.skillovilla.readingmaterial.repository.ReaderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ReaderAccessService {

    private final ReaderRepository readerRepository;
    private final ReaderMaterialAccessRepository readerMaterialAccessRepository;
    private final ReaderCollectionAccessRepository readerCollectionAccessRepository;

    public ReaderAccessService(ReaderRepository readerRepository,
                                ReaderMaterialAccessRepository readerMaterialAccessRepository,
                                ReaderCollectionAccessRepository readerCollectionAccessRepository) {
        this.readerRepository = readerRepository;
        this.readerMaterialAccessRepository = readerMaterialAccessRepository;
        this.readerCollectionAccessRepository = readerCollectionAccessRepository;
    }

    public Reader validateReader(UUID readerId) {
        return readerRepository.findById(readerId)
                .orElseThrow(() -> new ResourceNotFoundException("Reader", readerId.toString()));
    }

    public boolean hasAccess(UUID readerId, UUID materialId) {
        LocalDate today = LocalDate.now();
        
        boolean directAccess = readerMaterialAccessRepository.hasValidAccess(readerId, materialId, today);
        if (directAccess) return true;
        
        return readerCollectionAccessRepository.hasValidCollectionAccess(readerId, materialId, today);
    }

    public void requireAccess(UUID readerId, UUID materialId) {
        if (!hasAccess(readerId, materialId)) {
            throw new AccessDeniedException("Reader does not have access to this material");
        }
    }
}
