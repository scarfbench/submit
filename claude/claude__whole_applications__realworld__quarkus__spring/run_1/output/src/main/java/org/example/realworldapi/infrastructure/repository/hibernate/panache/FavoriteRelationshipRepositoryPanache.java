package org.example.realworldapi.infrastructure.repository.hibernate.panache;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import java.util.UUID;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.FavoriteRelationship;
import org.example.realworldapi.domain.model.article.FavoriteRelationshipRepository;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.*;
import org.springframework.stereotype.Repository;

@Repository
public class FavoriteRelationshipRepositoryPanache implements FavoriteRelationshipRepository {

  @PersistenceContext
  private EntityManager entityManager;

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
    var results = entityManager.createQuery(
            "from FavoriteRelationshipEntity f where f.article.id = :articleId and f.user.id = :currentUserId", FavoriteRelationshipEntity.class)
        .setParameter("articleId", articleId)
        .setParameter("currentUserId", currentUserId)
        .getResultList();
    return results.stream().findFirst().map(entityUtils::favoriteRelationship);
  }

  @Override
  public void save(FavoriteRelationship favoriteRelationship) {
    final var userEntity = entityManager.find(UserEntity.class, favoriteRelationship.getUser().getId());
    final var articleEntity = entityManager.find(ArticleEntity.class, favoriteRelationship.getArticle().getId());
    entityManager.persist(new FavoriteRelationshipEntity(userEntity, articleEntity));
  }

  @Override
  public void delete(FavoriteRelationship favoriteRelationship) {
    final var userEntity = entityManager.find(UserEntity.class, favoriteRelationship.getUser().getId());
    final var articleEntity = entityManager.find(ArticleEntity.class, favoriteRelationship.getArticle().getId());
    final var key = new FavoriteRelationshipEntityKey();
    key.setUser(userEntity);
    key.setArticle(articleEntity);
    final var entity = entityManager.find(FavoriteRelationshipEntity.class, key);
    if (entity != null) {
      entityManager.remove(entity);
    }
  }
}
