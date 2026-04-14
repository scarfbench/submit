package org.example.realworldapi.application.web.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.realworldapi.application.web.model.request.NewArticleRequestWrapper;
import org.example.realworldapi.application.web.model.request.NewCommentRequestWrapper;
import org.example.realworldapi.application.web.model.request.UpdateArticleRequestWrapper;
import org.example.realworldapi.application.web.resource.utils.ResourceUtils;
import org.example.realworldapi.domain.feature.*;
import org.example.realworldapi.domain.model.article.ArticleFilter;
import org.example.realworldapi.domain.model.comment.DeleteCommentInput;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/articles")
public class ArticlesResource {

    private final FindArticlesByFilter findArticlesByFilter;
    private final CreateArticle createArticle;
    private final FindMostRecentArticlesByFilter findMostRecentArticlesByFilter;
    private final FindArticleBySlug findArticleBySlug;
    private final UpdateArticleBySlug updateArticleBySlug;
    private final DeleteArticleBySlug deleteArticleBySlug;
    private final CreateComment createComment;
    private final DeleteComment deleteComment;
    private final FindCommentsByArticleSlug findCommentsByArticleSlug;
    private final FavoriteArticle favoriteArticle;
    private final UnfavoriteArticle unfavoriteArticle;
    private final ResourceUtils resourceUtils;
    private final ObjectMapper objectMapper;
    private final ObjectMapper noWrapObjectMapper;

    public ArticlesResource(
            FindArticlesByFilter findArticlesByFilter,
            CreateArticle createArticle,
            FindMostRecentArticlesByFilter findMostRecentArticlesByFilter,
            FindArticleBySlug findArticleBySlug,
            UpdateArticleBySlug updateArticleBySlug,
            DeleteArticleBySlug deleteArticleBySlug,
            CreateComment createComment,
            DeleteComment deleteComment,
            FindCommentsByArticleSlug findCommentsByArticleSlug,
            FavoriteArticle favoriteArticle,
            UnfavoriteArticle unfavoriteArticle,
            ResourceUtils resourceUtils,
            @Qualifier("wrappingObjectMapper") ObjectMapper objectMapper,
            @Qualifier("noWrapRootValueObjectMapper") ObjectMapper noWrapObjectMapper) {
        this.findArticlesByFilter = findArticlesByFilter;
        this.createArticle = createArticle;
        this.findMostRecentArticlesByFilter = findMostRecentArticlesByFilter;
        this.findArticleBySlug = findArticleBySlug;
        this.updateArticleBySlug = updateArticleBySlug;
        this.deleteArticleBySlug = deleteArticleBySlug;
        this.createComment = createComment;
        this.deleteComment = deleteComment;
        this.findCommentsByArticleSlug = findCommentsByArticleSlug;
        this.favoriteArticle = favoriteArticle;
        this.unfavoriteArticle = unfavoriteArticle;
        this.resourceUtils = resourceUtils;
        this.objectMapper = objectMapper;
        this.noWrapObjectMapper = noWrapObjectMapper;
    }

