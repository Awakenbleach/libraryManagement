package com.skillovilla.readingmaterial;

import com.skillovilla.readingmaterial.dto.request.*;
import com.skillovilla.readingmaterial.entity.MaterialType;
import com.skillovilla.readingmaterial.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SeriesProgressIT extends BaseIntegrationTest {

    @Autowired private MaterialService materialService;
    @Autowired private PublishService publishService;
    @Autowired private ReaderService readerService;
    @Autowired private AccessService accessService;
    @Autowired private ProgressService progressService;
    @Autowired private SeriesService seriesService;

    @Test
    void seriesProgress_derivedFromParts() {
        UUID adminId = UUID.randomUUID();

        // Create 4 PDF materials, each with pageCount=10
        UUID[] materialIds = new UUID[4];
        UUID[] versionIds = new UUID[4];
        for (int i = 0; i < 4; i++) {
            var created = materialService.createMaterial(
                new CreateMaterialRequest("Series Part " + (i+1), MaterialType.PDF,
                    new PdfDetailsRequest(10, "http://series.com/" + i + ".pdf"), null, null, null), adminId);
            materialIds[i] = created.id();
            versionIds[i] = created.versions().get(0).id();
            publishService.publishVersion(materialIds[i], versionIds[i]);
        }

        // Create reader with access to all 4
        var reader = readerService.createReader(new CreateReaderRequest("SeriesReader"));
        UUID readerId = reader.id();
        for (UUID mid : materialIds) {
            accessService.grantMaterialAccess(readerId, mid, null);
        }

        // Create series and add all 4 parts
        var series = seriesService.createSeries(new CreateSeriesRequest("Test Series"));
        UUID seriesId = series.id();
        for (int i = 0; i < 4; i++) {
            seriesService.addPart(seriesId, new AddSeriesPartRequest(materialIds[i], i + 1));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Reader-Id", readerId.toString());
        HttpEntity<Void> req = new HttpEntity<>(headers);

        // Check series — 0/4 completed
        ResponseEntity<Map> r0 = restTemplate.exchange(
            "/api/v1/reader/series/" + seriesId, HttpMethod.GET, req, Map.class);
        assertThat(r0.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((Number) r0.getBody().get("completedParts")).intValue()).isEqualTo(0);
        assertThat((Boolean) r0.getBody().get("finished")).isFalse();

        // Complete parts 1 and 2 (positionValue=10 = page_count = finished for PDF)
        progressService.updateProgress(readerId, materialIds[0],
            new UpdateProgressRequest(10, null, versionIds[0]));
        progressService.updateProgress(readerId, materialIds[1],
            new UpdateProgressRequest(10, null, versionIds[1]));

        // Check series — 2/4
        ResponseEntity<Map> r2 = restTemplate.exchange(
            "/api/v1/reader/series/" + seriesId, HttpMethod.GET, req, Map.class);
        assertThat(((Number) r2.getBody().get("completedParts")).intValue()).isEqualTo(2);
        assertThat(((Number) r2.getBody().get("totalParts")).intValue()).isEqualTo(4);
        assertThat((Boolean) r2.getBody().get("finished")).isFalse();

        // Complete remaining parts
        progressService.updateProgress(readerId, materialIds[2],
            new UpdateProgressRequest(10, null, versionIds[2]));
        progressService.updateProgress(readerId, materialIds[3],
            new UpdateProgressRequest(10, null, versionIds[3]));

        // Check series — 4/4 finished
        ResponseEntity<Map> r4 = restTemplate.exchange(
            "/api/v1/reader/series/" + seriesId, HttpMethod.GET, req, Map.class);
        assertThat(((Number) r4.getBody().get("completedParts")).intValue()).isEqualTo(4);
        assertThat((Boolean) r4.getBody().get("finished")).isTrue();
    }
}
