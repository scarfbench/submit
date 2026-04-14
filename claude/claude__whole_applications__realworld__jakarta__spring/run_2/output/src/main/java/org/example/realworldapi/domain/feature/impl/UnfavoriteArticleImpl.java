package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.FindArticleBySlug;
import org.example.realworldapi.domain.feature.UnfavoriteArticle;
import org.example.realworldapi.domain.model.article.FavoriteRelationshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UnfavoriteArticleImpl implements UnfavoriteArticle {

    @Autowired
    private FindArticleBySlug findArticleBySlug;
    @Autowired
    private FavoriteRelationshipRepository favoriteRelationshipRepository;

    @Override
    public void handle(String articleSlug, UUID currentUserId) {
        final var article = findArticleBySlug.handle(articleSlug);
        final var favoriteRelationshipOptional =
                favoriteRelationshipRepository.findByArticleIdAndUserId(article.getId(), currentUserId);
        favoriteRelationshipOptional.ifPresent(favoriteRelationshipRepository::delete);
    }
}
