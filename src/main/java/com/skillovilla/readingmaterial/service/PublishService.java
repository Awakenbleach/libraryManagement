package com.skillovilla.readingmaterial.service;

import com.skillovilla.readingmaterial.dto.response.MaterialVersionResponse;
import com.skillovilla.readingmaterial.entity.Material;
import com.skillovilla.readingmaterial.entity.MaterialVersion;
import com.skillovilla.readingmaterial.entity.VersionStatus;
import com.skillovilla.readingmaterial.exception.InvalidOperationException;
import com.skillovilla.readingmaterial.exception.ResourceNotFoundException;
import com.skillovilla.readingmaterial.repository.MaterialRepository;
import com.skillovilla.readingmaterial.repository.MaterialVersionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class PublishService {

    private final MaterialVersionRepository materialVersionRepository;
    private final EntityManager entityManager;
    private final TypeDetailServiceFactory typeDetailServiceFactory;

    public PublishService(
            MaterialVersionRepository materialVersionRepository,
            EntityManager entityManager,
            TypeDetailServiceFactory typeDetailServiceFactory) {
        this.materialVersionRepository = materialVersionRepository;
        this.entityManager = entityManager;
        this.typeDetailServiceFactory = typeDetailServiceFactory;
    }

    @Transactional
    public MaterialVersionResponse publishVersion(UUID materialId, UUID versionId) {
        Material material = entityManager.find(Material.class, materialId, LockModeType.PESSIMISTIC_WRITE);
        if (material == null) {
            throw new ResourceNotFoundException("Material", materialId.toString());
        }

        MaterialVersion targetVersion = materialVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("MaterialVersion", versionId.toString()));

        if (!targetVersion.getMaterial().getId().equals(materialId)) {
            throw new InvalidOperationException("Version does not belong to the specified material");
        }

        if (targetVersion.getStatus() == VersionStatus.LIVE) {
            throw new InvalidOperationException("Version is already LIVE");
        }

        materialVersionRepository.updateStatusByMaterialIdAndStatus(materialId, VersionStatus.LIVE,
                VersionStatus.ARCHIVED);

        targetVersion.setStatus(VersionStatus.LIVE);
        targetVersion.setPublishedAt(Instant.now());

        entityManager.flush();

        TypeDetailService detailService = typeDetailServiceFactory.getService(material.getMaterialType());

        return new MaterialVersionResponse(
                targetVersion.getId(),
                targetVersion.getVersionNumber(),
                targetVersion.getStatus(),
                targetVersion.getCreatedBy(),
                targetVersion.getCreatedAt(),
                targetVersion.getPublishedAt(),
                detailService.getDetailsResponse(targetVersion.getId()));
    }
}
