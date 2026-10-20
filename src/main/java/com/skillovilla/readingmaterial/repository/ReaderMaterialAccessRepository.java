package com.skillovilla.readingmaterial.repository;

import com.skillovilla.readingmaterial.entity.ReaderMaterialAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReaderMaterialAccessRepository extends JpaRepository<ReaderMaterialAccess, UUID> {
    Optional<ReaderMaterialAccess> findByReaderIdAndMaterialId(UUID readerId, UUID materialId);
    List<ReaderMaterialAccess> findByReaderId(UUID readerId);
    boolean existsByReaderIdAndMaterialId(UUID readerId, UUID materialId);

    @org.springframework.data.jpa.repository.Query("SELECT CASE WHEN COUNT(rma) > 0 THEN true ELSE false END FROM ReaderMaterialAccess rma " +
           "WHERE rma.reader.id = :readerId AND rma.material.id = :materialId " +
           "AND (rma.accessEndDate IS NULL OR rma.accessEndDate >= :today)")
    boolean hasValidAccess(@org.springframework.data.repository.query.Param("readerId") UUID readerId, @org.springframework.data.repository.query.Param("materialId") UUID materialId, @org.springframework.data.repository.query.Param("today") java.time.LocalDate today);
}
