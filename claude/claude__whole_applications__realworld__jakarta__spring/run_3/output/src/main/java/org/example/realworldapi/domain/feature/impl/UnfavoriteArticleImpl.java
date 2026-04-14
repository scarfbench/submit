package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.FindArticleBySlug;
import org.example.realworldapi.domain.feature.UnfavoriteArticle;
import org.example.realworldapi.domain.model.article.FavoriteRelationshipRepository;

import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class UnfavoriteArticleImpl implements UnfavoriteArticle {

    private final FindArticleBySlug findArticleBySlug;
    private final FavoriteRelationshipRepository favoriteRelationshipRepository;
    public UnfavoriteArticleImpl(FindArticleBySlug findArticleBySlug, FavoriteRelationshipRepository favoriteRelationshipRepository) {
        this.findArticleBySlug = findArticleBySlug;
        this.favoriteRelationshipRepository = favoriteRelationshipRepository;
    }

    @Override
    public void handle(String articleSlug, UUID currentUserId) {
        final var article = findArticleBySlug.handle(articleSlug);
        final var favoriteRelationshipOptional =
                favoriteRelationshipRepository.findByArticleIdAndUserId(article.getId(), currentUserId);
        favoriteRelationshipOptional.ifPresent(favoriteRelationshipRepository::delete);
    }
}
