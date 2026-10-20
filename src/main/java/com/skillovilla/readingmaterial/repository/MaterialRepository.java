package com.skillovilla.readingmaterial.repository;

import com.skillovilla.readingmaterial.entity.Material;
import com.skillovilla.readingmaterial.entity.MaterialType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface MaterialRepository extends JpaRepository<Material, UUID>, JpaSpecificationExecutor<Material> {

    @Query("""
        SELECT DISTINCT m FROM Material m
        JOIN m.versions v
        WHERE v.status = com.skillovilla.readingmaterial.entity.VersionStatus.LIVE
        AND (
            EXISTS (
                SELECT rma FROM ReaderMaterialAccess rma
                WHERE rma.reader.id = :readerId
                AND rma.material.id = m.id
                AND (rma.accessEndDate IS NULL OR rma.accessEndDate >= :today)
            )
            OR EXISTS (
                SELECT rca FROM ReaderCollectionAccess rca
                JOIN CollectionMaterial cm ON cm.collection.id = rca.collection.id
                WHERE rca.reader.id = :readerId
                AND cm.material.id = m.id
                AND (rca.accessEndDate IS NULL OR rca.accessEndDate >= :today)
            )
        )
    """)
    Page<Material> findAccessibleMaterials(@Param("readerId") UUID readerId,
                                            @Param("today") LocalDate today,
                                            Pageable pageable);

    @Query("""
        SELECT DISTINCT m FROM Material m
        JOIN m.versions v
        WHERE v.status = com.skillovilla.readingmaterial.entity.VersionStatus.LIVE
        AND (:materialType IS NULL OR m.materialType = :materialType)
        AND (:title IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%')))
        AND (
            :minPageCount IS NULL OR EXISTS (
                SELECT p FROM PdfVersionDetails p WHERE p.versionId = v.id AND p.pageCount >= :minPageCount
            )
        )
        AND (
            :maxPageCount IS NULL OR EXISTS (
                SELECT p FROM PdfVersionDetails p WHERE p.versionId = v.id AND p.pageCount <= :maxPageCount
            )
        )
        AND (
            :minWordCount IS NULL OR EXISTS (
                SELECT a FROM ArticleVersionDetails a WHERE a.versionId = v.id AND a.wordCount >= :minWordCount
            )
        )
        AND (
            :maxWordCount IS NULL OR EXISTS (
                SELECT a FROM ArticleVersionDetails a WHERE a.versionId = v.id AND a.wordCount <= :maxWordCount
            )
        )
        AND (
            :minSlideCount IS NULL OR EXISTS (
                SELECT s FROM SlideDeckVersionDetails s WHERE s.versionId = v.id AND s.slideCount >= :minSlideCount
            )
        )
        AND (
            :maxSlideCount IS NULL OR EXISTS (
                SELECT s FROM SlideDeckVersionDetails s WHERE s.versionId = v.id AND s.slideCount <= :maxSlideCount
            )
        )
        AND (
            :author IS NULL OR EXISTS (
                SELECT b FROM BookVersionDetails b WHERE b.versionId = v.id AND LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))
            )
        )
        AND (
            EXISTS (
                SELECT rma FROM ReaderMaterialAccess rma
                WHERE rma.reader.id = :readerId
                AND rma.material.id = m.id
                AND (rma.accessEndDate IS NULL OR rma.accessEndDate >= :today)
            )
            OR EXISTS (
                SELECT rca FROM ReaderCollectionAccess rca
                JOIN CollectionMaterial cm ON cm.collection.id = rca.collection.id
                WHERE rca.reader.id = :readerId
                AND cm.material.id = m.id
                AND (rca.accessEndDate IS NULL OR rca.accessEndDate >= :today)
            )
        )
    """)
    Page<Material> searchAccessibleMaterials(
            @Param("readerId") UUID readerId,
            @Param("materialType") MaterialType materialType,
            @Param("title") String title,
            @Param("minPageCount") Integer minPageCount,
            @Param("maxPageCount") Integer maxPageCount,
            @Param("minWordCount") Integer minWordCount,
            @Param("maxWordCount") Integer maxWordCount,
            @Param("minSlideCount") Integer minSlideCount,
            @Param("maxSlideCount") Integer maxSlideCount,
            @Param("author") String author,
            @Param("today") LocalDate today,
            Pageable pageable);
}

