package org.example.realworldapi.application.web.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.example.realworldapi.application.web.model.request.NewArticleRequest;
import org.example.realworldapi.application.web.model.request.NewCommentRequest;
import org.example.realworldapi.application.web.model.request.UpdateArticleRequest;
import org.example.realworldapi.application.web.resource.utils.ResourceUtils;
import org.example.realworldapi.domain.feature.*;
import org.example.realworldapi.domain.model.article.ArticleFilter;
import org.example.realworldapi.domain.model.comment.DeleteCommentInput;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/articles")
@AllArgsConstructor
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
  private final ObjectMapper noWrapRootValueObjectMapper;
  private final ResourceUtils resourceUtils;

  @GetMapping("/feed")
  public ResponseEntity<String> feed(
      @RequestParam(defaultValue = "0") int offset,
      @RequestParam(defaultValue = "0") int limit,
      Principal principal) throws JsonProcessingException {
    final var loggedUserId = resourceUtils.getLoggedUserId(principal);
    final var articlesFilter =
        new ArticleFilter(offset, resourceUtils.getLimit(limit), loggedUserId, null, null, null);
    final var articlesPageResult = findMostRecentArticlesByFilter.handle(articlesFilter);
    return ResponseEntity.ok(
        noWrapRootValueObjectMapper.writeValueAsString(
            resourceUtils.articlesResponse(articlesPageResult, loggedUserId)));
  }

  @GetMapping
  public ResponseEntity<String> getArticles(
      @RequestParam(defaultValue = "0") int offset,
      @RequestParam(defaultValue = "0") int limit,
      @RequestParam(required = false) List<String> tag,
      @RequestParam(required = false) List<String> author,
      @RequestParam(required = false) List<String> favorited,
      Principal principal) throws JsonProcessingException {
    final var loggedUserId = resourceUtils.getLoggedUserId(principal);
    final var filter =
        new ArticleFilter(
            offset, resourceUtils.getLimit(limit), loggedUserId, tag, author, favorited);
    final var articlesPageResult = findArticlesByFilter.handle(filter);
    return ResponseEntity.ok(
        noWrapRootValueObjectMapper.writeValueAsString(
            resourceUtils.articlesResponse(articlesPageResult, loggedUserId)));
  }

  @PostMapping
  @Transactional
  public ResponseEntity<Object> create(
      @Valid @RequestBody NewArticleRequest newArticleRequest,
      Principal principal) {
    final var loggedUserId = resourceUtils.getLoggedUserId(principal);
    final var article = createArticle.handle(newArticleRequest.toNewArticleInput(loggedUserId));
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(resourceUtils.articleResponse(article, loggedUserId));
  }

  @GetMapping("/{slug}")
  public ResponseEntity<Object> findBySlug(@PathVariable String slug) {
    final var article = findArticleBySlug.handle(slug);
    return ResponseEntity.ok(resourceUtils.articleResponse(article, null));
  }

  @PutMapping("/{slug}")
  @Transactional
  public ResponseEntity<Object> update(
      @PathVariable String slug,
      @Valid @RequestBody UpdateArticleRequest updateArticleRequest,
      Principal principal) {
    final var loggedUserId = resourceUtils.getLoggedUserId(principal);
    final var updatedArticle =
        updateArticleBySlug.handle(updateArticleRequest.toUpdateArticleInput(loggedUserId, slug));
    return ResponseEntity.ok(resourceUtils.articleResponse(updatedArticle, null));
  }

  @DeleteMapping("/{slug}")
  @Transactional
  public ResponseEntity<Void> delete(@PathVariable String slug, Principal principal) {
    final var loggedUserId = resourceUtils.getLoggedUserId(principal);
    deleteArticleBySlug.handle(loggedUserId, slug);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{slug}/comments")
  public ResponseEntity<String> getCommentsBySlug(
      @PathVariable String slug, Principal principal) throws JsonProcessingException {
    final var loggedUserId = resourceUtils.getLoggedUserId(principal);
    final var comments = findCommentsByArticleSlug.handle(slug);
    return ResponseEntity.ok(
        noWrapRootValueObjectMapper.writeValueAsString(
            resourceUtils.commentsResponse(comments, loggedUserId)));
  }

  @PostMapping("/{slug}/comments")
  @Transactional
  public ResponseEntity<Object> createComment(
      @PathVariable String slug,
      @Valid @RequestBody NewCommentRequest newCommentRequest,
      Principal principal) {
    final var loggedUserId = resourceUtils.getLoggedUserId(principal);
    final var comment =
        createComment.handle(newCommentRequest.toNewCommentInput(loggedUserId, slug));
    return ResponseEntity.ok(resourceUtils.commentResponse(comment, loggedUserId));
  }

  @DeleteMapping("/{slug}/comments/{id}")
  @Transactional
  public ResponseEntity<Void> deleteComment(
      @PathVariable String slug,
      @PathVariable UUID id,
      Principal principal) {
    final var loggedUserId = resourceUtils.getLoggedUserId(principal);
    deleteComment.handle(new DeleteCommentInput(id, loggedUserId, slug));
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{slug}/favorite")
  @Transactional
  public ResponseEntity<Object> favoriteArticle(
      @PathVariable String slug, Principal principal) {
    final var loggedUserId = resourceUtils.getLoggedUserId(principal);
    favoriteArticle.handle(slug, loggedUserId);
    final var article = findArticleBySlug.handle(slug);
    return ResponseEntity.ok(resourceUtils.articleResponse(article, loggedUserId));
  }

  @DeleteMapping("/{slug}/favorite")
  @Transactional
  public ResponseEntity<Object> unfavoriteArticle(
      @PathVariable String slug, Principal principal) {
    final var loggedUserId = resourceUtils.getLoggedUserId(principal);
    unfavoriteArticle.handle(slug, loggedUserId);
    final var article = findArticleBySlug.handle(slug);
    return ResponseEntity.ok(resourceUtils.articleResponse(article, loggedUserId));
  }
}
