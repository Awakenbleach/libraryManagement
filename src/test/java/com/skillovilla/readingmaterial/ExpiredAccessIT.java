package com.skillovilla.readingmaterial;

import com.skillovilla.readingmaterial.dto.request.*;
import com.skillovilla.readingmaterial.entity.MaterialType;
import com.skillovilla.readingmaterial.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ExpiredAccessIT extends BaseIntegrationTest {

    @Autowired private MaterialService materialService;
    @Autowired private PublishService publishService;
    @Autowired private ReaderService readerService;
    @Autowired private AccessService accessService;

    @Test
    void expiredAccess_deniesAccess_todayBoundaryIsInclusive() {
        UUID adminId = UUID.randomUUID();

        var created = materialService.createMaterial(
            new CreateMaterialRequest("Expiry PDF", MaterialType.PDF,
                new PdfDetailsRequest(10, "http://exp.com/a.pdf"), null, null, null), adminId);
        UUID materialId = created.id();
        publishService.publishVersion(materialId, created.versions().get(0).id());

        var reader = readerService.createReader(new CreateReaderRequest("ExpiryReader"));
        UUID readerId = reader.id();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Reader-Id", readerId.toString());
        HttpEntity<Void> req = new HttpEntity<>(headers);

        // Grant with YESTERDAY as end date — already expired
        accessService.grantMaterialAccess(readerId, materialId,
            new GrantAccessRequest(LocalDate.now().minusDays(1)));

        ResponseEntity<Map> expired = restTemplate.exchange("/api/v1/reader/materials", HttpMethod.GET, req, Map.class);
        assertThat(((Number) expired.getBody().get("totalElements")).longValue()).isZero();

        // Revoke and re-grant with TODAY as end date — still valid (inclusive)
        accessService.revokeMaterialAccess(readerId, materialId);
        accessService.grantMaterialAccess(readerId, materialId,
            new GrantAccessRequest(LocalDate.now()));

        ResponseEntity<Map> today = restTemplate.exchange("/api/v1/reader/materials", HttpMethod.GET, req, Map.class);
        assertThat(((Number) today.getBody().get("totalElements")).longValue()).isEqualTo(1);
    }
}
