package org.example.realworldapi.infrastructure.repository.hibernate.panache;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.FavoriteRelationship;
import org.example.realworldapi.domain.model.article.FavoriteRelationshipRepository;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.EntityUtils;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.FavoriteRelationshipEntity;

@ApplicationScoped
public class FavoriteRelationshipRepositoryPanache extends AbstractPanacheRepository
    implements FavoriteRelationshipRepository {

  @jakarta.inject.Inject private EntityUtils entityUtils;

  @Override
  public boolean isFavorited(Article article, UUID currentUserId) {
    Long count =
        em.createQuery(
                "select count(f) from FavoriteRelationshipEntity f where f.primaryKey.article.id = :articleId and f.primaryKey.user.id = :currentUserId",
                Long.class)
            .setParameter("articleId", article.getId())
            .setParameter("currentUserId", currentUserId)
            .getSingleResult();
    return count > 0;
  }

  @Override
  public long favoritesCount(Article article) {
    return em.createQuery(
            "select count(f) from FavoriteRelationshipEntity f where f.primaryKey.article.id = :articleId",
            Long.class)
        .setParameter("articleId", article.getId())
        .getSingleResult();
  }

  @Override
  public Optional<FavoriteRelationship> findByArticleIdAndUserId(
      UUID articleId, UUID currentUserId) {
    return em
        .createQuery(
            "from FavoriteRelationshipEntity f where f.primaryKey.article.id = :articleId and f.primaryKey.user.id = :currentUserId",
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
    if (favoriteRelationshipEntity != null) {
      em.remove(favoriteRelationshipEntity);
    }
  }
}
