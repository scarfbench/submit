package org.example.realworldapi.infrastructure.repository.hibernate.panache;

import java.util.List;
import java.util.stream.Collectors;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.TagRelationship;
import org.example.realworldapi.domain.model.article.TagRelationshipRepository;
import org.example.realworldapi.domain.model.tag.Tag;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.EntityUtils;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.TagRelationshipEntity;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.TagRelationshipEntityKey;
import org.springframework.stereotype.Repository;

@Repository
public class TagRelationshipRepositoryPanache extends AbstractPanacheRepository
    implements TagRelationshipRepository {

  private final EntityUtils entityUtils;

  public TagRelationshipRepositoryPanache(EntityUtils entityUtils) {
    this.entityUtils = entityUtils;
  }

  @Override
  public void save(TagRelationship tagRelationship) {
    final var articleEntity = findArticleEntityById(tagRelationship.getArticle().getId());
    final var tagEntity = findTagEntityById(tagRelationship.getTag().getId());
    entityManager.persist(new TagRelationshipEntity(articleEntity, tagEntity));
  }

  @Override
  public List<Tag> findArticleTags(Article article) {
    return entityManager.createQuery(
            "from TagRelationshipEntity as tagRelationship where tagRelationship.primaryKey.article.id = :articleId",
            TagRelationshipEntity.class)
        .setParameter("articleId", article.getId())
        .getResultList()
        .stream()
        .map(entityUtils::tag)
        .collect(Collectors.toList());
  }
}
