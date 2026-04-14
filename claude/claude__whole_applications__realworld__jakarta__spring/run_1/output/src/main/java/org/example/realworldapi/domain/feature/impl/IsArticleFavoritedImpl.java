package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.IsArticleFavorited;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.FavoriteRelationshipRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class IsArticleFavoritedImpl implements IsArticleFavorited {

    private final FavoriteRelationshipRepository favoriteRelationshipRepository;

    public IsArticleFavoritedImpl(FavoriteRelationshipRepository favoriteRelationshipRepository) {
        this.favoriteRelationshipRepository = favoriteRelationshipRepository;
    }

    @Override
    public boolean handle(Article article, UUID currentUserId) {
        return favoriteRelationshipRepository.isFavorited(article, currentUserId);
    }
}
