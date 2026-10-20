package com.skillovilla.readingmaterial.repository;

import com.skillovilla.readingmaterial.entity.ReadingProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReadingProgressRepository extends JpaRepository<ReadingProgress, UUID> {
    Optional<ReadingProgress> findByReaderIdAndMaterialVersionId(UUID readerId, UUID materialVersionId);
    List<ReadingProgress> findByReaderIdAndMaterialVersionIdIn(UUID readerId, List<UUID> materialVersionIds);
    
    @Modifying
    @Query("UPDATE ReadingProgress rp SET rp.positionValue = :positionValue, rp.completed = :completed, rp.progressRevision = rp.progressRevision + 1, rp.updatedAt = CURRENT_TIMESTAMP WHERE rp.id = :id AND rp.progressRevision = :expectedRevision")
    int updateProgressOptimistic(@Param("id") UUID id, @Param("positionValue") int positionValue, @Param("completed") boolean completed, @Param("expectedRevision") long expectedRevision);
}
