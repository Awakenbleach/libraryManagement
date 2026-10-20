package com.skillovilla.readingmaterial.repository;

import com.skillovilla.readingmaterial.entity.ReaderCollectionAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReaderCollectionAccessRepository extends JpaRepository<ReaderCollectionAccess, UUID> {
    Optional<ReaderCollectionAccess> findByReaderIdAndCollectionId(UUID readerId, UUID collectionId);
    List<ReaderCollectionAccess> findByReaderId(UUID readerId);
    boolean existsByReaderIdAndCollectionId(UUID readerId, UUID collectionId);

    @org.springframework.data.jpa.repository.Query("SELECT CASE WHEN COUNT(rca) > 0 THEN true ELSE false END FROM ReaderCollectionAccess rca " +
           "WHERE rca.reader.id = :readerId " +
           "AND rca.collection.id IN (SELECT cm.collection.id FROM CollectionMaterial cm WHERE cm.material.id = :materialId) " +
           "AND (rca.accessEndDate IS NULL OR rca.accessEndDate >= :today)")
    boolean hasValidCollectionAccess(@org.springframework.data.repository.query.Param("readerId") UUID readerId, @org.springframework.data.repository.query.Param("materialId") UUID materialId, @org.springframework.data.repository.query.Param("today") java.time.LocalDate today);
}
