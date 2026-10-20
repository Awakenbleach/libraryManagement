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

class DraftInvisibilityIT extends BaseIntegrationTest {

    @Autowired private MaterialService materialService;
    @Autowired private ReaderService readerService;
    @Autowired private AccessService accessService;

    @Test
    void draftOnlyMaterial_isCompletelyInvisibleToReaders() {
        UUID adminId = UUID.randomUUID();

        // Create PDF but do NOT publish
        var created = materialService.createMaterial(
            new CreateMaterialRequest("Draft Only PDF", MaterialType.PDF,
                new PdfDetailsRequest(10, "http://draft.com/a.pdf"), null, null, null), adminId);
        UUID materialId = created.id();
        // intentionally NOT publishing

        // Create reader with direct access (even with access, draft is invisible)
        var reader = readerService.createReader(new CreateReaderRequest("DraftReader"));
        UUID readerId = reader.id();
        accessService.grantMaterialAccess(readerId, materialId, null);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Reader-Id", readerId.toString());
        HttpEntity<Void> req = new HttpEntity<>(headers);

        // List — empty
        ResponseEntity<Map> list = restTemplate.exchange("/api/v1/reader/materials", HttpMethod.GET, req, Map.class);
        assertThat(((Number) list.getBody().get("totalElements")).longValue()).isZero();

        // Search — empty
        ResponseEntity<Map> search = restTemplate.exchange("/api/v1/reader/materials/search", HttpMethod.GET, req, Map.class);
        assertThat(((Number) search.getBody().get("totalElements")).longValue()).isZero();

        // Open by ID — 404 or 403 (no live version, so ResourceNotFoundException maps to 404)
        ResponseEntity<Map> open = restTemplate.exchange(
            "/api/v1/reader/materials/" + materialId, HttpMethod.GET, req, Map.class);
        // The service checks access first, then finds LIVE version
        // Since draft-only material has no LIVE version, toMaterialResponse will throw ResourceNotFoundException
        // This results in 403 (access exists) or 404 (no live version found)
        assertThat(open.getStatusCode().value()).isIn(403, 404);
    }
}
