package org.example.realworldapi.infrastructure.repository.hibernate.panache;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.stream.Collectors;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.TagRelationship;
import org.example.realworldapi.domain.model.article.TagRelationshipRepository;
import org.example.realworldapi.domain.model.tag.Tag;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.EntityUtils;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.TagRelationshipEntity;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.TagRelationshipEntityKey;

@ApplicationScoped
public class TagRelationshipRepositoryPanache extends AbstractPanacheRepository
    implements TagRelationshipRepository {

  @jakarta.inject.Inject private EntityUtils entityUtils;

  @Override
  public void save(TagRelationship tagRelationship) {
    final var articleEntity = findArticleEntityById(tagRelationship.getArticle().getId());
    final var tagEntity = findTagEntityById(tagRelationship.getTag().getId());
    getEm().persist(new TagRelationshipEntity(articleEntity, tagEntity));
  }

  @Override
  public List<Tag> findArticleTags(Article article) {
    return getEm().createQuery(
            "from TagRelationshipEntity as tagRelationship where tagRelationship.primaryKey.article.id = :articleId",
            TagRelationshipEntity.class)
        .setParameter("articleId", article.getId())
        .getResultList()
        .stream()
        .map(entityUtils::tag)
        .collect(Collectors.toList());
  }
}
