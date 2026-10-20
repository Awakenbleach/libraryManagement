package com.skillovilla.readingmaterial.unit;

import com.skillovilla.readingmaterial.dto.request.BookDetailsRequest;
import com.skillovilla.readingmaterial.dto.request.ChapterRequest;
import com.skillovilla.readingmaterial.dto.request.CreateMaterialRequest;
import com.skillovilla.readingmaterial.entity.MaterialType;
import com.skillovilla.readingmaterial.entity.PositionType;
import com.skillovilla.readingmaterial.exception.InvalidOperationException;
import com.skillovilla.readingmaterial.repository.BookChapterRepository;
import com.skillovilla.readingmaterial.repository.BookVersionDetailsRepository;
import com.skillovilla.readingmaterial.service.BookDetailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookDetailServiceTest {

    @Mock
    private BookVersionDetailsRepository bookRepository;

    @Mock
    private BookChapterRepository chapterRepository;

    private BookDetailService service;

    @BeforeEach
    void setUp() {
        service = new BookDetailService(bookRepository, chapterRepository);
    }

    @Test
    void getSupportedType_returnsBook() {
        assertThat(service.getSupportedType()).isEqualTo(MaterialType.BOOK);
        assertThat(service.getPositionType()).isEqualTo(PositionType.CHAPTER);
    }

    @Test
    void validateRequest_throwsWhenDetailsNullOrEmptyChapters() {
        CreateMaterialRequest reqNull = new CreateMaterialRequest("Book", MaterialType.BOOK, null, null, null, null);
        assertThatThrownBy(() -> service.validateRequest(reqNull))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Book details are required");

        CreateMaterialRequest reqEmpty = new CreateMaterialRequest("Book", MaterialType.BOOK, null, null,
                new BookDetailsRequest("Author", List.of()), null);
        assertThatThrownBy(() -> service.validateRequest(reqEmpty))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("At least one chapter is required");
    }

    @Test
    void validatePosition_throwsWhenOutsideChapterCount() {
        UUID versionId = UUID.randomUUID();
        when(chapterRepository.countByBookVersionDetailsVersionId(versionId)).thenReturn(5);

        assertThatThrownBy(() -> service.validatePosition(0, versionId))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("between 1 and 5");

        assertThatThrownBy(() -> service.validatePosition(6, versionId))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("between 1 and 5");
    }

    @Test
    void validatePosition_succeedsWithinChapterCount() {
        UUID versionId = UUID.randomUUID();
        when(chapterRepository.countByBookVersionDetailsVersionId(versionId)).thenReturn(5);

        service.validatePosition(1, versionId);
        service.validatePosition(3, versionId);
        service.validatePosition(5, versionId);
    }

    @Test
    void isCompleted_returnsTrueWhenLastChapterReached() {
        UUID versionId = UUID.randomUUID();
        when(chapterRepository.countByBookVersionDetailsVersionId(versionId)).thenReturn(5);

        assertThat(service.isCompleted(4, versionId)).isFalse();
        assertThat(service.isCompleted(5, versionId)).isTrue();
        assertThat(service.isCompleted(6, versionId)).isTrue();
    }
}
