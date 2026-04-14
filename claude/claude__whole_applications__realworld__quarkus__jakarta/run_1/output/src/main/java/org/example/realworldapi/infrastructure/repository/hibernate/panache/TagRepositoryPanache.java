package org.example.realworldapi.infrastructure.repository.hibernate.panache;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.example.realworldapi.domain.model.tag.Tag;
import org.example.realworldapi.domain.model.tag.TagRepository;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.EntityUtils;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.TagEntity;

@ApplicationScoped
public class TagRepositoryPanache extends AbstractPanacheRepository implements TagRepository {

  @jakarta.inject.Inject private EntityUtils entityUtils;

  @Override
  public List<Tag> findAllTags() {
    return getEm().createQuery("from TagEntity", TagEntity.class).getResultList().stream()
        .map(entityUtils::tag)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<Tag> findByName(String name) {
    var results =
        getEm().createQuery("from TagEntity where upper(name) = :name", TagEntity.class)
            .setParameter("name", name.toUpperCase().trim())
            .getResultList();
    return results.stream().findFirst().map(entityUtils::tag);
  }

  @Override
  public void save(Tag tag) {
    getEm().persist(new TagEntity(tag));
  }

  @Override
  public List<Tag> findByNames(List<String> names) {
    var tagsEntity =
        getEm().createQuery(
                "select tags from TagEntity as tags where upper(tags.name) in (:names)",
                TagEntity.class)
            .setParameter("names", toUpperCase(names))
            .getResultList();
    return tagsEntity.stream().map(entityUtils::tag).collect(Collectors.toList());
  }
}
