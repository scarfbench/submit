package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.CreateComment;
import org.example.realworldapi.domain.feature.FindArticleBySlug;
import org.example.realworldapi.domain.feature.FindUserById;
import org.example.realworldapi.domain.model.comment.Comment;
import org.example.realworldapi.domain.model.comment.CommentBuilder;
import org.example.realworldapi.domain.model.comment.CommentRepository;
import org.example.realworldapi.domain.model.comment.NewCommentInput;
import org.springframework.stereotype.Service;

@Service
public class CreateCommentImpl implements CreateComment {

    private final CommentRepository commentRepository;
    private final FindUserById findUserById;
    private final FindArticleBySlug findArticleBySlug;
    private final CommentBuilder commentBuilder;

    public CreateCommentImpl(
            CommentRepository commentRepository,
            FindUserById findUserById,
            FindArticleBySlug findArticleBySlug,
            CommentBuilder commentBuilder) {
        this.commentRepository = commentRepository;
        this.findUserById = findUserById;
        this.findArticleBySlug = findArticleBySlug;
        this.commentBuilder = commentBuilder;
    }

    @Override
    public Comment handle(NewCommentInput newCommentInput) {
        final var author = findUserById.handle(newCommentInput.getAuthorId());
        final var article = findArticleBySlug.handle(newCommentInput.getArticleSlug());
        final var comment = commentBuilder.build(author, article, newCommentInput.getBody());
        commentRepository.save(comment);
        return comment;
    }
}
