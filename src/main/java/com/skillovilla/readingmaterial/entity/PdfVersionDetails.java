package com.skillovilla.readingmaterial.entity;
import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
@Table(name = "pdf_version_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PdfVersionDetails {

    @Id
    private UUID versionId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "version_id")
    private MaterialVersion materialVersion;

    @Column(name = "page_count", nullable = false)
    @Positive
    private int pageCount;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PdfVersionDetails that)) return false;
        return Objects.equals(getVersionId(), that.getVersionId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVersionId());
    }
}
