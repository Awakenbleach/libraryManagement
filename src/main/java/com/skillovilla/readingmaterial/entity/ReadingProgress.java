package com.skillovilla.readingmaterial.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
    name = "reading_progress",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"reader_id", "material_version_id"})
    }
)
public class ReadingProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reader_id", nullable = false)
    private Reader reader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_version_id", nullable = false)
    private MaterialVersion materialVersion;

    @Column(name = "position_value", nullable = false)
    private int positionValue = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "position_type", nullable = false)
    private PositionType positionType;

    @Column(nullable = false)
    private boolean completed = false;

    @Column(name = "progress_revision", nullable = false)
    private long progressRevision = 1;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public ReadingProgress() {
    }

    public ReadingProgress(Reader reader, MaterialVersion materialVersion, PositionType positionType) {
        this.reader = reader;
        this.materialVersion = materialVersion;
        this.positionType = positionType;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Reader getReader() {
        return reader;
    }

    public void setReader(Reader reader) {
        this.reader = reader;
    }

    public MaterialVersion getMaterialVersion() {
        return materialVersion;
    }

    public void setMaterialVersion(MaterialVersion materialVersion) {
        this.materialVersion = materialVersion;
    }

    public int getPositionValue() {
        return positionValue;
    }

    public void setPositionValue(int positionValue) {
        this.positionValue = positionValue;
    }

    public PositionType getPositionType() {
        return positionType;
    }

    public void setPositionType(PositionType positionType) {
        this.positionType = positionType;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public long getProgressRevision() {
        return progressRevision;
    }

    public void setProgressRevision(long progressRevision) {
        this.progressRevision = progressRevision;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReadingProgress that)) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
