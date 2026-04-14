package org.example.realworldapi.infrastructure.repository.hibernate.panache;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.FavoriteRelationship;
import org.example.realworldapi.domain.model.article.FavoriteRelationshipRepository;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.EntityUtils;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.FavoriteRelationshipEntity;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.FavoriteRelationshipEntityKey;

@ApplicationScoped
public class FavoriteRelationshipRepositoryPanache
    extends AbstractPanacheRepository<FavoriteRelationshipEntity, FavoriteRelationshipEntityKey>
    implements FavoriteRelationshipRepository {

  @Inject EntityUtils entityUtils;

  @Override
  public boolean isFavorited(Article article, UUID currentUserId) {
    Long count = em.createQuery(
            "SELECT COUNT(e) FROM FavoriteRelationshipEntity e WHERE e.article.id = :articleId and e.user.id = :currentUserId",
            Long.class)
        .setParameter("articleId", article.getId())
        .setParameter("currentUserId", currentUserId)
        .getSingleResult();
    return count > 0;
  }

  @Override
  public long favoritesCount(Article article) {
    return em.createQuery(
            "SELECT COUNT(e) FROM FavoriteRelationshipEntity e WHERE e.article.id = ?1",
            Long.class)
        .setParameter(1, article.getId())
        .getSingleResult();
  }

  @Override
  public Optional<FavoriteRelationship> findByArticleIdAndUserId(
      UUID articleId, UUID currentUserId) {
    return em.createQuery(
            "FROM FavoriteRelationshipEntity WHERE article.id = :articleId and user.id = :currentUserId",
            FavoriteRelationshipEntity.class)
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
    em.persist(new FavoriteRelationshipEntity(userEntity, articleEntity));
  }

  @Override
  public void delete(FavoriteRelationship favoriteRelationship) {
    final var favoriteRelationshipEntity =
        findFavoriteRelationshipEntityByKey(favoriteRelationship);
    em.remove(em.merge(favoriteRelationshipEntity));
  }
}
