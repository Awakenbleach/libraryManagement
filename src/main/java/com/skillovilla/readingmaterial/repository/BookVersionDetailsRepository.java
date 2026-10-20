package com.skillovilla.readingmaterial.repository;

import com.skillovilla.readingmaterial.entity.BookVersionDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookVersionDetailsRepository extends JpaRepository<BookVersionDetails, UUID> {
    Optional<BookVersionDetails> findByVersionId(UUID versionId);
}
