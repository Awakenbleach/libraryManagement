package com.skillovilla.readingmaterial.service;

import com.skillovilla.readingmaterial.dto.request.CreateMaterialRequest;
import com.skillovilla.readingmaterial.dto.response.PdfDetailsResponse;
import com.skillovilla.readingmaterial.entity.*;
import com.skillovilla.readingmaterial.exception.InvalidOperationException;
import com.skillovilla.readingmaterial.exception.ResourceNotFoundException;
import com.skillovilla.readingmaterial.repository.PdfVersionDetailsRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PdfDetailService implements TypeDetailService {

    private final PdfVersionDetailsRepository pdfRepository;

    public PdfDetailService(PdfVersionDetailsRepository pdfRepository) {
        this.pdfRepository = pdfRepository;
    }

    @Override
    public MaterialType getSupportedType() { return MaterialType.PDF; }

    @Override
    public void validateRequest(CreateMaterialRequest request) {
        if (request.pdfDetails() == null) {
            throw new InvalidOperationException("PDF details are required for PDF material type");
        }
        // Bean validation handles field-level validation
    }

    @Override
    public void createDetails(MaterialVersion version, CreateMaterialRequest request) {
        PdfVersionDetails details = new PdfVersionDetails();
        details.setMaterialVersion(version);
        details.setPageCount(request.pdfDetails().pageCount());
        details.setFileUrl(request.pdfDetails().fileUrl());
        pdfRepository.save(details);
    }

    @Override
    public void copyDetails(MaterialVersion sourceVersion, MaterialVersion targetVersion, com.skillovilla.readingmaterial.dto.request.CreateVersionRequest overrides) {
        PdfVersionDetails source = pdfRepository.findById(sourceVersion.getId())
                .orElseThrow(() -> new ResourceNotFoundException("PdfVersionDetails", sourceVersion.getId().toString()));
        PdfVersionDetails target = new PdfVersionDetails();
        target.setMaterialVersion(targetVersion);
        if (overrides != null && overrides.pdfDetails() != null) {
            target.setPageCount(overrides.pdfDetails().pageCount());
            target.setFileUrl(overrides.pdfDetails().fileUrl());
        } else {
            target.setPageCount(source.getPageCount());
            target.setFileUrl(source.getFileUrl());
        }
        pdfRepository.save(target);
    }

    @Override
    public Object getDetailsResponse(UUID versionId) {
        PdfVersionDetails details = pdfRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("PdfVersionDetails", versionId.toString()));
        return new PdfDetailsResponse(details.getPageCount(), details.getFileUrl());
    }

    @Override
    public PositionType getPositionType() { return PositionType.PAGE; }

    @Override
    public void validatePosition(int positionValue, UUID versionId) {
        PdfVersionDetails details = pdfRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("PdfVersionDetails", versionId.toString()));
        if (positionValue < 1 || positionValue > details.getPageCount()) {
            throw new InvalidOperationException(
                    "Page position must be between 1 and " + details.getPageCount());
        }
    }

    @Override
    public boolean isCompleted(int positionValue, UUID versionId) {
        PdfVersionDetails details = pdfRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("PdfVersionDetails", versionId.toString()));
        return positionValue >= details.getPageCount();
    }
}
