package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.FindArticleBySlug;
import org.example.realworldapi.domain.feature.FindCommentsByArticleSlug;
import org.example.realworldapi.domain.model.comment.Comment;
import org.example.realworldapi.domain.model.comment.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FindCommentsByArticleSlugImpl implements FindCommentsByArticleSlug {

    @Autowired
    private FindArticleBySlug findArticleBySlug;
    @Autowired
    private CommentRepository commentRepository;

    @Override
    public List<Comment> handle(String slug) {
        final var article = findArticleBySlug.handle(slug);
        return commentRepository.findCommentsByArticle(article);
    }
}
