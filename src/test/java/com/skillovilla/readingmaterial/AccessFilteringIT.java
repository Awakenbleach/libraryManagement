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

class AccessFilteringIT extends BaseIntegrationTest {

    @Autowired private MaterialService materialService;
    @Autowired private PublishService publishService;
    @Autowired private ReaderService readerService;

    @Test
    void readerWithNoAccess_cannotSeeMaterial_anywhere() {
        UUID adminId = UUID.randomUUID();

        // Create and publish PDF
        var created = materialService.createMaterial(
            new CreateMaterialRequest("Secret PDF", MaterialType.PDF,
                new PdfDetailsRequest(50, "http://secret.com/a.pdf"), null, null, null), adminId);
        UUID materialId = created.id();
        publishService.publishVersion(materialId, created.versions().get(0).id());

        // Reader with NO access
        var reader = readerService.createReader(new CreateReaderRequest("NoAccessReader"));
        UUID readerId = reader.id();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Reader-Id", readerId.toString());
        HttpEntity<Void> req = new HttpEntity<>(headers);

        // List — should return empty page
        ResponseEntity<Map> listResp = restTemplate.exchange(
            "/api/v1/reader/materials", HttpMethod.GET, req, Map.class);
        assertThat(listResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((Number) listResp.getBody().get("totalElements")).longValue()).isZero();

        // Search — empty
        ResponseEntity<Map> searchResp = restTemplate.exchange(
            "/api/v1/reader/materials/search", HttpMethod.GET, req, Map.class);
        assertThat(searchResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((Number) searchResp.getBody().get("totalElements")).longValue()).isZero();

        // Open by ID — 403
        ResponseEntity<Map> openResp = restTemplate.exchange(
            "/api/v1/reader/materials/" + materialId, HttpMethod.GET, req, Map.class);
        assertThat(openResp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Resume — 403
        ResponseEntity<Map> resumeResp = restTemplate.exchange(
            "/api/v1/reader/materials/" + materialId + "/resume", HttpMethod.GET, req, Map.class);
        assertThat(resumeResp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
