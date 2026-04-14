package org.example.realworldapi.infrastructure.repository;

import jakarta.persistence.Query;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.comment.Comment;
import org.example.realworldapi.domain.model.comment.CommentRepository;
import org.example.realworldapi.infrastructure.repository.entity.CommentEntity;
import org.example.realworldapi.infrastructure.repository.entity.EntityUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class CommentDAO extends AbstractDAO<CommentEntity, UUID> implements CommentRepository {

    private final EntityUtils entityUtils;

    public CommentDAO(EntityUtils entityUtils) {
        this.entityUtils = entityUtils;
    }

    @Override
    public void save(Comment comment) {
        final var authorEntity = findUserEntityById(comment.getAuthor().getId());
        final var articleEntity = findArticleEntityById(comment.getArticle().getId());
        em.persist(new CommentEntity(authorEntity, articleEntity, comment));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Comment> findByIdAndAuthor(UUID commentId, UUID authorId) {
        Query query = em.createNamedQuery("CommentEntity.findByIdAndAuthor_Id");
        query.setParameter("commentId", commentId);
        query.setParameter("authorId", authorId);
        List<CommentEntity> resultList = query.getResultList();
        if (!resultList.isEmpty()) {
            return Optional.of(entityUtils.comment(resultList.get(0)));
        }
        return Optional.empty();
    }

    @Override
    public void delete(Comment comment) {
        final var commentEntity = findCommentEntityById(comment.getId());
        em.remove(commentEntity);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Comment> findCommentsByArticle(Article article) {
        Query query = em.createNamedQuery("CommentEntity.findByArticle_Id");
        query.setParameter("articleId", article.getId());
        List<CommentEntity> resultList = query.getResultList();
        return resultList.stream().map(entityUtils::comment).collect(Collectors.toList());
    }
}
