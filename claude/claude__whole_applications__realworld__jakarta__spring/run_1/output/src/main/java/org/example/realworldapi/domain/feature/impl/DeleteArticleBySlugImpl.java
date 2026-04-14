package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.DeleteArticleBySlug;
import org.example.realworldapi.domain.feature.FindArticleByAuthorAndSlug;
import org.example.realworldapi.domain.model.article.ArticleRepository;
import org.example.realworldapi.domain.model.article.FavoriteRelationship;
import org.example.realworldapi.domain.model.article.FavoriteRelationshipRepository;
import org.example.realworldapi.domain.model.article.TagRelationshipRepository;
import org.example.realworldapi.domain.model.comment.Comment;
import org.example.realworldapi.domain.model.comment.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DeleteArticleBySlugImpl implements DeleteArticleBySlug {

    private final FindArticleByAuthorAndSlug findArticleByAuthorAndSlug;
    private final ArticleRepository articleRepository;
    private final TagRelationshipRepository tagRelationshipRepository;
    private final CommentRepository commentRepository;
    private final FavoriteRelationshipRepository favoriteRelationshipRepository;

    public DeleteArticleBySlugImpl(
            FindArticleByAuthorAndSlug findArticleByAuthorAndSlug,
            ArticleRepository articleRepository,
            TagRelationshipRepository tagRelationshipRepository,
            CommentRepository commentRepository,
            FavoriteRelationshipRepository favoriteRelationshipRepository) {
        this.findArticleByAuthorAndSlug = findArticleByAuthorAndSlug;
        this.articleRepository = articleRepository;
        this.tagRelationshipRepository = tagRelationshipRepository;
        this.commentRepository = commentRepository;
        this.favoriteRelationshipRepository = favoriteRelationshipRepository;
    }

    @Override
    public void handle(UUID authorId, String slug) {
        final var article = findArticleByAuthorAndSlug.handle(authorId, slug);
        tagRelationshipRepository.delete(article);
        Optional<FavoriteRelationship> favorite = favoriteRelationshipRepository.findByArticleIdAndUserId(article.getId(), authorId);
        favorite.ifPresent(favoriteRelationship -> favoriteRelationshipRepository.delete(favoriteRelationship));
        List<Comment> comments = commentRepository.findCommentsByArticle(article);
        comments.forEach(c -> commentRepository.delete(c));
        articleRepository.delete(article);
    }
}
