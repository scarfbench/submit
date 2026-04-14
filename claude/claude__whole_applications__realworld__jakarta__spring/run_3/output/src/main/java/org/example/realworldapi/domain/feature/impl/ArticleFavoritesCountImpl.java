package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.ArticleFavoritesCount;
import org.example.realworldapi.domain.feature.FindArticleById;
import org.example.realworldapi.domain.model.article.FavoriteRelationshipRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ArticleFavoritesCountImpl implements ArticleFavoritesCount {

    private final FindArticleById findArticleById;
    private final FavoriteRelationshipRepository favoriteRelationshipRepository;

    public ArticleFavoritesCountImpl(FindArticleById findArticleById,
                                     FavoriteRelationshipRepository favoriteRelationshipRepository) {
        this.findArticleById = findArticleById;
        this.favoriteRelationshipRepository = favoriteRelationshipRepository;
    }

    @Override
    public long handle(UUID articleId) {
        final var article = findArticleById.handle(articleId);
        return favoriteRelationshipRepository.favoritesCount(article);
    }
}
