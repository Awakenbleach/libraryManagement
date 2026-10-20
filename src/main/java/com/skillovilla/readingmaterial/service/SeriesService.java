package com.skillovilla.readingmaterial.service;

import com.skillovilla.readingmaterial.dto.request.AddSeriesPartRequest;
import com.skillovilla.readingmaterial.dto.request.CreateSeriesRequest;
import com.skillovilla.readingmaterial.dto.response.SeriesPartResponse;
import com.skillovilla.readingmaterial.dto.response.SeriesResponse;
import com.skillovilla.readingmaterial.entity.*;
import com.skillovilla.readingmaterial.exception.InvalidOperationException;
import com.skillovilla.readingmaterial.exception.ResourceNotFoundException;
import com.skillovilla.readingmaterial.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SeriesService {

    private final SeriesRepository seriesRepository;
    private final SeriesPartRepository seriesPartRepository;
    private final MaterialRepository materialRepository;
    private final MaterialVersionRepository materialVersionRepository;
    private final ReadingProgressRepository readingProgressRepository;
    private final ReaderAccessService readerAccessService;

    public SeriesService(SeriesRepository seriesRepository,
                         SeriesPartRepository seriesPartRepository,
                         MaterialRepository materialRepository,
                         MaterialVersionRepository materialVersionRepository,
                         ReadingProgressRepository readingProgressRepository,
                         ReaderAccessService readerAccessService) {
        this.seriesRepository = seriesRepository;
        this.seriesPartRepository = seriesPartRepository;
        this.materialRepository = materialRepository;
        this.materialVersionRepository = materialVersionRepository;
        this.readingProgressRepository = readingProgressRepository;
        this.readerAccessService = readerAccessService;
    }

    @Transactional
    public SeriesResponse createSeries(CreateSeriesRequest request) {
        Series series = new Series();
        series.setTitle(request.title());
        series = seriesRepository.save(series);

        return new SeriesResponse(
                series.getId(),
                series.getTitle(),
                series.getCreatedAt(),
                List.of(),
                0,
                0,
                false
        );
    }

    @Transactional
    public void addPart(UUID seriesId, AddSeriesPartRequest request) {
        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new ResourceNotFoundException("Series", seriesId.toString()));

        Material material = materialRepository.findById(request.materialId())
                .orElseThrow(() -> new ResourceNotFoundException("Material", request.materialId().toString()));

        if (seriesPartRepository.existsBySeriesIdAndMaterialId(seriesId, request.materialId())) {
            throw new InvalidOperationException("Material is already part of this series");
        }

        if (seriesPartRepository.existsBySeriesIdAndPartOrder(seriesId, request.partOrder())) {
            throw new InvalidOperationException("Part order " + request.partOrder() + " is already taken in this series");
        }

        SeriesPart part = new SeriesPart();
        part.setSeries(series);
        part.setMaterial(material);
        part.setPartOrder(request.partOrder());
        seriesPartRepository.save(part);
    }

    public SeriesResponse getSeriesWithProgress(UUID seriesId, UUID readerId) {
        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new ResourceNotFoundException("Series", seriesId.toString()));

        List<SeriesPart> parts = seriesPartRepository.findBySeriesIdOrderByPartOrderAsc(seriesId);

        List<SeriesPartResponse> partResponses = new ArrayList<>();
        int completedCount = 0;

        for (SeriesPart part : parts) {
            Material material = part.getMaterial();
            boolean isCompleted = false;

            if (readerId != null) {
                Optional<MaterialVersion> liveVersionOpt = materialVersionRepository
                        .findByMaterialIdAndStatus(material.getId(), VersionStatus.LIVE);

                if (liveVersionOpt.isPresent()) {
                    Optional<ReadingProgress> progressOpt = readingProgressRepository
                            .findByReaderIdAndMaterialVersionId(readerId, liveVersionOpt.get().getId());
                    if (progressOpt.isPresent() && progressOpt.get().isCompleted()) {
                        isCompleted = true;
                    }
                }
            }

            if (isCompleted) {
                completedCount++;
            }

            partResponses.add(new SeriesPartResponse(
                    part.getPartOrder(),
                    material.getId(),
                    material.getTitle(),
                    isCompleted
            ));
        }

        int totalParts = parts.size();
        boolean finished = (totalParts > 0 && completedCount == totalParts);

        return new SeriesResponse(
                series.getId(),
                series.getTitle(),
                series.getCreatedAt(),
                partResponses,
                totalParts,
                completedCount,
                finished
        );
    }
}
