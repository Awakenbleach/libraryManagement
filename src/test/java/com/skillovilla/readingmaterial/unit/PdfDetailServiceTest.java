package com.skillovilla.readingmaterial.unit;

import com.skillovilla.readingmaterial.dto.request.CreateMaterialRequest;
import com.skillovilla.readingmaterial.dto.request.PdfDetailsRequest;
import com.skillovilla.readingmaterial.entity.MaterialType;
import com.skillovilla.readingmaterial.entity.PdfVersionDetails;
import com.skillovilla.readingmaterial.entity.PositionType;
import com.skillovilla.readingmaterial.exception.InvalidOperationException;
import com.skillovilla.readingmaterial.repository.PdfVersionDetailsRepository;
import com.skillovilla.readingmaterial.service.PdfDetailService;
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
class PdfDetailServiceTest {

    @Mock
    private PdfVersionDetailsRepository pdfRepository;

    private PdfDetailService service;

    @BeforeEach
    void setUp() {
        service = new PdfDetailService(pdfRepository);
    }

    @Test
    void getSupportedType_returnsPdf() {
        assertThat(service.getSupportedType()).isEqualTo(MaterialType.PDF);
        assertThat(service.getPositionType()).isEqualTo(PositionType.PAGE);
    }

    @Test
    void validateRequest_throwsWhenDetailsNull() {
        CreateMaterialRequest req = new CreateMaterialRequest("Title", MaterialType.PDF, null, null, null, null);
        assertThatThrownBy(() -> service.validateRequest(req))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("PDF details are required");
    }

    @Test
    void validatePosition_throwsWhenBelowOneOrAbovePageCount() {
        UUID versionId = UUID.randomUUID();
        PdfVersionDetails details = new PdfVersionDetails();
        details.setPageCount(100);
        when(pdfRepository.findById(versionId)).thenReturn(Optional.of(details));

        assertThatThrownBy(() -> service.validatePosition(0, versionId))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("between 1 and 100");

        assertThatThrownBy(() -> service.validatePosition(101, versionId))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("between 1 and 100");
    }

    @Test
    void validatePosition_succeedsWithinRange() {
        UUID versionId = UUID.randomUUID();
        PdfVersionDetails details = new PdfVersionDetails();
        details.setPageCount(100);
        when(pdfRepository.findById(versionId)).thenReturn(Optional.of(details));

        // Should not throw
        service.validatePosition(1, versionId);
        service.validatePosition(50, versionId);
        service.validatePosition(100, versionId);
    }

    @Test
    void isCompleted_returnsTrueOnlyWhenReachingPageCount() {
        UUID versionId = UUID.randomUUID();
        PdfVersionDetails details = new PdfVersionDetails();
        details.setPageCount(100);
        when(pdfRepository.findById(versionId)).thenReturn(Optional.of(details));

        assertThat(service.isCompleted(99, versionId)).isFalse();
        assertThat(service.isCompleted(100, versionId)).isTrue();
        assertThat(service.isCompleted(105, versionId)).isTrue();
    }
}
