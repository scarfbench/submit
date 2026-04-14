package org.example.realworldapi.infrastructure.repository.hibernate.panache;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.comment.Comment;
import org.example.realworldapi.domain.model.comment.CommentRepository;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.ArticleEntity;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.CommentEntity;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.EntityUtils;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.UserEntity;
import org.springframework.stereotype.Repository;

@Repository
public class CommentRepositoryPanache implements CommentRepository {

  @PersistenceContext
  private EntityManager entityManager;

  private final EntityUtils entityUtils;

  public CommentRepositoryPanache(EntityUtils entityUtils) {
    this.entityUtils = entityUtils;
  }

  @Override
  public void save(Comment comment) {
    final var authorEntity = entityManager.find(UserEntity.class, comment.getAuthor().getId());
    final var articleEntity = entityManager.find(ArticleEntity.class, comment.getArticle().getId());
    entityManager.persist(new CommentEntity(authorEntity, articleEntity, comment));
  }

  @Override
  public Optional<Comment> findByIdAndAuthor(UUID commentId, UUID authorId) {
    var results = entityManager.createQuery(
            "from CommentEntity where id = :commentId and author.id = :authorId", CommentEntity.class)
        .setParameter("commentId", commentId)
        .setParameter("authorId", authorId)
        .getResultList();
    return results.stream().findFirst().map(entityUtils::comment);
  }

  @Override
  public void delete(Comment comment) {
    final var commentEntity = entityManager.find(CommentEntity.class, comment.getId());
    if (commentEntity != null) {
      entityManager.remove(commentEntity);
    }
  }

  @Override
  public List<Comment> findCommentsByArticle(Article article) {
    var commentsEntity = entityManager.createQuery(
            "from CommentEntity where article.id = :articleId", CommentEntity.class)
        .setParameter("articleId", article.getId())
        .getResultList();
    return commentsEntity.stream().map(entityUtils::comment).collect(Collectors.toList());
  }
}
