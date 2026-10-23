package com.skillovilla.readingmaterial.entity;
import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
    name = "book_chapter",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"version_id", "chapter_number"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookChapter {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", nullable = false)
    private BookVersionDetails bookVersionDetails;

    @Column(name = "chapter_number", nullable = false)
    private int chapterNumber;

    @Column(nullable = false)
    private String title;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookChapter that)) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
