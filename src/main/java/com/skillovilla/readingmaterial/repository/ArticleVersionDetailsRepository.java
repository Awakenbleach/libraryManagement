package com.skillovilla.readingmaterial.repository;

import com.skillovilla.readingmaterial.entity.ArticleVersionDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArticleVersionDetailsRepository extends JpaRepository<ArticleVersionDetails, UUID> {
    Optional<ArticleVersionDetails> findByVersionId(UUID versionId);
}
