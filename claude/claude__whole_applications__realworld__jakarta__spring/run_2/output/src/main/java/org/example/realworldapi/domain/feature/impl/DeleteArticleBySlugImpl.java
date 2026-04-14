package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.DeleteArticleBySlug;
import org.example.realworldapi.domain.feature.FindArticleByAuthorAndSlug;
import org.example.realworldapi.domain.model.article.ArticleRepository;
import org.example.realworldapi.domain.model.article.FavoriteRelationship;
import org.example.realworldapi.domain.model.article.FavoriteRelationshipRepository;
import org.example.realworldapi.domain.model.article.TagRelationshipRepository;
import org.example.realworldapi.domain.model.comment.Comment;
import org.example.realworldapi.domain.model.comment.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class DeleteArticleBySlugImpl implements DeleteArticleBySlug {

    @Autowired
    private FindArticleByAuthorAndSlug findArticleByAuthorAndSlug;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private TagRelationshipRepository tagRelationshipRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private FavoriteRelationshipRepository favoriteRelationshipRepository;

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
