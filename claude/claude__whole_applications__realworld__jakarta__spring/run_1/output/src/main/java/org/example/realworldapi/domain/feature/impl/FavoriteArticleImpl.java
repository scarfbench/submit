package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.FavoriteArticle;
import org.example.realworldapi.domain.feature.FindArticleBySlug;
import org.example.realworldapi.domain.feature.FindUserById;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.FavoriteRelationship;
import org.example.realworldapi.domain.model.article.FavoriteRelationshipRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FavoriteArticleImpl implements FavoriteArticle {

    private final FindArticleBySlug findArticleBySlug;
    private final FindUserById findUserById;
    private final FavoriteRelationshipRepository favoriteRelationshipRepository;

    public FavoriteArticleImpl(
            FindArticleBySlug findArticleBySlug,
            FindUserById findUserById,
            FavoriteRelationshipRepository favoriteRelationshipRepository) {
        this.findArticleBySlug = findArticleBySlug;
        this.findUserById = findUserById;
        this.favoriteRelationshipRepository = favoriteRelationshipRepository;
    }

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
