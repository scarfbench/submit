package org.example.realworldapi.application.web.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.realworldapi.application.web.model.request.NewArticleRequestWrapper;
import org.example.realworldapi.application.web.model.request.NewCommentRequestWrapper;
import org.example.realworldapi.application.web.model.request.UpdateArticleRequestWrapper;
import org.example.realworldapi.application.web.resource.utils.ResourceUtils;
import org.example.realworldapi.domain.feature.*;
import org.example.realworldapi.domain.model.article.ArticleFilter;
import org.example.realworldapi.domain.model.comment.DeleteCommentInput;
import org.example.realworldapi.domain.model.constants.ValidationMessages;
import org.example.realworldapi.infrastructure.web.security.annotation.Authenticated;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/articles")
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
            @Qualifier("wrapRootValueObjectMapper") ObjectMapper objectMapper,
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
    @Authenticated
    public ResponseEntity<String> feed(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "0") int limit,
            @RequestAttribute(name = "loggedUserId", required = false) UUID loggedUserId) throws JsonProcessingException {
        final var articlesFilter = new ArticleFilter(offset, resourceUtils.getLimit(limit), loggedUserId, null, null, null);
        final var articlesPageResult = findMostRecentArticlesByFilter.handle(articlesFilter);
        return ResponseEntity.ok(noWrapObjectMapper.writeValueAsString(resourceUtils.articlesResponse(articlesPageResult, loggedUserId)));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Authenticated(optional = true)
    public ResponseEntity<String> getArticles(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "0") int limit,
            @RequestParam(required = false) List<String> tag,
            @RequestParam(required = false) List<String> author,
            @RequestParam(required = false) List<String> favorited,
            @RequestAttribute(name = "loggedUserId", required = false) UUID loggedUserId) throws JsonProcessingException {
        final var filter =
                new ArticleFilter(
                        offset, resourceUtils.getLimit(limit), loggedUserId, tag, author, favorited);
        final var articlesPageResult = findArticlesByFilter.handle(filter);
        return ResponseEntity.ok(noWrapObjectMapper.writeValueAsString(resourceUtils.articlesResponse(articlesPageResult, loggedUserId)));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    @Authenticated
    public ResponseEntity<String> create(
            @Valid @RequestBody NewArticleRequestWrapper newArticleRequest,
            @RequestAttribute(name = "loggedUserId", required = false) UUID loggedUserId) throws JsonProcessingException {
        final var article = createArticle.handle(newArticleRequest.getArticle().toNewArticleInput(loggedUserId));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(objectMapper.writeValueAsString(resourceUtils.articleResponse(article, loggedUserId)));
    }

    @GetMapping(value = "/{slug}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> findBySlug(
            @PathVariable @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String slug) throws JsonProcessingException {
        final var article = findArticleBySlug.handle(slug);
        return ResponseEntity.ok(objectMapper.writeValueAsString(resourceUtils.articleResponse(article, null)));
    }

    @PutMapping(value = "/{slug}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    @Authenticated
    public ResponseEntity<String> update(
            @PathVariable @NotBlank String slug,
            @Valid @RequestBody UpdateArticleRequestWrapper updateArticleRequest,
            @RequestAttribute(name = "loggedUserId", required = false) UUID loggedUserId) throws JsonProcessingException {
        final var updatedArticle =
                updateArticleBySlug.handle(updateArticleRequest.getArticle().toUpdateArticleInput(loggedUserId, slug));
        return ResponseEntity.ok(objectMapper.writeValueAsString(resourceUtils.articleResponse(updatedArticle, null)));
    }

    @DeleteMapping(value = "/{slug}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    @Authenticated
    public ResponseEntity<Void> delete(
            @PathVariable @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String slug,
            @RequestAttribute(name = "loggedUserId", required = false) UUID loggedUserId) {
        deleteArticleBySlug.handle(loggedUserId, slug);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/{slug}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    @Authenticated(optional = true)
    public ResponseEntity<String> getCommentsBySlug(
            @PathVariable @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String slug,
            @RequestAttribute(name = "loggedUserId", required = false) UUID loggedUserId) throws JsonProcessingException {
        final var comments = findCommentsByArticleSlug.handle(slug);
        return ResponseEntity.ok(noWrapObjectMapper.writeValueAsString(resourceUtils.commentsResponse(comments, loggedUserId)));
    }

    @PostMapping(value = "/{slug}/comments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    @Authenticated
    public ResponseEntity<String> createComment(
            @PathVariable @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String slug,
            @Valid @RequestBody NewCommentRequestWrapper newCommentRequest,
            @RequestAttribute(name = "loggedUserId", required = false) UUID loggedUserId) throws JsonProcessingException {
        final var comment =
                createComment.handle(newCommentRequest.getComment().toNewCommentInput(loggedUserId, slug));
        return ResponseEntity.ok(objectMapper.writeValueAsString(resourceUtils.commentResponse(comment, loggedUserId)));
    }

    @DeleteMapping(value = "/{slug}/comments/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    @Authenticated
    public ResponseEntity<Void> deleteComment(
            @PathVariable @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String slug,
            @PathVariable @NotNull(message = ValidationMessages.COMMENT_ID_MUST_BE_NOT_NULL) UUID id,
            @RequestAttribute(name = "loggedUserId", required = false) UUID loggedUserId) {
        deleteComment.handle(new DeleteCommentInput(id, loggedUserId, slug));
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{slug}/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    @Authenticated
    public ResponseEntity<String> favoriteArticle(
            @PathVariable @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String slug,
            @RequestAttribute(name = "loggedUserId", required = false) UUID loggedUserId) throws JsonProcessingException {
        favoriteArticle.handle(slug, loggedUserId);
        final var article = findArticleBySlug.handle(slug);
        return ResponseEntity.ok(objectMapper.writeValueAsString(resourceUtils.articleResponse(article, loggedUserId)));
    }

    @DeleteMapping(value = "/{slug}/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    @Authenticated
    public ResponseEntity<String> unfavoriteArticle(
            @PathVariable @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String slug,
            @RequestAttribute(name = "loggedUserId", required = false) UUID loggedUserId) throws JsonProcessingException {
        unfavoriteArticle.handle(slug, loggedUserId);
        final var article = findArticleBySlug.handle(slug);
        return ResponseEntity.ok(objectMapper.writeValueAsString(resourceUtils.articleResponse(article, loggedUserId)));
    }
}
