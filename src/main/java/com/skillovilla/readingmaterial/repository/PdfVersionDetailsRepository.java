package com.skillovilla.readingmaterial.repository;

import com.skillovilla.readingmaterial.entity.PdfVersionDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PdfVersionDetailsRepository extends JpaRepository<PdfVersionDetails, UUID> {
    Optional<PdfVersionDetails> findByVersionId(UUID versionId);
}
