package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.exception.CommentNotFoundException;
import org.example.realworldapi.domain.feature.FindCommentByIdAndAuthor;
import org.example.realworldapi.domain.model.comment.Comment;
import org.example.realworldapi.domain.model.comment.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FindCommentByIdAndAuthorImpl implements FindCommentByIdAndAuthor {

    @Autowired
    private CommentRepository commentRepository;

    @Override
    public Comment handle(UUID commentId, UUID authorId) {
        return commentRepository
                .findByIdAndAuthor(commentId, authorId)
                .orElseThrow(CommentNotFoundException::new);
    }
}
