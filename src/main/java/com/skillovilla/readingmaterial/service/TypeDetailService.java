package com.skillovilla.readingmaterial.service;

import com.skillovilla.readingmaterial.dto.request.CreateMaterialRequest;
import com.skillovilla.readingmaterial.dto.request.CreateVersionRequest;
import com.skillovilla.readingmaterial.entity.MaterialType;
import com.skillovilla.readingmaterial.entity.MaterialVersion;
import com.skillovilla.readingmaterial.entity.PositionType;

import java.util.UUID;

public interface TypeDetailService {
    MaterialType getSupportedType();
    void validateRequest(CreateMaterialRequest request);
    void createDetails(MaterialVersion version, CreateMaterialRequest request);
    void copyDetails(MaterialVersion sourceVersion, MaterialVersion targetVersion, CreateVersionRequest overrides);
    Object getDetailsResponse(UUID versionId);
    PositionType getPositionType();
    void validatePosition(int positionValue, UUID versionId);
    boolean isCompleted(int positionValue, UUID versionId);
}

