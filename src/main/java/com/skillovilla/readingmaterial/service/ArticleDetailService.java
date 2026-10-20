package com.skillovilla.readingmaterial.service;

import com.skillovilla.readingmaterial.dto.request.CreateMaterialRequest;
import com.skillovilla.readingmaterial.dto.response.ArticleDetailsResponse;
import com.skillovilla.readingmaterial.entity.*;
import com.skillovilla.readingmaterial.exception.InvalidOperationException;
import com.skillovilla.readingmaterial.exception.ResourceNotFoundException;
import com.skillovilla.readingmaterial.repository.ArticleVersionDetailsRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ArticleDetailService implements TypeDetailService {

    private final ArticleVersionDetailsRepository articleRepository;

    public ArticleDetailService(ArticleVersionDetailsRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Override
    public MaterialType getSupportedType() { return MaterialType.ARTICLE; }

    @Override
    public void validateRequest(CreateMaterialRequest request) {
        if (request.articleDetails() == null) {
            throw new InvalidOperationException("Article details are required for ARTICLE material type");
        }
    }

    @Override
    public void createDetails(MaterialVersion version, CreateMaterialRequest request) {
        ArticleVersionDetails details = new ArticleVersionDetails();
        details.setMaterialVersion(version);
        details.setWordCount(request.articleDetails().wordCount());
        articleRepository.save(details);
    }

    @Override
    public void copyDetails(MaterialVersion sourceVersion, MaterialVersion targetVersion, com.skillovilla.readingmaterial.dto.request.CreateVersionRequest overrides) {
        ArticleVersionDetails source = articleRepository.findById(sourceVersion.getId())
                .orElseThrow(() -> new ResourceNotFoundException("ArticleVersionDetails", sourceVersion.getId().toString()));
        ArticleVersionDetails target = new ArticleVersionDetails();
        target.setMaterialVersion(targetVersion);
        if (overrides != null && overrides.articleDetails() != null) {
            target.setWordCount(overrides.articleDetails().wordCount());
        } else {
            target.setWordCount(source.getWordCount());
        }
        articleRepository.save(target);
    }

    @Override
    public Object getDetailsResponse(UUID versionId) {
        ArticleVersionDetails details = articleRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("ArticleVersionDetails", versionId.toString()));
        return new ArticleDetailsResponse(details.getWordCount());
    }

    @Override
    public PositionType getPositionType() { return PositionType.PERCENTAGE; }

    @Override
    public void validatePosition(int positionValue, UUID versionId) {
        if (positionValue < 0 || positionValue > 100) {
            throw new InvalidOperationException("Article progress percentage must be between 0 and 100");
        }
    }

    @Override
    public boolean isCompleted(int positionValue, UUID versionId) {
        return positionValue >= 100;
    }
}
