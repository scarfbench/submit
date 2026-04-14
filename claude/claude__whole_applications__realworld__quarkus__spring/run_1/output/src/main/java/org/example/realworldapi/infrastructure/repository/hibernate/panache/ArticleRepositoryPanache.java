package org.example.realworldapi.infrastructure.repository.hibernate.panache;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.ArticleFilter;
import org.example.realworldapi.domain.model.article.ArticleRepository;
import org.example.realworldapi.domain.model.article.PageResult;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.ArticleEntity;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.EntityUtils;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.UserEntity;
import org.example.realworldapi.infrastructure.repository.hibernate.panache.utils.SimpleQueryBuilder;
import org.springframework.stereotype.Repository;

@Repository
public class ArticleRepositoryPanache implements ArticleRepository {

  @PersistenceContext
  private EntityManager entityManager;

  private final EntityUtils entityUtils;

  public ArticleRepositoryPanache(EntityUtils entityUtils) {
    this.entityUtils = entityUtils;
  }

  @Override
  public boolean existsBySlug(String slug) {
    Long count = entityManager.createQuery(
            "select count(a) from ArticleEntity a where upper(a.slug) = :slug", Long.class)
        .setParameter("slug", slug.toUpperCase().trim())
        .getSingleResult();
    return count > 0;
  }

  @Override
  public void save(Article article) {
    final var author = entityManager.find(UserEntity.class, article.getAuthor().getId());
    entityManager.persist(new ArticleEntity(article, author));
    entityManager.flush();
  }

  @Override
  public Optional<Article> findArticleById(UUID id) {
    ArticleEntity entity = entityManager.find(ArticleEntity.class, id);
    return Optional.ofNullable(entity).map(entityUtils::article);
  }

  @Override
  public Optional<Article> findBySlug(String slug) {
    var results = entityManager.createQuery(
            "from ArticleEntity where upper(slug) = :slug", ArticleEntity.class)
        .setParameter("slug", slug.toUpperCase().trim())
        .getResultList();
    return results.stream().findFirst().map(entityUtils::article);
  }

  @Override
  public void update(Article article) {
    final var articleEntity = entityManager.find(ArticleEntity.class, article.getId());
    articleEntity.update(article);
    entityManager.merge(articleEntity);
  }

  @Override
  public Optional<Article> findByAuthorAndSlug(UUID authorId, String slug) {
    var results = entityManager.createQuery(
            "from ArticleEntity where author.id = :authorId and upper(slug) = :slug", ArticleEntity.class)
        .setParameter("authorId", authorId)
        .setParameter("slug", slug.toUpperCase().trim())
        .getResultList();
    return results.stream().findFirst().map(entityUtils::article);
  }

  @Override
  public void delete(Article article) {
    ArticleEntity entity = entityManager.find(ArticleEntity.class, article.getId());
    if (entity != null) {
      entityManager.remove(entity);
    }
  }

  @Override
  public PageResult<Article> findMostRecentArticlesByFilter(ArticleFilter articleFilter) {
    var query = entityManager.createQuery(
            "select articles from ArticleEntity as articles inner join articles.author as author inner join author.followedBy as followedBy where followedBy.user.id = :loggedUserId order by articles.createdAt desc, articles.updatedAt desc",
            ArticleEntity.class)
        .setParameter("loggedUserId", articleFilter.getLoggedUserId())
        .setFirstResult(articleFilter.getOffset())
        .setMaxResults(articleFilter.getLimit());

    var articlesEntity = query.getResultList();
    var articlesResult = articlesEntity.stream().map(entityUtils::article).collect(Collectors.toList());

    Long total = entityManager.createQuery(
            "select count(articles) from ArticleEntity as articles inner join articles.author as author inner join author.followedBy as followedBy where followedBy.user.id = :loggedUserId",
            Long.class)
        .setParameter("loggedUserId", articleFilter.getLoggedUserId())
        .getSingleResult();

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

    String queryStr = findArticlesQueryBuilder.toQueryString() + " order by articles.createdAt desc, articles.updatedAt desc";
    TypedQuery<ArticleEntity> query = entityManager.createQuery(queryStr, ArticleEntity.class);
    params.forEach(query::setParameter);
    query.setFirstResult(filter.getOffset());
    query.setMaxResults(filter.getLimit());

    var articlesEntity = query.getResultList();
    var articlesResult = articlesEntity.stream().map(entityUtils::article).collect(Collectors.toList());
    var total = count(filter.getTags(), filter.getAuthors(), filter.getFavorited());
    return new PageResult<>(articlesResult, total);
  }

  @Override
  public long count(List<String> tags, List<String> authors, List<String> favorited) {
    Map<String, Object> params = new LinkedHashMap<>();
    SimpleQueryBuilder countArticlesQueryBuilder = new SimpleQueryBuilder();
    countArticlesQueryBuilder.addQueryStatement("select count(distinct articles) from ArticleEntity as articles");
    configFilterFindArticlesQueryBuilder(
        countArticlesQueryBuilder, tags, authors, favorited, params);
    TypedQuery<Long> query = entityManager.createQuery(countArticlesQueryBuilder.toQueryString(), Long.class);
    params.forEach(query::setParameter);
    return query.getSingleResult();
  }

  private boolean isNotEmpty(List<?> list) {
    return list != null && !list.isEmpty();
  }

  private List<String> toUpperCase(List<String> subjectList) {
    return subjectList.stream().map(String::toUpperCase).collect(Collectors.toList());
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
