package org.example.realworldapi.infrastructure.repository.hibernate.panache;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import java.util.UUID;
import org.example.realworldapi.domain.model.user.FollowRelationship;
import org.example.realworldapi.domain.model.user.FollowRelationshipRepository;
import org.example.realworldapi.domain.model.user.User;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.*;
import org.springframework.stereotype.Repository;

@Repository
public class FollowRelationshipRepositoryPanache implements FollowRelationshipRepository {

  @PersistenceContext
  private EntityManager entityManager;

  private final EntityUtils entityUtils;

  public FollowRelationshipRepositoryPanache(EntityUtils entityUtils) {
    this.entityUtils = entityUtils;
  }

  @Override
  public boolean isFollowing(UUID currentUserId, UUID followedUserId) {
    Long count = entityManager.createQuery(
            "select count(f) from FollowRelationshipEntity f where f.primaryKey.user.id = :currentUserId and f.primaryKey.followed.id = :followedUserId", Long.class)
        .setParameter("currentUserId", currentUserId)
        .setParameter("followedUserId", followedUserId)
        .getSingleResult();
    return count > 0;
  }

  @Override
  public void save(FollowRelationship followRelationship) {
    final var userEntity = entityManager.find(UserEntity.class, followRelationship.getUser().getId());
    final var userToFollowEntity = entityManager.find(UserEntity.class, followRelationship.getFollowed().getId());
    entityManager.persist(new FollowRelationshipEntity(userEntity, userToFollowEntity));
    entityManager.flush();
  }

  @Override
  public Optional<FollowRelationship> findByUsers(User loggedUser, User followedUser) {
    return findFollowEntity(loggedUser, followedUser)
        .map(this::toFollowRelationship);
  }

  @Override
  public void remove(FollowRelationship followRelationship) {
    final var entity = findFollowEntity(
        followRelationship.getUser(), followRelationship.getFollowed())
        .orElseThrow();
    entityManager.remove(entity);
  }

  private Optional<FollowRelationshipEntity> findFollowEntity(User loggedUser, User followedUser) {
    final var loggedUserEntity = entityManager.find(UserEntity.class, loggedUser.getId());
    final var followedEntity = entityManager.find(UserEntity.class, followedUser.getId());
    final var primaryKey = new FollowRelationshipEntityKey();
    primaryKey.setUser(loggedUserEntity);
    primaryKey.setFollowed(followedEntity);
    return Optional.ofNullable(entityManager.find(FollowRelationshipEntity.class, primaryKey));
  }

  private FollowRelationship toFollowRelationship(FollowRelationshipEntity entity) {
    final var user = entityUtils.user(entity.getUser());
    final var followed = entityUtils.user(entity.getFollowed());
    return new FollowRelationship(user, followed);
  }
}
