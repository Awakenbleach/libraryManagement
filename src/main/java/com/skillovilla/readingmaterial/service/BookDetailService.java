package com.skillovilla.readingmaterial.service;

import com.skillovilla.readingmaterial.dto.request.CreateMaterialRequest;
import com.skillovilla.readingmaterial.dto.response.BookDetailsResponse;
import com.skillovilla.readingmaterial.dto.response.ChapterResponse;
import com.skillovilla.readingmaterial.entity.*;
import com.skillovilla.readingmaterial.exception.InvalidOperationException;
import com.skillovilla.readingmaterial.exception.ResourceNotFoundException;
import com.skillovilla.readingmaterial.repository.BookChapterRepository;
import com.skillovilla.readingmaterial.repository.BookVersionDetailsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BookDetailService implements TypeDetailService {

    private final BookVersionDetailsRepository bookRepository;
    private final BookChapterRepository chapterRepository;

    public BookDetailService(BookVersionDetailsRepository bookRepository, BookChapterRepository chapterRepository) {
        this.bookRepository = bookRepository;
        this.chapterRepository = chapterRepository;
    }

    @Override
    public MaterialType getSupportedType() { return MaterialType.BOOK; }

    @Override
    public void validateRequest(CreateMaterialRequest request) {
        if (request.bookDetails() == null) {
            throw new InvalidOperationException("Book details are required for BOOK material type");
        }
        if (request.bookDetails().chapters() == null || request.bookDetails().chapters().isEmpty()) {
            throw new InvalidOperationException("At least one chapter is required for a book");
        }
    }

    @Override
    public void createDetails(MaterialVersion version, CreateMaterialRequest request) {
        BookVersionDetails details = new BookVersionDetails();
        details.setMaterialVersion(version);
        details.setAuthor(request.bookDetails().author());
        bookRepository.save(details);
        
        for (var chapterReq : request.bookDetails().chapters()) {
            BookChapter chapter = new BookChapter();
            chapter.setBookVersionDetails(details);
            chapter.setChapterNumber(chapterReq.chapterNumber());
            chapter.setTitle(chapterReq.title());
            chapterRepository.save(chapter);
        }
    }

    @Override
    public void copyDetails(MaterialVersion sourceVersion, MaterialVersion targetVersion, com.skillovilla.readingmaterial.dto.request.CreateVersionRequest overrides) {
        BookVersionDetails source = bookRepository.findById(sourceVersion.getId())
                .orElseThrow(() -> new ResourceNotFoundException("BookVersionDetails", sourceVersion.getId().toString()));
        BookVersionDetails target = new BookVersionDetails();
        target.setMaterialVersion(targetVersion);
        
        if (overrides != null && overrides.bookDetails() != null) {
            target.setAuthor(overrides.bookDetails().author());
            bookRepository.save(target);
            for (var chapterReq : overrides.bookDetails().chapters()) {
                BookChapter chapter = new BookChapter();
                chapter.setBookVersionDetails(target);
                chapter.setChapterNumber(chapterReq.chapterNumber());
                chapter.setTitle(chapterReq.title());
                chapterRepository.save(chapter);
            }
        } else {
            target.setAuthor(source.getAuthor());
            bookRepository.save(target);
            List<BookChapter> sourceChapters = chapterRepository.findByBookVersionDetailsVersionIdOrderByChapterNumberAsc(sourceVersion.getId());
            for (BookChapter sourceChapter : sourceChapters) {
                BookChapter newChapter = new BookChapter();
                newChapter.setBookVersionDetails(target);
                newChapter.setChapterNumber(sourceChapter.getChapterNumber());
                newChapter.setTitle(sourceChapter.getTitle());
                chapterRepository.save(newChapter);
            }
        }
    }

    @Override
    public Object getDetailsResponse(UUID versionId) {
        BookVersionDetails details = bookRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("BookVersionDetails", versionId.toString()));
        List<BookChapter> chapters = chapterRepository.findByBookVersionDetailsVersionIdOrderByChapterNumberAsc(versionId);
        List<ChapterResponse> chapterResponses = chapters.stream()
                .map(ch -> new ChapterResponse(ch.getChapterNumber(), ch.getTitle()))
                .toList();
        return new BookDetailsResponse(details.getAuthor(), chapterResponses);
    }

    @Override
    public PositionType getPositionType() { return PositionType.CHAPTER; }

    @Override
    public void validatePosition(int positionValue, UUID versionId) {
        int totalChapters = chapterRepository.countByBookVersionDetailsVersionId(versionId);
        if (positionValue < 1 || positionValue > totalChapters) {
            throw new InvalidOperationException(
                    "Chapter position must be between 1 and " + totalChapters);
        }
    }

    @Override
    public boolean isCompleted(int positionValue, UUID versionId) {
        int totalChapters = chapterRepository.countByBookVersionDetailsVersionId(versionId);
        return positionValue >= totalChapters;
    }
}
