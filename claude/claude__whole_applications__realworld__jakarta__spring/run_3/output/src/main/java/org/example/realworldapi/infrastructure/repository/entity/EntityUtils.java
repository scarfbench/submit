package org.example.realworldapi.infrastructure.repository.entity;

import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.ArticleModelBuilder;
import org.example.realworldapi.domain.model.article.FavoriteRelationship;
import org.example.realworldapi.domain.model.article.TagRelationship;
import org.example.realworldapi.domain.model.comment.Comment;
import org.example.realworldapi.domain.model.comment.CommentBuilder;
import org.example.realworldapi.domain.model.tag.Tag;
import org.example.realworldapi.domain.model.tag.TagBuilder;
import org.example.realworldapi.domain.model.user.User;
import org.example.realworldapi.domain.model.user.UserModelBuilder;
import org.springframework.stereotype.Component;

@Component
public class EntityUtils {

    private final UserModelBuilder userBuilder;
    private final TagBuilder tagBuilder;
    private final ArticleModelBuilder articleBuilder;
    private final CommentBuilder commentBuilder;

    public EntityUtils(UserModelBuilder userBuilder, TagBuilder tagBuilder,
                       ArticleModelBuilder articleBuilder, CommentBuilder commentBuilder) {
        this.userBuilder = userBuilder;
        this.tagBuilder = tagBuilder;
        this.articleBuilder = articleBuilder;
        this.commentBuilder = commentBuilder;
    }

    public User user(UserEntity userEntity) {
        return userBuilder.build(userEntity.getId(), userEntity.getUsername(),
                userEntity.getBio(), userEntity.getImage(),
                userEntity.getPassword(), userEntity.getEmail());
    }

    public Tag tag(TagEntity tagEntity) {
        return tagBuilder.build(tagEntity.getId(), tagEntity.getName());
    }

    public Tag tag(TagRelationshipEntity tagRelationshipEntity) {
        return tag(tagRelationshipEntity.getTag());
    }

    public TagRelationship tagRelationship(TagRelationshipEntity tagRelationshipEntity) {
        return new TagRelationship(article(tagRelationshipEntity.getArticle()), tag(tagRelationshipEntity.getTag()));
    }

    public Article article(ArticleEntity articleEntity) {
        return articleBuilder.build(
                articleEntity.getId(), articleEntity.getSlug(), articleEntity.getTitle(),
                articleEntity.getDescription(), articleEntity.getBody(),
                articleEntity.getCreatedAt(), articleEntity.getUpdatedAt(),
                user(articleEntity.getAuthor()));
    }

    public Comment comment(CommentEntity commentEntity) {
        return commentBuilder.build(
                commentEntity.getId(), user(commentEntity.getAuthor()),
                article(commentEntity.getArticle()), commentEntity.getBody(),
                commentEntity.getCreatedAt(), commentEntity.getUpdatedAt());
    }

    public FavoriteRelationship favoriteRelationship(FavoriteRelationshipEntity favoriteRelationshipEntity) {
        return new FavoriteRelationship(
                user(favoriteRelationshipEntity.getUser()),
                article(favoriteRelationshipEntity.getArticle()));
    }
}
