package com.skillovilla.readingmaterial;

import com.skillovilla.readingmaterial.dto.request.*;
import com.skillovilla.readingmaterial.entity.MaterialType;
import com.skillovilla.readingmaterial.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DirectMaterialAccessIT extends BaseIntegrationTest {

    @Autowired private MaterialService materialService;
    @Autowired private PublishService publishService;
    @Autowired private ReaderService readerService;
    @Autowired private AccessService accessService;

    @Test
    void directAccess_grantsAndRevokesVisibility() {
        UUID adminId = UUID.randomUUID();

        var created = materialService.createMaterial(
            new CreateMaterialRequest("Direct PDF", MaterialType.PDF,
                new PdfDetailsRequest(20, "http://direct.com/a.pdf"), null, null, null), adminId);
        UUID materialId = created.id();
        publishService.publishVersion(materialId, created.versions().get(0).id());

        var reader = readerService.createReader(new CreateReaderRequest("DirectReader"));
        UUID readerId = reader.id();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Reader-Id", readerId.toString());
        HttpEntity<Void> req = new HttpEntity<>(headers);

        // Not visible
        ResponseEntity<Map> before = restTemplate.exchange("/api/v1/reader/materials", HttpMethod.GET, req, Map.class);
        assertThat(((Number) before.getBody().get("totalElements")).longValue()).isZero();

        // Grant direct access
        accessService.grantMaterialAccess(readerId, materialId, null);

        // Visible
        ResponseEntity<Map> after = restTemplate.exchange("/api/v1/reader/materials", HttpMethod.GET, req, Map.class);
        assertThat(((Number) after.getBody().get("totalElements")).longValue()).isEqualTo(1);

        // Revoke
        accessService.revokeMaterialAccess(readerId, materialId);

        // Gone
        ResponseEntity<Map> revoked = restTemplate.exchange("/api/v1/reader/materials", HttpMethod.GET, req, Map.class);
        assertThat(((Number) revoked.getBody().get("totalElements")).longValue()).isZero();
    }
}
