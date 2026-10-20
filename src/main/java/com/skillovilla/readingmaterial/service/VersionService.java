package com.skillovilla.readingmaterial.service;

import com.skillovilla.readingmaterial.dto.request.CreateMaterialRequest;
import com.skillovilla.readingmaterial.dto.request.CreateVersionRequest;
import com.skillovilla.readingmaterial.dto.response.MaterialVersionResponse;
import com.skillovilla.readingmaterial.entity.Material;
import com.skillovilla.readingmaterial.entity.MaterialVersion;
import com.skillovilla.readingmaterial.entity.VersionStatus;
import com.skillovilla.readingmaterial.exception.InvalidOperationException;
import com.skillovilla.readingmaterial.exception.ResourceNotFoundException;
import com.skillovilla.readingmaterial.repository.MaterialRepository;
import com.skillovilla.readingmaterial.repository.MaterialVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class VersionService {

    private final MaterialRepository materialRepository;
    private final MaterialVersionRepository materialVersionRepository;
    private final TypeDetailServiceFactory typeDetailServiceFactory;

    public VersionService(MaterialRepository materialRepository,
                          MaterialVersionRepository materialVersionRepository,
                          TypeDetailServiceFactory typeDetailServiceFactory) {
        this.materialRepository = materialRepository;
        this.materialVersionRepository = materialVersionRepository;
        this.typeDetailServiceFactory = typeDetailServiceFactory;
    }

    @Transactional
    public MaterialVersionResponse createVersionFromCopy(UUID materialId, CreateVersionRequest request, UUID createdBy) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material", materialId.toString()));

        MaterialVersion sourceVersion = materialVersionRepository.findById(request.sourceVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("MaterialVersion", request.sourceVersionId().toString()));

        if (!sourceVersion.getMaterial().getId().equals(materialId)) {
            throw new InvalidOperationException("Source version does not belong to the specified material");
        }

        MaterialVersion lastVersion = materialVersionRepository.findTopByMaterialIdOrderByVersionNumberDesc(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("MaterialVersion", "last version"));

        MaterialVersion newVersion = new MaterialVersion();
        newVersion.setMaterial(material);
        newVersion.setVersionNumber(lastVersion.getVersionNumber() + 1);
        newVersion.setStatus(VersionStatus.DRAFT);
        newVersion.setCreatedBy(createdBy);
        newVersion = materialVersionRepository.save(newVersion);

        TypeDetailService detailService = typeDetailServiceFactory.getService(material.getMaterialType());
        detailService.copyDetails(sourceVersion, newVersion, request);

        return new MaterialVersionResponse(
                newVersion.getId(),
                newVersion.getVersionNumber(),
                newVersion.getStatus(),
                newVersion.getCreatedBy(),
                newVersion.getCreatedAt(),
                newVersion.getPublishedAt(),
                detailService.getDetailsResponse(newVersion.getId())
        );
    }

    @Transactional
    public MaterialVersionResponse updateDraftVersion(UUID materialId, UUID versionId, CreateMaterialRequest request) {
        MaterialVersion version = materialVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("MaterialVersion", versionId.toString()));

        if (!version.getMaterial().getId().equals(materialId)) {
            throw new InvalidOperationException("Version does not belong to the specified material");
        }

        if (version.getStatus() != VersionStatus.DRAFT) {
            throw new InvalidOperationException("Only DRAFT versions can be updated");
        }

        if (request.title() != null && !request.title().isBlank()) {
            version.getMaterial().setTitle(request.title());
            materialRepository.save(version.getMaterial());
        }

        TypeDetailService detailService = typeDetailServiceFactory.getService(version.getMaterial().getMaterialType());
        detailService.createDetails(version, request);

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
}

