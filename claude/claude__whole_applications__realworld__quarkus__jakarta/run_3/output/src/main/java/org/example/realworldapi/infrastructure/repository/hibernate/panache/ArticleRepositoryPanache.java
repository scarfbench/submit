package org.example.realworldapi.infrastructure.repository.hibernate.panache;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.ArticleFilter;
import org.example.realworldapi.domain.model.article.ArticleRepository;
import org.example.realworldapi.domain.model.article.PageResult;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.ArticleEntity;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.EntityUtils;
import org.example.realworldapi.infrastructure.repository.hibernate.panache.utils.SimpleQueryBuilder;

@ApplicationScoped
public class ArticleRepositoryPanache extends AbstractPanacheRepository<ArticleEntity, UUID>
    implements ArticleRepository {

  @Inject EntityUtils entityUtils;

  @Override
  public boolean existsBySlug(String slug) {
    Long count = em.createQuery("SELECT COUNT(e) FROM ArticleEntity e WHERE upper(e.slug) = ?1", Long.class)
        .setParameter(1, slug.toUpperCase().trim())
        .getSingleResult();
    return count > 0;
  }

  @Override
  public void save(Article article) {
    final var author = findUserEntityById(article.getAuthor().getId());
    ArticleEntity articleEntity = new ArticleEntity(article, author);
    em.persist(articleEntity);
    em.flush();
  }

  @Override
  public Optional<Article> findArticleById(UUID id) {
    return Optional.ofNullable(em.find(ArticleEntity.class, id)).map(entityUtils::article);
  }

  @Override
  public Optional<Article> findBySlug(String slug) {
    return em.createQuery("FROM ArticleEntity WHERE upper(slug) = ?1", ArticleEntity.class)
        .setParameter(1, slug.toUpperCase().trim())
        .getResultStream()
        .findFirst()
        .map(entityUtils::article);
  }

  @Override
  public void update(Article article) {
    final var articleEntity = findArticleEntityById(article.getId());
    articleEntity.update(article);
  }

  @Override
  public Optional<Article> findByAuthorAndSlug(UUID authorId, String slug) {
    return em.createQuery(
            "FROM ArticleEntity WHERE author.id = :authorId and upper(slug) = :slug",
            ArticleEntity.class)
        .setParameter("authorId", authorId)
        .setParameter("slug", slug.toUpperCase().trim())
        .getResultStream()
        .findFirst()
        .map(entityUtils::article);
  }

  @Override
  public void delete(Article article) {
    ArticleEntity articleEntity = em.find(ArticleEntity.class, article.getId());
    if (articleEntity != null) {
      em.remove(articleEntity);
    }
  }

  @Override
  public PageResult<Article> findMostRecentArticlesByFilter(ArticleFilter articleFilter) {
    final var articlesEntity = em.createQuery(
            "select articles from ArticleEntity as articles inner join articles.author as author inner join author.followedBy as followedBy where followedBy.user.id = :loggedUserId ORDER BY articles.createdAt DESC, articles.updatedAt DESC",
            ArticleEntity.class)
        .setParameter("loggedUserId", articleFilter.getLoggedUserId())
        .setFirstResult(articleFilter.getOffset())
        .setMaxResults(articleFilter.getLimit())
        .getResultList();
    final var articlesResult =
        articlesEntity.stream().map(entityUtils::article).collect(Collectors.toList());
    final var total = count(articleFilter.getLoggedUserId());
    return new PageResult<>(articlesResult, total);
  }

  @Override
  public PageResult<Article> findArticlesByFilter(ArticleFilter filter) {
    Map<String, Object> params = new LinkedHashMap<>();
    SimpleQueryBuilder findArticlesQueryBuilder = new SimpleQueryBuilder();
    findArticlesQueryBuilder.addQueryStatement("select articles from ArticleEntity as articles");
    configFilterFindArticlesQueryBuilder(
        findArticlesQueryBuilder,
        filter.getTags(),
        filter.getAuthors(),
        filter.getFavorited(),
        params);

    String queryString = findArticlesQueryBuilder.toQueryString() + " ORDER BY articles.createdAt DESC, articles.updatedAt DESC";
    var query = em.createQuery(queryString, ArticleEntity.class)
        .setFirstResult(filter.getOffset())
        .setMaxResults(filter.getLimit());

    for (Map.Entry<String, Object> entry : params.entrySet()) {
      query.setParameter(entry.getKey(), entry.getValue());
    }

    final var articlesEntity = query.getResultList();
    final var articlesResult =
        articlesEntity.stream().map(entityUtils::article).collect(Collectors.toList());
    final var total = count(filter.getTags(), filter.getAuthors(), filter.getFavorited());
    return new PageResult<>(articlesResult, total);
  }

  @Override
  public long count(List<String> tags, List<String> authors, List<String> favorited) {
    Map<String, Object> params = new LinkedHashMap<>();
    SimpleQueryBuilder countArticlesQueryBuilder = new SimpleQueryBuilder();
    countArticlesQueryBuilder.addQueryStatement("from ArticleEntity as articles");
    configFilterFindArticlesQueryBuilder(
        countArticlesQueryBuilder, tags, authors, favorited, params);

    String countQuery = "SELECT COUNT(articles) " + countArticlesQueryBuilder.toQueryString();
    var query = em.createQuery(countQuery, Long.class);

    for (Map.Entry<String, Object> entry : params.entrySet()) {
      query.setParameter(entry.getKey(), entry.getValue());
    }

    return query.getSingleResult();
  }

  public long count(UUID loggedUserId) {
    return em.createQuery(
            "SELECT COUNT(articles) from ArticleEntity as articles inner join articles.author as author inner join author.followedBy as followedBy where followedBy.user.id = :loggedUserId",
            Long.class)
        .setParameter("loggedUserId", loggedUserId)
        .getSingleResult();
  }

  private void configFilterFindArticlesQueryBuilder(
      SimpleQueryBuilder findArticlesQueryBuilder,
      List<String> tags,
      List<String> authors,
      List<String> favorited,
      Map<String, Object> params) {

    findArticlesQueryBuilder.updateQueryStatementConditional(
        isNotEmpty(tags),
        "inner join articles.tags as tags inner join tags.primaryKey.tag as tag",
        "upper(tag.name) in (:tags)",
        () -> params.put("tags", toUpperCase(tags)));

    findArticlesQueryBuilder.updateQueryStatementConditional(
        isNotEmpty(authors),
        "inner join articles.author as authors",
        "upper(authors.username) in (:authors)",
        () -> params.put("authors", toUpperCase(authors)));

    findArticlesQueryBuilder.updateQueryStatementConditional(
        isNotEmpty(favorited),
        "inner join articles.favorites as favorites inner join favorites.primaryKey.user as user",
        "upper(user.username) in (:favorites)",
        () -> params.put("favorites", toUpperCase(favorited)));
  }
}
