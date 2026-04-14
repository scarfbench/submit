package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.IsArticleFavorited;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.FavoriteRelationshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class IsArticleFavoritedImpl implements IsArticleFavorited {

    @Autowired
    private FavoriteRelationshipRepository favoriteRelationshipRepository;

    @Override
    public boolean handle(Article article, UUID currentUserId) {
        return favoriteRelationshipRepository.isFavorited(article, currentUserId);
    }
}
