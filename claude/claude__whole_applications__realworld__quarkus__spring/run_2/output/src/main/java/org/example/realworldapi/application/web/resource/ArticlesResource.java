package org.example.realworldapi.application.web.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.example.realworldapi.application.web.model.request.NewArticleRequest;
import org.example.realworldapi.application.web.model.request.NewCommentRequest;
import org.example.realworldapi.application.web.model.request.UpdateArticleRequest;
import org.example.realworldapi.application.web.resource.utils.ResourceUtils;
import org.example.realworldapi.domain.feature.*;
import org.example.realworldapi.domain.model.article.ArticleFilter;
import org.example.realworldapi.domain.model.comment.DeleteCommentInput;
import org.example.realworldapi.domain.model.constants.ValidationMessages;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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
  private final ObjectMapper noWrapMapper;
  private final ObjectMapper wrapMapper;
  private final ResourceUtils resourceUtils;

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
      @Qualifier("noWrapRootValueObjectMapper") ObjectMapper noWrapMapper,
      @Qualifier("wrapRootValueObjectMapper") ObjectMapper wrapMapper,
      ResourceUtils resourceUtils) {
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
    this.noWrapMapper = noWrapMapper;
    this.wrapMapper = wrapMapper;
    this.resourceUtils = resourceUtils;
  }

  @GetMapping(value = "/feed", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> feed(
      @RequestParam(defaultValue = "0") int offset,
      @RequestParam(defaultValue = "0") int limit,
      Principal principal)
      throws JsonProcessingException {
    final var loggedUserId = resourceUtils.getLoggedUserId(principal);
    final var articlesFilter =
        new ArticleFilter(offset, resourceUtils.getLimit(limit), loggedUserId, null, null, null);
    final var articlesPageResult = findMostRecentArticlesByFilter.handle(articlesFilter);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(noWrapMapper.writeValueAsString(
            resourceUtils.articlesResponse(articlesPageResult, loggedUserId)));
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getArticles(
      @RequestParam(defaultValue = "0") int offset,
      @RequestParam(defaultValue = "0") int limit,
      @RequestParam(required = false) List<String> tag,
      @RequestParam(required = false) List<String> author,
      @RequestParam(required = false) List<String> favorited,
      Principal principal)
      throws JsonProcessingException {
    final var loggedUserId = resourceUtils.getLoggedUserId(principal);
    final var filter =
        new ArticleFilter(
            offset, resourceUtils.getLimit(limit), loggedUserId, tag, author, favorited);
    final var articlesPageResult = findArticlesByFilter.handle(filter);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(noWrapMapper.writeValueAsString(
            resourceUtils.articlesResponse(articlesPageResult, loggedUserId)));
  }

  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  public ResponseEntity<String> create(
      @Valid @RequestBody @NotNull(message = ValidationMessages.REQUEST_BODY_MUST_BE_NOT_NULL)
          NewArticleRequest newArticleRequest,
      Principal principal)
      throws JsonProcessingException {
    final var loggedUserId = resourceUtils.getLoggedUserId(principal);
    final var article = createArticle.handle(newArticleRequest.toNewArticleInput(loggedUserId));
    return ResponseEntity.status(HttpStatus.CREATED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(wrapMapper.writeValueAsString(resourceUtils.articleResponse(article, loggedUserId)));
  }

  @GetMapping(value = "/{slug}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> findBySlug(
      @PathVariable @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String slug)
      throws JsonProcessingException {
    final var article = findArticleBySlug.handle(slug);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(wrapMapper.writeValueAsString(resourceUtils.articleResponse(article, null)));
  }

  @PutMapping(value = "/{slug}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  public ResponseEntity<String> update(
      @PathVariable @NotBlank String slug,
      @Valid @RequestBody @NotNull UpdateArticleRequest updateArticleRequest,
      Principal principal)
      throws JsonProcessingException {
    final var loggedUserId = resourceUtils.getLoggedUserId(principal);
    final var updatedArticle =
        updateArticleBySlug.handle(updateArticleRequest.toUpdateArticleInput(loggedUserId, slug));
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(wrapMapper.writeValueAsString(resourceUtils.articleResponse(updatedArticle, null)));
  }

  @DeleteMapping("/{slug}")
  @Transactional
  public ResponseEntity<?> delete(
      @PathVariable @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String slug,
      Principal principal) {
    final var loggedUserId = resourceUtils.getLoggedUserId(principal);
    deleteArticleBySlug.handle(loggedUserId, slug);
    return ResponseEntity.ok().build();
  }

  @GetMapping(value = "/{slug}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getCommentsBySlug(
      @PathVariable @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String slug,
      Principal principal)
      throws JsonProcessingException {
    final var loggedUserId = resourceUtils.getLoggedUserId(principal);
    final var comments = findCommentsByArticleSlug.handle(slug);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(noWrapMapper.writeValueAsString(resourceUtils.commentsResponse(comments, loggedUserId)));
  }

  @PostMapping(value = "/{slug}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  public ResponseEntity<String> createComment(
      @PathVariable @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String slug,
      @Valid @RequestBody NewCommentRequest newCommentRequest,
      Principal principal)
      throws JsonProcessingException {
    final var loggedUserId = resourceUtils.getLoggedUserId(principal);
    final var comment =
        createComment.handle(newCommentRequest.toNewCommentInput(loggedUserId, slug));
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(wrapMapper.writeValueAsString(resourceUtils.commentResponse(comment, loggedUserId)));
  }

  @DeleteMapping("/{slug}/comments/{id}")
  @Transactional
  public ResponseEntity<?> deleteComment(
      @PathVariable @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String slug,
      @PathVariable @NotNull(message = ValidationMessages.COMMENT_ID_MUST_BE_NOT_NULL) UUID id,
      Principal principal) {
    final var loggedUserId = resourceUtils.getLoggedUserId(principal);
    deleteComment.handle(new DeleteCommentInput(id, loggedUserId, slug));
    return ResponseEntity.ok().build();
  }

  @PostMapping(value = "/{slug}/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  public ResponseEntity<String> favoriteArticle(
      @PathVariable @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String slug,
      Principal principal)
      throws JsonProcessingException {
    final var loggedUserId = resourceUtils.getLoggedUserId(principal);
    favoriteArticle.handle(slug, loggedUserId);
    final var article = findArticleBySlug.handle(slug);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(wrapMapper.writeValueAsString(resourceUtils.articleResponse(article, loggedUserId)));
  }

  @DeleteMapping(value = "/{slug}/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  public ResponseEntity<String> unfavoriteArticle(
      @PathVariable @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String slug,
      Principal principal)
      throws JsonProcessingException {
    final var loggedUserId = resourceUtils.getLoggedUserId(principal);
    unfavoriteArticle.handle(slug, loggedUserId);
    final var article = findArticleBySlug.handle(slug);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(wrapMapper.writeValueAsString(resourceUtils.articleResponse(article, loggedUserId)));
  }
}
