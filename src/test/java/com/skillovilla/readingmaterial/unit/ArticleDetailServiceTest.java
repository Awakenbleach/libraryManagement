package com.skillovilla.readingmaterial.unit;

import com.skillovilla.readingmaterial.dto.request.ArticleDetailsRequest;
import com.skillovilla.readingmaterial.dto.request.CreateMaterialRequest;
import com.skillovilla.readingmaterial.entity.MaterialType;
import com.skillovilla.readingmaterial.entity.PositionType;
import com.skillovilla.readingmaterial.exception.InvalidOperationException;
import com.skillovilla.readingmaterial.repository.ArticleVersionDetailsRepository;
import com.skillovilla.readingmaterial.service.ArticleDetailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ArticleDetailServiceTest {

    @Mock
    private ArticleVersionDetailsRepository articleRepository;

    private ArticleDetailService service;

    @BeforeEach
    void setUp() {
        service = new ArticleDetailService(articleRepository);
    }

    @Test
    void getSupportedType_returnsArticle() {
        assertThat(service.getSupportedType()).isEqualTo(MaterialType.ARTICLE);
        assertThat(service.getPositionType()).isEqualTo(PositionType.PERCENTAGE);
    }

    @Test
    void validateRequest_throwsWhenDetailsNull() {
        CreateMaterialRequest req = new CreateMaterialRequest("Article", MaterialType.ARTICLE, null, null, null, null);
        assertThatThrownBy(() -> service.validateRequest(req))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Article details are required");
    }

    @Test
    void validatePosition_throwsWhenOutside0To100() {
        UUID versionId = UUID.randomUUID();
        assertThatThrownBy(() -> service.validatePosition(-1, versionId))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("between 0 and 100");

        assertThatThrownBy(() -> service.validatePosition(101, versionId))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("between 0 and 100");
    }

    @Test
    void validatePosition_succeedsForValidPercentages() {
        UUID versionId = UUID.randomUUID();
        service.validatePosition(0, versionId);
        service.validatePosition(50, versionId);
        service.validatePosition(100, versionId);
    }

    @Test
    void isCompleted_returnsTrueAtOrAbove100() {
        UUID versionId = UUID.randomUUID();
        assertThat(service.isCompleted(0, versionId)).isFalse();
        assertThat(service.isCompleted(99, versionId)).isFalse();
        assertThat(service.isCompleted(100, versionId)).isTrue();
        assertThat(service.isCompleted(120, versionId)).isTrue();
    }
}
