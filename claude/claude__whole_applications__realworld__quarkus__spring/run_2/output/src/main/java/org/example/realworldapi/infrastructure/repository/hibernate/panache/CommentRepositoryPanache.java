package org.example.realworldapi.infrastructure.repository.hibernate.panache;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.comment.Comment;
import org.example.realworldapi.domain.model.comment.CommentRepository;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.CommentEntity;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.EntityUtils;
import org.springframework.stereotype.Repository;

@Repository
public class CommentRepositoryPanache extends AbstractPanacheRepository implements CommentRepository {

  private final EntityUtils entityUtils;

  public CommentRepositoryPanache(EntityUtils entityUtils) {
    this.entityUtils = entityUtils;
  }

  @Override
  public void save(Comment comment) {
    final var authorEntity = findUserEntityById(comment.getAuthor().getId());
    final var articleEntity = findArticleEntityById(comment.getArticle().getId());
    entityManager.persist(new CommentEntity(authorEntity, articleEntity, comment));
  }

  @Override
  public Optional<Comment> findByIdAndAuthor(UUID commentId, UUID authorId) {
    return entityManager.createQuery(
            "from CommentEntity where id = :commentId and author.id = :authorId", CommentEntity.class)
        .setParameter("commentId", commentId)
        .setParameter("authorId", authorId)
        .getResultStream()
        .findFirst()
        .map(entityUtils::comment);
  }

  @Override
  public void delete(Comment comment) {
    final var commentEntity = findCommentEntityById(comment.getId());
    entityManager.remove(commentEntity);
  }

  @Override
  public List<Comment> findCommentsByArticle(Article article) {
    return entityManager.createQuery(
            "from CommentEntity where article.id = :articleId", CommentEntity.class)
        .setParameter("articleId", article.getId())
        .getResultList()
        .stream()
        .map(entityUtils::comment)
        .collect(Collectors.toList());
  }
}
