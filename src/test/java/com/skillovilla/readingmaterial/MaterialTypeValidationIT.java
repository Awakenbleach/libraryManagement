package com.skillovilla.readingmaterial;

import com.skillovilla.readingmaterial.dto.request.*;
import com.skillovilla.readingmaterial.entity.MaterialType;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MaterialTypeValidationIT extends BaseIntegrationTest {

    @Test
    void pdfWithoutDetails_returns400() {
        Map<String, Object> body = Map.of("title", "Bad PDF", "materialType", "PDF");
        ResponseEntity<Map> resp = restTemplate.postForEntity("/api/v1/admin/materials", body, Map.class);
        // pdfDetails null => INVALID_OPERATION from PdfDetailService.validateRequest
        assertThat(resp.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void pdfWithZeroPageCount_returns400() {
        Map<String, Object> body = Map.of(
            "title", "Zero PDF", "materialType", "PDF",
            "pdfDetails", Map.of("pageCount", 0, "fileUrl", "http://x.com"));
        ResponseEntity<Map> resp = restTemplate.postForEntity("/api/v1/admin/materials", body, Map.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void articleWithoutWordCount_returns400() {
        Map<String, Object> body = Map.of(
            "title", "Bad Article", "materialType", "ARTICLE",
            "articleDetails", Map.of());
        ResponseEntity<Map> resp = restTemplate.postForEntity("/api/v1/admin/materials", body, Map.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void bookWithoutChapters_returns400() {
        Map<String, Object> body = Map.of(
            "title", "No Chapters", "materialType", "BOOK",
            "bookDetails", Map.of("author", "Author X", "chapters", List.of()));
        ResponseEntity<Map> resp = restTemplate.postForEntity("/api/v1/admin/materials", body, Map.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void slideDeckWithNegativeSlideCount_returns400() {
        Map<String, Object> body = Map.of(
            "title", "Bad Deck", "materialType", "SLIDE_DECK",
            "slideDeckDetails", Map.of("slideCount", -1, "fileUrl", "http://x.com"));
        ResponseEntity<Map> resp = restTemplate.postForEntity("/api/v1/admin/materials", body, Map.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(400);
    }
}
