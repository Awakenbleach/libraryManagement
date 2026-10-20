package com.skillovilla.readingmaterial.repository;

import com.skillovilla.readingmaterial.entity.SlideDeckVersionDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SlideDeckVersionDetailsRepository extends JpaRepository<SlideDeckVersionDetails, UUID> {
    Optional<SlideDeckVersionDetails> findByVersionId(UUID versionId);
}
