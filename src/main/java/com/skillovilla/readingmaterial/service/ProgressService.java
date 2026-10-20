package com.skillovilla.readingmaterial.service;

import com.skillovilla.readingmaterial.dto.request.UpdateProgressRequest;
import com.skillovilla.readingmaterial.dto.response.ProgressResponse;
import com.skillovilla.readingmaterial.entity.*;
import com.skillovilla.readingmaterial.exception.ConcurrencyConflictException;
import com.skillovilla.readingmaterial.exception.ResourceNotFoundException;
import com.skillovilla.readingmaterial.repository.MaterialVersionRepository;
import com.skillovilla.readingmaterial.repository.ReadingProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProgressService {

    private final ReadingProgressRepository progressRepository;
    private final MaterialVersionRepository versionRepository;
    private final ReaderAccessService readerAccessService;
    private final TypeDetailServiceFactory typeDetailServiceFactory;

    public ProgressService(ReadingProgressRepository progressRepository,
                           MaterialVersionRepository versionRepository,
                           ReaderAccessService readerAccessService,
                           TypeDetailServiceFactory typeDetailServiceFactory) {
        this.progressRepository = progressRepository;
        this.versionRepository = versionRepository;
        this.readerAccessService = readerAccessService;
        this.typeDetailServiceFactory = typeDetailServiceFactory;
    }

    @Transactional
    public ProgressResponse updateProgress(UUID readerId, UUID materialId, UpdateProgressRequest request) {
        readerAccessService.validateReader(readerId);
        readerAccessService.requireAccess(readerId, materialId);
        
        UUID targetVersionId;
        if (request.materialVersionId() != null) {
            targetVersionId = request.materialVersionId();
        } else {
            MaterialVersion liveVersion = versionRepository.findByMaterialIdAndStatus(materialId, VersionStatus.LIVE)
                    .orElseThrow(() -> new ResourceNotFoundException("LiveVersion", materialId.toString()));
            targetVersionId = liveVersion.getId();
        }
        
        MaterialVersion version = versionRepository.findById(targetVersionId)
                .orElseThrow(() -> new ResourceNotFoundException("MaterialVersion", targetVersionId.toString()));
        
        Material material = version.getMaterial();
        TypeDetailService typeService = typeDetailServiceFactory.getService(material.getMaterialType());
        
        typeService.validatePosition(request.positionValue(), targetVersionId);
        boolean completed = typeService.isCompleted(request.positionValue(), targetVersionId);
        
        Optional<ReadingProgress> existingProgress = progressRepository
                .findByReaderIdAndMaterialVersionId(readerId, targetVersionId);
        
        if (existingProgress.isPresent()) {
            ReadingProgress progress = existingProgress.get();
            
            if (request.expectedRevision() == null) {
                throw new ConcurrencyConflictException(
                        "expectedRevision is required for updating existing progress. Current revision: " + progress.getProgressRevision());
            }
            
            int updated = progressRepository.updateProgressOptimistic(
                    progress.getId(),
                    request.positionValue(),
                    completed,
                    request.expectedRevision()
            );
            
            if (updated == 0) {
                throw new ConcurrencyConflictException(
                        "Progress update conflict. Expected revision " + request.expectedRevision() 
                        + " but current revision is " + progress.getProgressRevision());
            }
            
            progress = progressRepository.findById(progress.getId()).orElseThrow();
            return toResponse(progress);
        } else {
            ReadingProgress progress = new ReadingProgress();
            progress.setReader(readerAccessService.validateReader(readerId));
            progress.setMaterialVersion(version);
            progress.setPositionValue(request.positionValue());
            progress.setPositionType(typeService.getPositionType());
            progress.setCompleted(completed);
            progress.setProgressRevision(1L);
            progress.setUpdatedAt(Instant.now());
            
            progress = progressRepository.save(progress);
            return toResponse(progress);
        }
    }

    public ProgressResponse getProgress(UUID readerId, UUID materialVersionId) {
        ReadingProgress progress = progressRepository
                .findByReaderIdAndMaterialVersionId(readerId, materialVersionId)
                .orElse(null);
        return progress != null ? toResponse(progress) : null;
    }

    private ProgressResponse toResponse(ReadingProgress progress) {
        return new ProgressResponse(
                progress.getId(),
                progress.getMaterialVersion().getId(),
                progress.getMaterialVersion().getVersionNumber(),
                progress.getPositionValue(),
                progress.getPositionType(),
                progress.isCompleted(),
                progress.getProgressRevision(),
                progress.getUpdatedAt()
        );
    }
}
