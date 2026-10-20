package com.skillovilla.readingmaterial;

import com.skillovilla.readingmaterial.dto.request.*;
import com.skillovilla.readingmaterial.dto.response.CollectionResponse;
import com.skillovilla.readingmaterial.entity.MaterialType;
import com.skillovilla.readingmaterial.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CollectionAccessIT extends BaseIntegrationTest {

    @Autowired private MaterialService materialService;
    @Autowired private PublishService publishService;
    @Autowired private ReaderService readerService;
    @Autowired private CollectionService collectionService;
    @Autowired private AccessService accessService;

    @Test
    void collectionAccess_grantsAndRevokesVisibility() {
        UUID adminId = UUID.randomUUID();

        // Create PDF + publish
        var created = materialService.createMaterial(
            new CreateMaterialRequest("Coll PDF", MaterialType.PDF,
                new PdfDetailsRequest(30, "http://coll.com/a.pdf"), null, null, null), adminId);
        UUID materialId = created.id();
        publishService.publishVersion(materialId, created.versions().get(0).id());

        // Create collection and add material
        CollectionResponse coll = collectionService.createCollection(new CreateCollectionRequest("My Collection"));
        UUID collectionId = coll.id();
        collectionService.addMaterialToCollection(collectionId, materialId);

        // Reader with NO access
        var reader = readerService.createReader(new CreateReaderRequest("CollReader"));
        UUID readerId = reader.id();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Reader-Id", readerId.toString());
        HttpEntity<Void> req = new HttpEntity<>(headers);

        // Not visible before access grant
        ResponseEntity<Map> before = restTemplate.exchange("/api/v1/reader/materials", HttpMethod.GET, req, Map.class);
        assertThat(((Number) before.getBody().get("totalElements")).longValue()).isZero();

        // Grant collection access
        accessService.grantCollectionAccess(readerId, collectionId, null);

        // Now visible
        ResponseEntity<Map> after = restTemplate.exchange("/api/v1/reader/materials", HttpMethod.GET, req, Map.class);
        assertThat(((Number) after.getBody().get("totalElements")).longValue()).isEqualTo(1);

        // Revoke collection access
        accessService.revokeCollectionAccess(readerId, collectionId);

        // No longer visible
        ResponseEntity<Map> revoked = restTemplate.exchange("/api/v1/reader/materials", HttpMethod.GET, req, Map.class);
        assertThat(((Number) revoked.getBody().get("totalElements")).longValue()).isZero();
    }
}
