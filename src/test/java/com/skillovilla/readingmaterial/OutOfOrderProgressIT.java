package com.skillovilla.readingmaterial;

import com.skillovilla.readingmaterial.dto.request.*;
import com.skillovilla.readingmaterial.dto.response.ProgressResponse;
import com.skillovilla.readingmaterial.entity.*;
import com.skillovilla.readingmaterial.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OutOfOrderProgressIT extends BaseIntegrationTest {

    @Autowired private MaterialService materialService;
    @Autowired private PublishService publishService;
    @Autowired private ReaderService readerService;
    @Autowired private AccessService accessService;
    @Autowired private ProgressService progressService;

    @Test
    void staleProgressUpdate_returns409() {
        UUID adminId = UUID.randomUUID();

        // Setup PDF material
        var created = materialService.createMaterial(
            new CreateMaterialRequest("Out-of-Order PDF", MaterialType.PDF,
                new PdfDetailsRequest(100, "http://test.com/oo.pdf"), null, null, null), adminId);
        UUID materialId = created.id();
        UUID v1Id = created.versions().get(0).id();
        publishService.publishVersion(materialId, v1Id);

        // Create reader with access
        var reader = readerService.createReader(new CreateReaderRequest("OOReader"));
        UUID readerId = reader.id();
        accessService.grantMaterialAccess(readerId, materialId, null);

        // First progress (revision 1 created)
        ProgressResponse p1 = progressService.updateProgress(readerId, materialId,
            new UpdateProgressRequest(10, null, v1Id));
        assertThat(p1.progressRevision()).isEqualTo(1);

        // Advance to revision 2 (phone)
        ProgressResponse p2 = progressService.updateProgress(readerId, materialId,
            new UpdateProgressRequest(40, 1L, v1Id));
        assertThat(p2.progressRevision()).isEqualTo(2);
        assertThat(p2.positionValue()).isEqualTo(40);

        // Stale update from laptop (expectedRevision=1, but current is 2) — via API
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Reader-Id", readerId.toString());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of("positionValue", 12, "expectedRevision", 1);
        ResponseEntity<Map> staleResp = restTemplate.exchange(
            "/api/v1/reader/materials/" + materialId + "/progress",
            HttpMethod.PUT,
            new HttpEntity<>(body, headers),
            Map.class);

        assertThat(staleResp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // Newer progress still intact
        ProgressResponse current = progressService.updateProgress(readerId, materialId,
            new UpdateProgressRequest(50, 2L, v1Id));
        assertThat(current.positionValue()).isEqualTo(50);
        assertThat(current.progressRevision()).isEqualTo(3);
    }
}
