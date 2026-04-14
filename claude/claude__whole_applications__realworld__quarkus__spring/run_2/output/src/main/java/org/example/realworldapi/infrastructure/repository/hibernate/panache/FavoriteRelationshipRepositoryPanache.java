package org.example.realworldapi.infrastructure.repository.hibernate.panache;

import java.util.Optional;
import java.util.UUID;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.FavoriteRelationship;
import org.example.realworldapi.domain.model.article.FavoriteRelationshipRepository;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.EntityUtils;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.FavoriteRelationshipEntity;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.FavoriteRelationshipEntityKey;
import org.springframework.stereotype.Repository;

@Repository
public class FavoriteRelationshipRepositoryPanache extends AbstractPanacheRepository
    implements FavoriteRelationshipRepository {

  private final EntityUtils entityUtils;

  public FavoriteRelationshipRepositoryPanache(EntityUtils entityUtils) {
    this.entityUtils = entityUtils;
  }

  @Override
  public boolean isFavorited(Article article, UUID currentUserId) {
    Long count = entityManager.createQuery(
            "select count(f) from FavoriteRelationshipEntity f where f.article.id = :articleId and f.user.id = :currentUserId", Long.class)
        .setParameter("articleId", article.getId())
        .setParameter("currentUserId", currentUserId)
        .getSingleResult();
    return count > 0;
  }

  @Override
  public long favoritesCount(Article article) {
    return entityManager.createQuery(
            "select count(f) from FavoriteRelationshipEntity f where f.article.id = :articleId", Long.class)
        .setParameter("articleId", article.getId())
        .getSingleResult();
  }

  @Override
  public Optional<FavoriteRelationship> findByArticleIdAndUserId(UUID articleId, UUID currentUserId) {
    return entityManager.createQuery(
            "from FavoriteRelationshipEntity where article.id = :articleId and user.id = :currentUserId", FavoriteRelationshipEntity.class)
        .setParameter("articleId", articleId)
        .setParameter("currentUserId", currentUserId)
        .getResultStream()
        .findFirst()
        .map(entityUtils::favoriteRelationship);
  }

  @Override
  public void save(FavoriteRelationship favoriteRelationship) {
    final var userEntity = findUserEntityById(favoriteRelationship.getUser().getId());
    final var articleEntity = findArticleEntityById(favoriteRelationship.getArticle().getId());
    entityManager.persist(new FavoriteRelationshipEntity(userEntity, articleEntity));
  }

  @Override
  public void delete(FavoriteRelationship favoriteRelationship) {
    final var favoriteRelationshipEntity = findFavoriteRelationshipEntityByKey(favoriteRelationship);
    entityManager.remove(favoriteRelationshipEntity);
  }
}
