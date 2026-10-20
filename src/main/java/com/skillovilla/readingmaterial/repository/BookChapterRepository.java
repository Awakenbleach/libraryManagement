package com.skillovilla.readingmaterial.repository;

import com.skillovilla.readingmaterial.entity.BookChapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookChapterRepository extends JpaRepository<BookChapter, UUID> {
    List<BookChapter> findByBookVersionDetailsVersionIdOrderByChapterNumberAsc(UUID versionId);
    int countByBookVersionDetailsVersionId(UUID versionId);
}
