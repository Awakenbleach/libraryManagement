package com.skillovilla.readingmaterial.service;

import com.skillovilla.readingmaterial.dto.request.CreateMaterialRequest;
import com.skillovilla.readingmaterial.dto.response.SlideDeckDetailsResponse;
import com.skillovilla.readingmaterial.entity.*;
import com.skillovilla.readingmaterial.exception.InvalidOperationException;
import com.skillovilla.readingmaterial.exception.ResourceNotFoundException;
import com.skillovilla.readingmaterial.repository.SlideDeckVersionDetailsRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SlideDeckDetailService implements TypeDetailService {

    private final SlideDeckVersionDetailsRepository slideDeckRepository;

    public SlideDeckDetailService(SlideDeckVersionDetailsRepository slideDeckRepository) {
        this.slideDeckRepository = slideDeckRepository;
    }

    @Override
    public MaterialType getSupportedType() { return MaterialType.SLIDE_DECK; }

    @Override
    public void validateRequest(CreateMaterialRequest request) {
        if (request.slideDeckDetails() == null) {
            throw new InvalidOperationException("Slide deck details are required for SLIDE_DECK material type");
        }
    }

    @Override
    public void createDetails(MaterialVersion version, CreateMaterialRequest request) {
        SlideDeckVersionDetails details = new SlideDeckVersionDetails();
        details.setMaterialVersion(version);
        details.setSlideCount(request.slideDeckDetails().slideCount());
        details.setFileUrl(request.slideDeckDetails().fileUrl());
        slideDeckRepository.save(details);
    }

    @Override
    public void copyDetails(MaterialVersion sourceVersion, MaterialVersion targetVersion, com.skillovilla.readingmaterial.dto.request.CreateVersionRequest overrides) {
        SlideDeckVersionDetails source = slideDeckRepository.findById(sourceVersion.getId())
                .orElseThrow(() -> new ResourceNotFoundException("SlideDeckVersionDetails", sourceVersion.getId().toString()));
        SlideDeckVersionDetails target = new SlideDeckVersionDetails();
        target.setMaterialVersion(targetVersion);
        if (overrides != null && overrides.slideDeckDetails() != null) {
            target.setSlideCount(overrides.slideDeckDetails().slideCount());
            target.setFileUrl(overrides.slideDeckDetails().fileUrl());
        } else {
            target.setSlideCount(source.getSlideCount());
            target.setFileUrl(source.getFileUrl());
        }
        slideDeckRepository.save(target);
    }

    @Override
    public Object getDetailsResponse(UUID versionId) {
        SlideDeckVersionDetails details = slideDeckRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("SlideDeckVersionDetails", versionId.toString()));
        return new SlideDeckDetailsResponse(details.getSlideCount(), details.getFileUrl());
    }

    @Override
    public PositionType getPositionType() { return PositionType.SLIDE; }

    @Override
    public void validatePosition(int positionValue, UUID versionId) {
        SlideDeckVersionDetails details = slideDeckRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("SlideDeckVersionDetails", versionId.toString()));
        if (positionValue < 1 || positionValue > details.getSlideCount()) {
            throw new InvalidOperationException(
                    "Slide position must be between 1 and " + details.getSlideCount());
        }
    }

    @Override
    public boolean isCompleted(int positionValue, UUID versionId) {
        SlideDeckVersionDetails details = slideDeckRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("SlideDeckVersionDetails", versionId.toString()));
        return positionValue >= details.getSlideCount();
    }
}
