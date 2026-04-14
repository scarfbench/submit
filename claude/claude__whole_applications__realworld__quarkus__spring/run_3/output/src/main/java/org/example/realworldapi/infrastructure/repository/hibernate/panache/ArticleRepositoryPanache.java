package org.example.realworldapi.infrastructure.repository.hibernate.panache;

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
import org.example.realworldapi.infrastructure.repository.hibernate.panache.utils.SimpleQueryBuilder;
import org.springframework.stereotype.Repository;

@Repository
public class ArticleRepositoryPanache extends AbstractPanacheRepository<ArticleEntity, UUID>
        implements ArticleRepository {

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
        final var author = findUserEntityById(article.getAuthor().getId());
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
        final var articleEntity = findArticleEntityById(article.getId());
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
        String jpql = "select articles from ArticleEntity as articles inner join articles.author as author inner join author.followedBy as followedBy where followedBy.user.id = :loggedUserId order by articles.createdAt desc, articles.updatedAt desc";
        var query = entityManager.createQuery(jpql, ArticleEntity.class)
                .setParameter("loggedUserId", articleFilter.getLoggedUserId())
                .setFirstResult(articleFilter.getOffset())
                .setMaxResults(articleFilter.getLimit());
        var articlesEntity = query.getResultList();
        var articlesResult = articlesEntity.stream().map(entityUtils::article).collect(Collectors.toList());
        var total = countByFollowing(articleFilter.getLoggedUserId());
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
        String jpql = findArticlesQueryBuilder.toQueryString() + " order by articles.createdAt desc, articles.updatedAt desc";
        TypedQuery<ArticleEntity> query = entityManager.createQuery(jpql, ArticleEntity.class);
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
        var query = entityManager.createQuery(countArticlesQueryBuilder.toQueryString(), Long.class);
        params.forEach(query::setParameter);
        return query.getSingleResult();
    }

    public long countByFollowing(UUID loggedUserId) {
        return entityManager.createQuery(
                        "select count(articles) from ArticleEntity as articles inner join articles.author as author inner join author.followedBy as followedBy where followedBy.user.id = :loggedUserId", Long.class)
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
