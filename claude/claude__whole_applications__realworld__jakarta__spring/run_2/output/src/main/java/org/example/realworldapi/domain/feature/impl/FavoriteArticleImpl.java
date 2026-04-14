package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.FavoriteArticle;
import org.example.realworldapi.domain.feature.FindArticleBySlug;
import org.example.realworldapi.domain.feature.FindUserById;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.FavoriteRelationship;
import org.example.realworldapi.domain.model.article.FavoriteRelationshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FavoriteArticleImpl implements FavoriteArticle {

    @Autowired
    private FindArticleBySlug findArticleBySlug;
    @Autowired
    private FindUserById findUserById;
    @Autowired
    private FavoriteRelationshipRepository favoriteRelationshipRepository;

    @Override
    public FavoriteRelationship handle(String articleSlug, UUID currentUserId) {
        final var article = findArticleBySlug.handle(articleSlug);
        final var favoriteRelationshipOptional =
                favoriteRelationshipRepository.findByArticleIdAndUserId(article.getId(), currentUserId);
        return favoriteRelationshipOptional.orElseGet(() -> createFavoriteRelationship(currentUserId, article));
    }

    private FavoriteRelationship createFavoriteRelationship(UUID currentUserId, Article article) {
        final var user = findUserById.handle(currentUserId);
        final var favoriteRelationship = new FavoriteRelationship(user, article);
        favoriteRelationshipRepository.save(favoriteRelationship);
        return favoriteRelationship;
    }
}
