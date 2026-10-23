package com.skillovilla.readingmaterial.entity;
import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "collection_material")
@IdClass(CollectionMaterial.CollectionMaterialId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CollectionMaterial {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CollectionMaterial that)) return false;
        return Objects.equals(collection.getId(), that.collection.getId())
                && Objects.equals(material.getId(), that.material.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(collection.getId(), material.getId());
    }

    public static class CollectionMaterialId implements Serializable {
        private UUID collection;
        private UUID material;

        public CollectionMaterialId() {}

        public CollectionMaterialId(UUID collection, UUID material) {
            this.collection = collection;
            this.material = material;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CollectionMaterialId that)) return false;
            return Objects.equals(collection, that.collection) && Objects.equals(material, that.material);
        }

        @Override
        public int hashCode() {
            return Objects.hash(collection, material);
        }

        public UUID getCollection() { return collection; }
        public void setCollection(UUID collection) { this.collection = collection; }
        public UUID getMaterial() { return material; }
        public void setMaterial(UUID material) { this.material = material; }
    }
}