    @GetMapping(value = "/feed", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> feed(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "0") int limit) throws JsonProcessingException {
        final var loggedUserId = resourceUtils.getLoggedUserId();
        final var articlesFilter =
                new ArticleFilter(offset, resourceUtils.getLimit(limit), loggedUserId, null, null, null);
        final var articlesPageResult = findMostRecentArticlesByFilter.handle(articlesFilter);
        return ResponseEntity.ok(noWrapObjectMapper.writeValueAsString(
                resourceUtils.articlesResponse(articlesPageResult, loggedUserId)));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getArticles(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "0") int limit,
            @RequestParam(required = false) List<String> tag,
            @RequestParam(required = false) List<String> author,
            @RequestParam(required = false) List<String> favorited) throws JsonProcessingException {
        final var loggedUserId = resourceUtils.getLoggedUserId();
        final var filter =
                new ArticleFilter(
                        offset, resourceUtils.getLimit(limit), loggedUserId, tag, author, favorited);
        final var articlesPageResult = findArticlesByFilter.handle(filter);
        return ResponseEntity.ok(noWrapObjectMapper.writeValueAsString(
                resourceUtils.articlesResponse(articlesPageResult, loggedUserId)));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<String> create(
            @Valid @RequestBody NewArticleRequestWrapper newArticleRequest) throws JsonProcessingException {
        final var loggedUserId = resourceUtils.getLoggedUserId();
        final var article = createArticle.handle(newArticleRequest.getArticle().toNewArticleInput(loggedUserId));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(objectMapper.writeValueAsString(resourceUtils.articleResponse(article, loggedUserId)));
    }

    @GetMapping(value = "/{slug}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> findBySlug(@PathVariable String slug) throws JsonProcessingException {
        final var article = findArticleBySlug.handle(slug);
        return ResponseEntity.ok(objectMapper.writeValueAsString(resourceUtils.articleResponse(article, null)));
    }

    @PutMapping(value = "/{slug}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<String> update(
            @PathVariable String slug,
            @Valid @RequestBody UpdateArticleRequestWrapper updateArticleRequest) throws JsonProcessingException {
        final var loggedUserId = resourceUtils.getLoggedUserId();
        final var updatedArticle =
                updateArticleBySlug.handle(updateArticleRequest.getArticle().toUpdateArticleInput(loggedUserId, slug));
        return ResponseEntity.ok(objectMapper.writeValueAsString(resourceUtils.articleResponse(updatedArticle, null)));
    }

    @DeleteMapping(value = "/{slug}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable String slug) {
        final var loggedUserId = resourceUtils.getLoggedUserId();
        deleteArticleBySlug.handle(loggedUserId, slug);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/{slug}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getCommentsBySlug(@PathVariable String slug) throws JsonProcessingException {
        final var loggedUserId = resourceUtils.getLoggedUserId();
        final var comments = findCommentsByArticleSlug.handle(slug);
        return ResponseEntity.ok(noWrapObjectMapper.writeValueAsString(
                resourceUtils.commentsResponse(comments, loggedUserId)));
    }

    @PostMapping(value = "/{slug}/comments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<String> createComment(
            @PathVariable String slug,
            @Valid @RequestBody NewCommentRequestWrapper newCommentRequest) throws JsonProcessingException {
        final var loggedUserId = resourceUtils.getLoggedUserId();
        final var comment =
                createComment.handle(newCommentRequest.getComment().toNewCommentInput(loggedUserId, slug));
        return ResponseEntity.ok(objectMapper.writeValueAsString(
                resourceUtils.commentResponse(comment, loggedUserId)));
    }

    @DeleteMapping(value = "/{slug}/comments/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<Void> deleteComment(
            @PathVariable String slug,
            @PathVariable UUID id) {
        final var loggedUserId = resourceUtils.getLoggedUserId();
        deleteComment.handle(new DeleteCommentInput(id, loggedUserId, slug));
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{slug}/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<String> favoriteArticle(@PathVariable String slug) throws JsonProcessingException {
        final var loggedUserId = resourceUtils.getLoggedUserId();
        favoriteArticle.handle(slug, loggedUserId);
        final var article = findArticleBySlug.handle(slug);
        return ResponseEntity.ok(objectMapper.writeValueAsString(
                resourceUtils.articleResponse(article, loggedUserId)));
    }

    @DeleteMapping(value = "/{slug}/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<String> unfavoriteArticle(@PathVariable String slug) throws JsonProcessingException {
        final var loggedUserId = resourceUtils.getLoggedUserId();
        unfavoriteArticle.handle(slug, loggedUserId);
        final var article = findArticleBySlug.handle(slug);
        return ResponseEntity.ok(objectMapper.writeValueAsString(
                resourceUtils.articleResponse(article, loggedUserId)));
    }
}
