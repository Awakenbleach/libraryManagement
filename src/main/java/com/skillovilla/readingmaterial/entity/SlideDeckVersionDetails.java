package com.skillovilla.readingmaterial.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "slide_deck_version_details")
public class SlideDeckVersionDetails {

    @Id
    private UUID versionId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "version_id")
    private MaterialVersion materialVersion;

    @Column(name = "slide_count", nullable = false)
    @Positive
    private int slideCount;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    public SlideDeckVersionDetails() {
    }

    public SlideDeckVersionDetails(MaterialVersion materialVersion, int slideCount, String fileUrl) {
        this.materialVersion = materialVersion;
        this.slideCount = slideCount;
        this.fileUrl = fileUrl;
    }

    public UUID getVersionId() {
        return versionId;
    }

    public void setVersionId(UUID versionId) {
        this.versionId = versionId;
    }

    public MaterialVersion getMaterialVersion() {
        return materialVersion;
    }

    public void setMaterialVersion(MaterialVersion materialVersion) {
        this.materialVersion = materialVersion;
    }

    public int getSlideCount() {
        return slideCount;
    }

    public void setSlideCount(int slideCount) {
        this.slideCount = slideCount;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SlideDeckVersionDetails that)) return false;
        return Objects.equals(getVersionId(), that.getVersionId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVersionId());
    }
}
