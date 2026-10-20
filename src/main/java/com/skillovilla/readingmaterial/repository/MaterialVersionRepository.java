package com.skillovilla.readingmaterial.repository;

import com.skillovilla.readingmaterial.entity.MaterialVersion;
import com.skillovilla.readingmaterial.entity.VersionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MaterialVersionRepository extends JpaRepository<MaterialVersion, UUID> {
    Optional<MaterialVersion> findByMaterialIdAndStatus(UUID materialId, VersionStatus status);
    List<MaterialVersion> findByMaterialIdOrderByVersionNumberDesc(UUID materialId);
    Optional<MaterialVersion> findTopByMaterialIdOrderByVersionNumberDesc(UUID materialId);
    boolean existsByMaterialIdAndStatus(UUID materialId, VersionStatus status);
    
    @Modifying
    @Query("UPDATE MaterialVersion mv SET mv.status = :newStatus WHERE mv.material.id = :materialId AND mv.status = :currentStatus")
    int updateStatusByMaterialIdAndStatus(@Param("materialId") UUID materialId, @Param("currentStatus") VersionStatus currentStatus, @Param("newStatus") VersionStatus newStatus);
}
