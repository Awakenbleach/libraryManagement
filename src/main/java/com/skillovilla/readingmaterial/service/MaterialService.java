package com.skillovilla.readingmaterial.service;

import com.skillovilla.readingmaterial.dto.request.CreateMaterialRequest;
import com.skillovilla.readingmaterial.dto.response.MaterialDetailResponse;
import com.skillovilla.readingmaterial.dto.response.MaterialVersionResponse;
import com.skillovilla.readingmaterial.entity.Material;
import com.skillovilla.readingmaterial.entity.MaterialVersion;
import com.skillovilla.readingmaterial.entity.VersionStatus;
import com.skillovilla.readingmaterial.exception.ResourceNotFoundException;
import com.skillovilla.readingmaterial.repository.MaterialRepository;
import com.skillovilla.readingmaterial.repository.MaterialVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final MaterialVersionRepository materialVersionRepository;
    private final TypeDetailServiceFactory typeDetailServiceFactory;

    public MaterialService(MaterialRepository materialRepository,
                           MaterialVersionRepository materialVersionRepository,
                           TypeDetailServiceFactory typeDetailServiceFactory) {
        this.materialRepository = materialRepository;
        this.materialVersionRepository = materialVersionRepository;
        this.typeDetailServiceFactory = typeDetailServiceFactory;
    }

    @Transactional
    public MaterialDetailResponse createMaterial(CreateMaterialRequest request, UUID createdBy) {
        TypeDetailService detailService = typeDetailServiceFactory.getService(request.materialType());
        detailService.validateRequest(request);

        Material material = new Material();
        material.setTitle(request.title());
        material.setMaterialType(request.materialType());
        material = materialRepository.save(material);

        MaterialVersion version = new MaterialVersion();
        version.setMaterial(material);
        version.setVersionNumber(1);
        version.setStatus(VersionStatus.DRAFT);
        version.setCreatedBy(createdBy);
        version = materialVersionRepository.save(version);

        detailService.createDetails(version, request);

        MaterialVersionResponse versionResponse = new MaterialVersionResponse(
                version.getId(),
                version.getVersionNumber(),
                version.getStatus(),
                version.getCreatedBy(),
                version.getCreatedAt(),
                version.getPublishedAt(),
                detailService.getDetailsResponse(version.getId())
        );

        return new MaterialDetailResponse(
                material.getId(),
                material.getTitle(),
                material.getMaterialType(),
                material.getCreatedAt(),
                material.getUpdatedAt(),
                List.of(versionResponse)
        );
    }

    public MaterialDetailResponse getMaterialAdmin(UUID materialId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material", materialId.toString()));

        List<MaterialVersion> versions = materialVersionRepository.findByMaterialIdOrderByVersionNumberDesc(materialId);

        List<MaterialVersionResponse> versionResponses = versions.stream().map(version -> {
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
        }).toList();

        return new MaterialDetailResponse(
                material.getId(),
                material.getTitle(),
                material.getMaterialType(),
                material.getCreatedAt(),
                material.getUpdatedAt(),
                versionResponses
        );
    }
}

