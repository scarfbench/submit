package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.ArticleFavoritesCount;
import org.example.realworldapi.domain.feature.FindArticleById;
import org.example.realworldapi.domain.model.article.FavoriteRelationshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ArticleFavoritesCountImpl implements ArticleFavoritesCount {

    @Autowired
    private FindArticleById findArticleById;
    @Autowired
    private FavoriteRelationshipRepository favoriteRelationshipRepository;

    @Override
    public long handle(UUID articleId) {
        final var article = findArticleById.handle(articleId);
        return favoriteRelationshipRepository.favoritesCount(article);
    }
}
