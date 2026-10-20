package com.skillovilla.readingmaterial.repository;

import com.skillovilla.readingmaterial.entity.CollectionMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CollectionMaterialRepository extends JpaRepository<CollectionMaterial, CollectionMaterial.CollectionMaterialId> {
    boolean existsByCollectionIdAndMaterialId(UUID collectionId, UUID materialId);
    void deleteByCollectionIdAndMaterialId(UUID collectionId, UUID materialId);
}
