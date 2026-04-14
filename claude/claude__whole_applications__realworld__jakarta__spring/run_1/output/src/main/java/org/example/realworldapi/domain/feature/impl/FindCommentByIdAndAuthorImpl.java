package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.exception.CommentNotFoundException;
import org.example.realworldapi.domain.feature.FindCommentByIdAndAuthor;
import org.example.realworldapi.domain.model.comment.Comment;
import org.example.realworldapi.domain.model.comment.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FindCommentByIdAndAuthorImpl implements FindCommentByIdAndAuthor {

    private final CommentRepository commentRepository;

    public FindCommentByIdAndAuthorImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public Comment handle(UUID commentId, UUID authorId) {
        return commentRepository
                .findByIdAndAuthor(commentId, authorId)
                .orElseThrow(CommentNotFoundException::new);
    }
}
