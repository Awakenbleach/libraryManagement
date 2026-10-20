package com.skillovilla.readingmaterial.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "book_version_details")
public class BookVersionDetails {

    @Id
    private UUID versionId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "version_id")
    private MaterialVersion materialVersion;

    @Column(nullable = false)
    private String author;

    @OneToMany(mappedBy = "bookVersionDetails", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookChapter> chapters = new ArrayList<>();

    public BookVersionDetails() {
    }

    public BookVersionDetails(MaterialVersion materialVersion, String author) {
        this.materialVersion = materialVersion;
        this.author = author;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<BookChapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<BookChapter> chapters) {
        this.chapters = chapters;
    }

    public void addChapter(BookChapter chapter) {
        chapters.add(chapter);
        chapter.setBookVersionDetails(this);
    }

    public void removeChapter(BookChapter chapter) {
        chapters.remove(chapter);
        chapter.setBookVersionDetails(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookVersionDetails that)) return false;
        return Objects.equals(getVersionId(), that.getVersionId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVersionId());
    }
}
