package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.DeleteComment;
import org.example.realworldapi.domain.feature.FindCommentByIdAndAuthor;
import org.example.realworldapi.domain.model.comment.CommentRepository;
import org.example.realworldapi.domain.model.comment.DeleteCommentInput;
import org.springframework.stereotype.Service;

@Service
public class DeleteCommentImpl implements DeleteComment {

    private final FindCommentByIdAndAuthor findCommentByIdAndAuthor;
    private final CommentRepository commentRepository;
    public DeleteCommentImpl(FindCommentByIdAndAuthor findCommentByIdAndAuthor, CommentRepository commentRepository) {
        this.findCommentByIdAndAuthor = findCommentByIdAndAuthor;
        this.commentRepository = commentRepository;
    }

    @Override
    public void handle(DeleteCommentInput deleteCommentInput) {
        final var comment =
                findCommentByIdAndAuthor.handle(
                        deleteCommentInput.getCommentId(), deleteCommentInput.getAuthorId());
        commentRepository.delete(comment);
    }
}
