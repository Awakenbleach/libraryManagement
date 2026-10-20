package com.skillovilla.readingmaterial.unit;

import com.skillovilla.readingmaterial.dto.request.CreateMaterialRequest;
import com.skillovilla.readingmaterial.dto.request.SlideDeckDetailsRequest;
import com.skillovilla.readingmaterial.entity.MaterialType;
import com.skillovilla.readingmaterial.entity.PositionType;
import com.skillovilla.readingmaterial.entity.SlideDeckVersionDetails;
import com.skillovilla.readingmaterial.exception.InvalidOperationException;
import com.skillovilla.readingmaterial.repository.SlideDeckVersionDetailsRepository;
import com.skillovilla.readingmaterial.service.SlideDeckDetailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlideDeckDetailServiceTest {

    @Mock
    private SlideDeckVersionDetailsRepository slideDeckRepository;

    private SlideDeckDetailService service;

    @BeforeEach
    void setUp() {
        service = new SlideDeckDetailService(slideDeckRepository);
    }

    @Test
    void getSupportedType_returnsSlideDeck() {
        assertThat(service.getSupportedType()).isEqualTo(MaterialType.SLIDE_DECK);
        assertThat(service.getPositionType()).isEqualTo(PositionType.SLIDE);
    }

    @Test
    void validateRequest_throwsWhenDetailsNull() {
        CreateMaterialRequest req = new CreateMaterialRequest("Deck", MaterialType.SLIDE_DECK, null, null, null, null);
        assertThatThrownBy(() -> service.validateRequest(req))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Slide deck details are required");
    }

    @Test
    void validatePosition_throwsWhenOutsideSlideRange() {
        UUID versionId = UUID.randomUUID();
        SlideDeckVersionDetails details = new SlideDeckVersionDetails();
        details.setSlideCount(40);
        when(slideDeckRepository.findById(versionId)).thenReturn(Optional.of(details));

        assertThatThrownBy(() -> service.validatePosition(0, versionId))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("between 1 and 40");

        assertThatThrownBy(() -> service.validatePosition(41, versionId))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("between 1 and 40");
    }

    @Test
    void validatePosition_succeedsWithinSlideRange() {
        UUID versionId = UUID.randomUUID();
        SlideDeckVersionDetails details = new SlideDeckVersionDetails();
        details.setSlideCount(40);
        when(slideDeckRepository.findById(versionId)).thenReturn(Optional.of(details));

        service.validatePosition(1, versionId);
        service.validatePosition(20, versionId);
        service.validatePosition(40, versionId);
    }

    @Test
    void isCompleted_returnsTrueWhenLastSlideReached() {
        UUID versionId = UUID.randomUUID();
        SlideDeckVersionDetails details = new SlideDeckVersionDetails();
        details.setSlideCount(40);
        when(slideDeckRepository.findById(versionId)).thenReturn(Optional.of(details));

        assertThat(service.isCompleted(39, versionId)).isFalse();
        assertThat(service.isCompleted(40, versionId)).isTrue();
        assertThat(service.isCompleted(50, versionId)).isTrue();
    }
}
