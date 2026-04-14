package org.example.realworldapi.domain.model.comment;

import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.user.User;
import org.example.realworldapi.domain.validator.ModelValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class CommentBuilder {

    @Autowired
    private ModelValidator modelValidator;

    public Comment build(User author, Article article, String body) {
        final var createdAt = LocalDateTime.now();
        return modelValidator.validate(
                new Comment(UUID.randomUUID(), author, article, body, createdAt, createdAt));
    }

    public Comment build(
            UUID id,
            User author,
            Article article,
            String body,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        return modelValidator.validate(new Comment(id, author, article, body, createdAt, updatedAt));
    }
}
