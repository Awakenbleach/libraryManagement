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

class TypeSpecificSearchIT extends BaseIntegrationTest {

    @Autowired private MaterialService materialService;
    @Autowired private PublishService publishService;
    @Autowired private ReaderService readerService;
    @Autowired private AccessService accessService;

    @Test
    void search_byTypeAndMinPageCount_returnsOnlyAccessibleAndMatchingMaterials() {
        UUID adminId = UUID.randomUUID();

        // Create 3 PDF materials: pageCount = 50, 150, 200
        var pdf50 = materialService.createMaterial(
            new CreateMaterialRequest("PDF 50 pages", MaterialType.PDF,
                new PdfDetailsRequest(50, "http://search.com/50.pdf"),
                null, null, null), adminId);
        publishService.publishVersion(pdf50.id(), pdf50.versions().get(0).id());

        var pdf150 = materialService.createMaterial(
            new CreateMaterialRequest("PDF 150 pages", MaterialType.PDF,
                new PdfDetailsRequest(150, "http://search.com/150.pdf"),
                null, null, null), adminId);
        publishService.publishVersion(pdf150.id(), pdf150.versions().get(0).id());

        var pdf200 = materialService.createMaterial(
            new CreateMaterialRequest("PDF 200 pages", MaterialType.PDF,
                new PdfDetailsRequest(200, "http://search.com/200.pdf"),
                null, null, null), adminId);
        publishService.publishVersion(pdf200.id(), pdf200.versions().get(0).id());

        // Also create 1 ARTICLE with 2000 words
        var article = materialService.createMaterial(
            new CreateMaterialRequest("Search Article", MaterialType.ARTICLE,
                null, new ArticleDetailsRequest(2000), null, null), adminId);
        publishService.publishVersion(article.id(), article.versions().get(0).id());

        // Reader 1: granted access to pdf50, pdf150, and article (NOT pdf200)
        var r1 = readerService.createReader(new CreateReaderRequest("SearchReader1"));
        accessService.grantMaterialAccess(r1.id(), pdf50.id(), null);
        accessService.grantMaterialAccess(r1.id(), pdf150.id(), null);
        accessService.grantMaterialAccess(r1.id(), article.id(), null);

        HttpHeaders h1 = new HttpHeaders();
        h1.set("X-Reader-Id", r1.id().toString());

        // 1. Search type=PDF & minPageCount=100 for Reader 1:
        // Accessible PDFs are pdf50 and pdf150.
        // Among them, only pdf150 satisfies pageCount >= 100.
        // pdf200 satisfies pageCount >= 100 but Reader 1 has NO access.
        // Therefore, result count must be exactly 1!
        ResponseEntity<Map> pdfSearch = restTemplate.exchange(
            "/api/v1/reader/materials/search?type=PDF&minPageCount=100",
            HttpMethod.GET, new HttpEntity<>(h1), Map.class);
        assertThat(pdfSearch.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((Number) pdfSearch.getBody().get("totalElements")).longValue()).isEqualTo(1);

        // 2. Reader 2 with NO access at all searching same query gets 0 results
        var r2 = readerService.createReader(new CreateReaderRequest("SearchReader2"));
        HttpHeaders h2 = new HttpHeaders();
        h2.set("X-Reader-Id", r2.id().toString());
        ResponseEntity<Map> noAccess = restTemplate.exchange(
            "/api/v1/reader/materials/search?type=PDF&minPageCount=100",
            HttpMethod.GET, new HttpEntity<>(h2), Map.class);
        assertThat(noAccess.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((Number) noAccess.getBody().get("totalElements")).longValue()).isZero();

        // 3. Search type=ARTICLE for Reader 1 gets 1 result
        ResponseEntity<Map> articleSearch = restTemplate.exchange(
            "/api/v1/reader/materials/search?type=ARTICLE",
            HttpMethod.GET, new HttpEntity<>(h1), Map.class);
        assertThat(articleSearch.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((Number) articleSearch.getBody().get("totalElements")).longValue()).isEqualTo(1);
    }
}
