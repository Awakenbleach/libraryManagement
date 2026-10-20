package com.skillovilla.readingmaterial.service;

import com.skillovilla.readingmaterial.dto.response.*;
import com.skillovilla.readingmaterial.entity.*;
import com.skillovilla.readingmaterial.exception.ResourceNotFoundException;
import com.skillovilla.readingmaterial.repository.MaterialRepository;
import com.skillovilla.readingmaterial.repository.MaterialVersionRepository;
import com.skillovilla.readingmaterial.repository.ReadingProgressRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ReaderMaterialService {

    private final MaterialRepository materialRepository;
    private final MaterialVersionRepository materialVersionRepository;
    private final ReadingProgressRepository readingProgressRepository;
    private final ReaderAccessService readerAccessService;
    private final TypeDetailServiceFactory typeDetailServiceFactory;

    public ReaderMaterialService(MaterialRepository materialRepository,
                                 MaterialVersionRepository materialVersionRepository,
                                 ReadingProgressRepository readingProgressRepository,
                                 ReaderAccessService readerAccessService,
                                 TypeDetailServiceFactory typeDetailServiceFactory) {
        this.materialRepository = materialRepository;
        this.materialVersionRepository = materialVersionRepository;
        this.readingProgressRepository = readingProgressRepository;
        this.readerAccessService = readerAccessService;
        this.typeDetailServiceFactory = typeDetailServiceFactory;
    }

    public PageResponse<MaterialResponse> listAccessibleMaterials(UUID readerId, int page, int size) {
        readerAccessService.validateReader(readerId);

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending().and(Sort.by("id").descending()));
        Page<Material> materials = materialRepository.findAccessibleMaterials(readerId, LocalDate.now(), pageRequest);

        Page<MaterialResponse> responsePage = materials.map(this::toMaterialResponse);
        return PageResponse.from(responsePage);
    }

    public PageResponse<MaterialResponse> searchMaterials(
            UUID readerId,
            MaterialType type,
            Integer minPageCount, Integer maxPageCount,
            Integer minWordCount, Integer maxWordCount,
            Integer minSlideCount, Integer maxSlideCount,
            String author, String title,
            int page, int size) {

        readerAccessService.validateReader(readerId);

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending().and(Sort.by("id").descending()));
        Page<Material> materials = materialRepository.searchAccessibleMaterials(
                readerId, type, title,
                minPageCount, maxPageCount,
                minWordCount, maxWordCount,
                minSlideCount, maxSlideCount,
                author, LocalDate.now(), pageRequest);

        Page<MaterialResponse> responsePage = materials.map(this::toMaterialResponse);
        return PageResponse.from(responsePage);
    }

    public MaterialResponse getAccessibleMaterial(UUID readerId, UUID materialId) {
        readerAccessService.validateReader(readerId);
        readerAccessService.requireAccess(readerId, materialId);

        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material", materialId.toString()));

        return toMaterialResponse(material);
    }

    public ResumeResponse resumeMaterial(UUID readerId, UUID materialId) {
        readerAccessService.validateReader(readerId);
        readerAccessService.requireAccess(readerId, materialId);

        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material", materialId.toString()));

        MaterialVersion liveVersion = materialVersionRepository
                .findByMaterialIdAndStatus(materialId, VersionStatus.LIVE)
                .orElseThrow(() -> new ResourceNotFoundException("LiveVersion", materialId.toString()));

        MaterialVersionResponse liveVersionResponse = toVersionResponse(material, liveVersion);

        // Check progress on LIVE version
        Optional<ReadingProgress> liveProgressOpt = readingProgressRepository
                .findByReaderIdAndMaterialVersionId(readerId, liveVersion.getId());

        ProgressResponse currentProgress = null;
        boolean progressOnDifferentVersion = false;

        if (liveProgressOpt.isPresent()) {
            currentProgress = toProgressResponse(liveProgressOpt.get());
        } else {
            // Check if reader has progress on ANY older version of this material
            List<MaterialVersion> allVersions = materialVersionRepository.findByMaterialIdOrderByVersionNumberDesc(materialId);
            for (MaterialVersion v : allVersions) {
                if (!v.getId().equals(liveVersion.getId())) {
                    Optional<ReadingProgress> oldProgressOpt = readingProgressRepository
                            .findByReaderIdAndMaterialVersionId(readerId, v.getId());
                    if (oldProgressOpt.isPresent()) {
                        currentProgress = toProgressResponse(oldProgressOpt.get());
                        progressOnDifferentVersion = true;
                        break;
                    }
                }
            }
        }

        return new ResumeResponse(
                materialId,
                material.getTitle(),
                liveVersionResponse,
                currentProgress,
                progressOnDifferentVersion
        );
    }

    private MaterialResponse toMaterialResponse(Material material) {
        MaterialVersion liveVersion = materialVersionRepository
                .findByMaterialIdAndStatus(material.getId(), VersionStatus.LIVE)
                .orElseThrow(() -> new ResourceNotFoundException("LiveVersion", material.getId().toString()));

        return new MaterialResponse(
                material.getId(),
                material.getTitle(),
                material.getMaterialType(),
                material.getCreatedAt(),
                material.getUpdatedAt(),
                toVersionResponse(material, liveVersion)
        );
    }

    private MaterialVersionResponse toVersionResponse(Material material, MaterialVersion version) {
        TypeDetailService detailService = typeDetailServiceFactory.getService(material.getMaterialType());
        return new MaterialVersionResponse(
                version.getId(),
                version.getVersionNumber(),
                version.getStatus(),
                version.getCreatedBy(),
                version.getCreatedAt(),
                version.getPublishedAt(),
                detailService.getDetailsResponse(version.getId())
        );
    }

    private ProgressResponse toProgressResponse(ReadingProgress progress) {
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
