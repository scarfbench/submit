package org.example.realworldapi.infrastructure.repository.hibernate.panache;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.example.realworldapi.EntityManagerProducer;
import org.example.realworldapi.domain.model.article.FavoriteRelationship;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.*;

public abstract class AbstractPanacheRepository {

  /**
   * Get the current thread-local EntityManager.
   * This ensures all repositories in the same request share the same EM.
   */
  protected EntityManager getEm() {
    return EntityManagerProducer.getCurrentEntityManager();
  }

  protected UserEntity findUserEntityById(UUID id) {
    return getEm().find(UserEntity.class, id);
  }

  protected TagEntity findTagEntityById(UUID id) {
    return getEm().find(TagEntity.class, id);
  }

  protected ArticleEntity findArticleEntityById(UUID id) {
    return getEm().find(ArticleEntity.class, id);
  }

  protected CommentEntity findCommentEntityById(UUID id) {
    return getEm().find(CommentEntity.class, id);
  }

  protected FavoriteRelationshipEntity findFavoriteRelationshipEntityByKey(
      FavoriteRelationship favoriteRelationship) {

    final var userEntity = findUserEntityById(favoriteRelationship.getUser().getId());
    final var articleEntity = findArticleEntityById(favoriteRelationship.getArticle().getId());

    final var favoriteRelationshipEntityKey = new FavoriteRelationshipEntityKey();
    favoriteRelationshipEntityKey.setUser(userEntity);
    favoriteRelationshipEntityKey.setArticle(articleEntity);

    return getEm().find(FavoriteRelationshipEntity.class, favoriteRelationshipEntityKey);
  }

  protected boolean isNotEmpty(List<?> list) {
    return list != null && !list.isEmpty();
  }

  protected List<String> toUpperCase(List<String> subjectList) {
    return subjectList.stream().map(String::toUpperCase).collect(Collectors.toList());
  }
}
