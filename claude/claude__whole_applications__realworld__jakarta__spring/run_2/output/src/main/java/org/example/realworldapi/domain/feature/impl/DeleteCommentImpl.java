package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.DeleteComment;
import org.example.realworldapi.domain.feature.FindCommentByIdAndAuthor;
import org.example.realworldapi.domain.model.comment.CommentRepository;
import org.example.realworldapi.domain.model.comment.DeleteCommentInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeleteCommentImpl implements DeleteComment {

    @Autowired
    private FindCommentByIdAndAuthor findCommentByIdAndAuthor;
    @Autowired
    private CommentRepository commentRepository;

    @Override
    public void handle(DeleteCommentInput deleteCommentInput) {
        final var comment =
                findCommentByIdAndAuthor.handle(
                        deleteCommentInput.getCommentId(), deleteCommentInput.getAuthorId());
        commentRepository.delete(comment);
    }
}
