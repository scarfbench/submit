package org.example.realworldapi.infrastructure.repository.hibernate.panache;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.comment.Comment;
import org.example.realworldapi.domain.model.comment.CommentRepository;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.CommentEntity;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.EntityUtils;

@ApplicationScoped
public class CommentRepositoryPanache extends AbstractPanacheRepository<CommentEntity, UUID>
    implements CommentRepository {

  @Inject EntityUtils entityUtils;

  @Override
  public void save(Comment comment) {
    final var authorEntity = findUserEntityById(comment.getAuthor().getId());
    final var articleEntity = findArticleEntityById(comment.getArticle().getId());
    em.persist(new CommentEntity(authorEntity, articleEntity, comment));
  }

  @Override
  public Optional<Comment> findByIdAndAuthor(UUID commentId, UUID authorId) {
    return em.createQuery(
            "FROM CommentEntity WHERE id = :commentId and author.id = :authorId",
            CommentEntity.class)
        .setParameter("commentId", commentId)
        .setParameter("authorId", authorId)
        .getResultStream()
        .findFirst()
        .map(entityUtils::comment);
  }

  @Override
  public void delete(Comment comment) {
    final var commentEntity = findCommentEntityById(comment.getId());
    em.remove(em.merge(commentEntity));
  }

  @Override
  public List<Comment> findCommentsByArticle(Article article) {
    final var commentsEntity = em.createQuery(
            "FROM CommentEntity WHERE article.id = :articleId",
            CommentEntity.class)
        .setParameter("articleId", article.getId())
        .getResultList();
    return commentsEntity.stream().map(entityUtils::comment).collect(Collectors.toList());
  }
}
