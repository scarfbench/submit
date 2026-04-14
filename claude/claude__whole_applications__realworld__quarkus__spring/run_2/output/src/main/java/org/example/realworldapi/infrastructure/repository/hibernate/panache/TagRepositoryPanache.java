package org.example.realworldapi.infrastructure.repository.hibernate.panache;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.example.realworldapi.domain.model.tag.Tag;
import org.example.realworldapi.domain.model.tag.TagRepository;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.EntityUtils;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.TagEntity;
import org.springframework.stereotype.Repository;

@Repository
public class TagRepositoryPanache extends AbstractPanacheRepository implements TagRepository {

  private final EntityUtils entityUtils;

  public TagRepositoryPanache(EntityUtils entityUtils) {
    this.entityUtils = entityUtils;
  }

  @Override
  public List<Tag> findAllTags() {
    return entityManager.createQuery("from TagEntity", TagEntity.class)
        .getResultList()
        .stream()
        .map(entityUtils::tag)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<Tag> findByName(String name) {
    return entityManager.createQuery(
            "from TagEntity where upper(name) = :name", TagEntity.class)
        .setParameter("name", name.toUpperCase().trim())
        .getResultStream()
        .findFirst()
        .map(entityUtils::tag);
  }

  @Override
  public void save(Tag tag) {
    entityManager.persist(new TagEntity(tag));
  }

  @Override
  public List<Tag> findByNames(List<String> names) {
    return entityManager.createQuery(
            "select tags from TagEntity as tags where upper(tags.name) in (:names)", TagEntity.class)
        .setParameter("names", toUpperCase(names))
        .getResultList()
        .stream()
        .map(entityUtils::tag)
        .collect(Collectors.toList());
  }
}
