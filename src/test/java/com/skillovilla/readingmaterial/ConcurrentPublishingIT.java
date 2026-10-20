package com.skillovilla.readingmaterial;

import com.skillovilla.readingmaterial.dto.request.*;
import com.skillovilla.readingmaterial.entity.*;
import com.skillovilla.readingmaterial.repository.MaterialVersionRepository;
import com.skillovilla.readingmaterial.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ConcurrentPublishingIT extends BaseIntegrationTest {

    @Autowired private MaterialService materialService;
    @Autowired private PublishService publishService;
    @Autowired private VersionService versionService;
    @Autowired private MaterialVersionRepository materialVersionRepository;

    @Test
    void concurrentPublish_onlyOneLiveVersionSurvives() throws Exception {
        UUID adminId = UUID.randomUUID();

        // Create PDF material — starts as V1 DRAFT
        var created = materialService.createMaterial(
            new CreateMaterialRequest("Concurrent PDF", MaterialType.PDF,
                new PdfDetailsRequest(100, "http://test.com/a.pdf"), null, null, null),
            adminId);
        UUID materialId = created.id();
        UUID v1Id = created.versions().get(0).id();

        // Publish V1
        publishService.publishVersion(materialId, v1Id);

        // Create V2 and V3
        UUID v2Id = versionService.createVersionFromCopy(materialId,
            new CreateVersionRequest(v1Id, null, null, null, null), adminId).id();
        UUID v3Id = versionService.createVersionFromCopy(materialId,
            new CreateVersionRequest(v1Id, null, null, null, null), adminId).id();

        // Fire both publish requests concurrently
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(() -> {
            try { publishService.publishVersion(materialId, v2Id); successCount.incrementAndGet(); }
            catch (Exception e) { failCount.incrementAndGet(); }
            finally { latch.countDown(); }
        });
        executor.submit(() -> {
            try { publishService.publishVersion(materialId, v3Id); successCount.incrementAndGet(); }
            catch (Exception e) { failCount.incrementAndGet(); }
            finally { latch.countDown(); }
        });

        assertThat(latch.await(15, TimeUnit.SECONDS)).isTrue();
        executor.shutdown();

        // Assert exactly ONE LIVE version
        long liveCount = materialVersionRepository.findByMaterialIdOrderByVersionNumberDesc(materialId)
            .stream().filter(v -> v.getStatus() == VersionStatus.LIVE).count();

        assertThat(liveCount).isEqualTo(1);
        assertThat(successCount.get() + failCount.get()).isEqualTo(2);
    }
}
