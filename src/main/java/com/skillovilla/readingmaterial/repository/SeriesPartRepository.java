package com.skillovilla.readingmaterial.repository;

import com.skillovilla.readingmaterial.entity.SeriesPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeriesPartRepository extends JpaRepository<SeriesPart, UUID> {
    List<SeriesPart> findBySeriesIdOrderByPartOrderAsc(UUID seriesId);
    Optional<SeriesPart> findTopBySeriesIdOrderByPartOrderDesc(UUID seriesId);
    boolean existsBySeriesIdAndMaterialId(UUID seriesId, UUID materialId);
    boolean existsBySeriesIdAndPartOrder(UUID seriesId, Integer partOrder);
}

