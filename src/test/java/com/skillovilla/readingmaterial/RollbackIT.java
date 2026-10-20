package com.skillovilla.readingmaterial;

import com.skillovilla.readingmaterial.dto.request.*;
import com.skillovilla.readingmaterial.entity.*;
import com.skillovilla.readingmaterial.repository.MaterialVersionRepository;
import com.skillovilla.readingmaterial.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RollbackIT extends BaseIntegrationTest {

    @Autowired private MaterialService materialService;
    @Autowired private PublishService publishService;
    @Autowired private VersionService versionService;
    @Autowired private MaterialVersionRepository materialVersionRepository;

    @Test
    void publishOldVersion_archivesNewerVersions_noVersionsDeleted() {
        UUID adminId = UUID.randomUUID();

        // Create PDF material — V1 DRAFT
        var created = materialService.createMaterial(
            new CreateMaterialRequest("Rollback PDF", MaterialType.PDF,
                new PdfDetailsRequest(50, "http://test.com/roll.pdf"), null, null, null),
            adminId);
        UUID materialId = created.id();
        UUID v1Id = created.versions().get(0).id();

        // Publish V1
        publishService.publishVersion(materialId, v1Id);

        // Create and publish V2
        UUID v2Id = versionService.createVersionFromCopy(materialId,
            new CreateVersionRequest(v1Id, null, null, null, null), adminId).id();
        publishService.publishVersion(materialId, v2Id);

        // Create and publish V3
        UUID v3Id = versionService.createVersionFromCopy(materialId,
            new CreateVersionRequest(v2Id, null, null, null, null), adminId).id();
        publishService.publishVersion(materialId, v3Id);

        // Rollback: publish V1 again
        publishService.publishVersion(materialId, v1Id);

        // Assert
        List<MaterialVersion> versions = materialVersionRepository.findByMaterialIdOrderByVersionNumberDesc(materialId);
        assertThat(versions).hasSize(3); // No deletions

        MaterialVersion v1 = versions.stream().filter(v -> v.getId().equals(v1Id)).findFirst().orElseThrow();
        MaterialVersion v2 = versions.stream().filter(v -> v.getId().equals(v2Id)).findFirst().orElseThrow();
        MaterialVersion v3 = versions.stream().filter(v -> v.getId().equals(v3Id)).findFirst().orElseThrow();

        assertThat(v1.getStatus()).isEqualTo(VersionStatus.LIVE);
        assertThat(v2.getStatus()).isEqualTo(VersionStatus.ARCHIVED);
        assertThat(v3.getStatus()).isEqualTo(VersionStatus.ARCHIVED);
    }
}
