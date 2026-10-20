package com.skillovilla.readingmaterial;

import com.skillovilla.readingmaterial.dto.request.*;
import com.skillovilla.readingmaterial.entity.*;
import com.skillovilla.readingmaterial.repository.*;
import com.skillovilla.readingmaterial.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class ProgressSurvivesVersionChangeIT extends BaseIntegrationTest {

    @Autowired private MaterialService materialService;
    @Autowired private PublishService publishService;
    @Autowired private VersionService versionService;
    @Autowired private ReaderService readerService;
    @Autowired private AccessService accessService;
    @Autowired private ProgressService progressService;
    @Autowired private ReadingProgressRepository readingProgressRepository;
    @Autowired private MaterialVersionRepository materialVersionRepository;

    @Test
    void progressOnV1_remainsAfterV2Published() {
        UUID adminId = UUID.randomUUID();

        // Setup: PDF material V1
        var created = materialService.createMaterial(
            new CreateMaterialRequest("Progress Test PDF", MaterialType.PDF,
                new PdfDetailsRequest(100, "http://test.com/prog.pdf"), null, null, null),
            adminId);
        UUID materialId = created.id();
        UUID v1Id = created.versions().get(0).id();
        publishService.publishVersion(materialId, v1Id);

        // Create reader with access
        var reader = readerService.createReader(new CreateReaderRequest("TestReader"));
        UUID readerId = reader.id();
        accessService.grantMaterialAccess(readerId, materialId, null);

        // Update progress on V1
        progressService.updateProgress(readerId, materialId,
            new UpdateProgressRequest(25, null, v1Id));

        // Publish V2
        UUID v2Id = versionService.createVersionFromCopy(materialId,
            new CreateVersionRequest(v1Id, null, null, null, null), adminId).id();
        publishService.publishVersion(materialId, v2Id);

        // Assert V1 progress still exists
        var v1Progress = readingProgressRepository.findByReaderIdAndMaterialVersionId(readerId, v1Id);
        assertThat(v1Progress).isPresent();
        assertThat(v1Progress.get().getPositionValue()).isEqualTo(25);
        assertThat(v1Progress.get().getMaterialVersion().getId()).isEqualTo(v1Id);

        // Assert V2 has no progress yet
        var v2Progress = readingProgressRepository.findByReaderIdAndMaterialVersionId(readerId, v2Id);
        assertThat(v2Progress).isEmpty();

        // Call resume API and check progressOnDifferentVersion
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Reader-Id", readerId.toString());
        ResponseEntity<Map> resumeResp = restTemplate.exchange(
            "/api/v1/reader/materials/" + materialId + "/resume",
            HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        assertThat(resumeResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resumeResp.getBody()).isNotNull();
        assertThat((Boolean) resumeResp.getBody().get("progressOnDifferentVersion")).isTrue();
    }
}
