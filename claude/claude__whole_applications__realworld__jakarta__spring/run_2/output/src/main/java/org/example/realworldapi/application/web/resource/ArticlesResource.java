package org.example.realworldapi.application.web.resource;

import org.example.realworldapi.application.web.model.request.NewArticleRequestWrapper;
import org.example.realworldapi.application.web.model.request.NewCommentRequestWrapper;
import org.example.realworldapi.application.web.model.request.UpdateArticleRequestWrapper;
import org.example.realworldapi.application.web.resource.utils.ResourceUtils;
import org.example.realworldapi.domain.feature.*;
import org.example.realworldapi.domain.model.article.ArticleFilter;
import org.example.realworldapi.domain.model.comment.DeleteCommentInput;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/articles")
public class ArticlesResource {

    @Autowired
    private FindArticlesByFilter findArticlesByFilter;
    @Autowired
    private CreateArticle createArticle;
    @Autowired
    private FindMostRecentArticlesByFilter findMostRecentArticlesByFilter;
    @Autowired
    private FindArticleBySlug findArticleBySlug;
    @Autowired
    private UpdateArticleBySlug updateArticleBySlug;
    @Autowired
    private DeleteArticleBySlug deleteArticleBySlug;
    @Autowired
    private CreateComment createComment;
    @Autowired
    private DeleteComment deleteComment;
    @Autowired
    private FindCommentsByArticleSlug findCommentsByArticleSlug;
    @Autowired
    private FavoriteArticle favoriteArticle;
    @Autowired
    private UnfavoriteArticle unfavoriteArticle;
    @Autowired
    private ResourceUtils resourceUtils;

    @GetMapping(value = "/feed", produces = "application/json")
    public ResponseEntity<?> feed(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "0") int limit,
            @RequestAttribute(required = false) UUID loggedUserId) {
        final var articlesFilter =
                new ArticleFilter(offset, resourceUtils.getLimit(limit), loggedUserId, null, null, null);
        final var articlesPageResult = findMostRecentArticlesByFilter.handle(articlesFilter);
        return ResponseEntity.ok(resourceUtils.articlesResponse(articlesPageResult, loggedUserId));
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> getArticles(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "0") int limit,
            @RequestParam(required = false) List<String> tag,
            @RequestParam(required = false) List<String> author,
            @RequestParam(required = false) List<String> favorited,
            @RequestAttribute(required = false) UUID loggedUserId) {
        final var filter =
                new ArticleFilter(
                        offset, resourceUtils.getLimit(limit), loggedUserId, tag, author, favorited);
        final var articlesPageResult = findArticlesByFilter.handle(filter);
        return ResponseEntity.ok(resourceUtils.articlesResponse(articlesPageResult, loggedUserId));
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    @Transactional
    public ResponseEntity<?> create(
            @Valid @RequestBody NewArticleRequestWrapper newArticleRequest,
            @RequestAttribute UUID loggedUserId) {
        final var article = createArticle.handle(newArticleRequest.getArticle().toNewArticleInput(loggedUserId));
        return ResponseEntity.status(HttpStatus.CREATED).body(Collections.singletonMap("article", resourceUtils.articleResponse(article, loggedUserId)));
    }

    @GetMapping(value = "/{slug}", produces = "application/json")
    public ResponseEntity<?> findBySlug(@PathVariable String slug) {
        final var article = findArticleBySlug.handle(slug);
        return ResponseEntity.ok(Collections.singletonMap("article", resourceUtils.articleResponse(article, null)));
    }

    @PutMapping(value = "/{slug}", consumes = "application/json", produces = "application/json")
    @Transactional
    public ResponseEntity<?> update(
            @PathVariable String slug,
            @Valid @RequestBody UpdateArticleRequestWrapper updateArticleRequest,
            @RequestAttribute UUID loggedUserId) {
        final var updatedArticle =
                updateArticleBySlug.handle(updateArticleRequest.getArticle().toUpdateArticleInput(loggedUserId, slug));
        return ResponseEntity.ok(Collections.singletonMap("article", resourceUtils.articleResponse(updatedArticle, null)));
    }

    @DeleteMapping(value = "/{slug}", produces = "application/json")
    @Transactional
    public ResponseEntity<?> delete(
            @PathVariable String slug,
            @RequestAttribute UUID loggedUserId) {
        deleteArticleBySlug.handle(loggedUserId, slug);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/{slug}/comments", produces = "application/json")
    public ResponseEntity<?> getCommentsBySlug(
            @PathVariable String slug,
            @RequestAttribute(required = false) UUID loggedUserId) {
        final var comments = findCommentsByArticleSlug.handle(slug);
        return ResponseEntity.ok(resourceUtils.commentsResponse(comments, loggedUserId));
    }

    @PostMapping(value = "/{slug}/comments", consumes = "application/json", produces = "application/json")
    @Transactional
    public ResponseEntity<?> createComment(
            @PathVariable String slug,
            @Valid @RequestBody NewCommentRequestWrapper newCommentRequest,
            @RequestAttribute UUID loggedUserId) {
        final var comment =
                createComment.handle(newCommentRequest.getComment().toNewCommentInput(loggedUserId, slug));
        return ResponseEntity.ok(Collections.singletonMap("comment", resourceUtils.commentResponse(comment, loggedUserId)));
    }

    @DeleteMapping(value = "/{slug}/comments/{id}", produces = "application/json")
    @Transactional
    public ResponseEntity<?> deleteComment(
            @PathVariable String slug,
            @PathVariable UUID id,
            @RequestAttribute UUID loggedUserId) {
        deleteComment.handle(new DeleteCommentInput(id, loggedUserId, slug));
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{slug}/favorite", produces = "application/json")
    @Transactional
    public ResponseEntity<?> favoriteArticle(
            @PathVariable String slug,
            @RequestAttribute UUID loggedUserId) {
        favoriteArticle.handle(slug, loggedUserId);
        final var article = findArticleBySlug.handle(slug);
        return ResponseEntity.ok(Collections.singletonMap("article", resourceUtils.articleResponse(article, loggedUserId)));
    }

    @DeleteMapping(value = "/{slug}/favorite", produces = "application/json")
    @Transactional
    public ResponseEntity<?> unfavoriteArticle(
            @PathVariable String slug,
            @RequestAttribute UUID loggedUserId) {
        unfavoriteArticle.handle(slug, loggedUserId);
        final var article = findArticleBySlug.handle(slug);
        return ResponseEntity.ok(Collections.singletonMap("article", resourceUtils.articleResponse(article, loggedUserId)));
    }
}
