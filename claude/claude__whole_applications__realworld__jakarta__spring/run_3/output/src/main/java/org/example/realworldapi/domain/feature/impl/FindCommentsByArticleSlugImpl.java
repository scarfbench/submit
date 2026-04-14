package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.FindArticleBySlug;
import org.example.realworldapi.domain.feature.FindCommentsByArticleSlug;
import org.example.realworldapi.domain.model.comment.Comment;
import org.example.realworldapi.domain.model.comment.CommentRepository;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FindCommentsByArticleSlugImpl implements FindCommentsByArticleSlug {

    private final FindArticleBySlug findArticleBySlug;
    private final CommentRepository commentRepository;
    public FindCommentsByArticleSlugImpl(FindArticleBySlug findArticleBySlug, CommentRepository commentRepository) {
        this.findArticleBySlug = findArticleBySlug;
        this.commentRepository = commentRepository;
    }

    @Override
    public List<Comment> handle(String slug) {
        final var article = findArticleBySlug.handle(slug);
        return commentRepository.findCommentsByArticle(article);
    }
}
