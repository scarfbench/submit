package org.example.realworldapi.infrastructure.repository.hibernate.panache;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import org.example.realworldapi.domain.model.user.FollowRelationship;
import org.example.realworldapi.domain.model.user.FollowRelationshipRepository;
import org.example.realworldapi.domain.model.user.User;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.EntityUtils;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.FollowRelationshipEntity;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.FollowRelationshipEntityKey;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.UserEntity;

@ApplicationScoped
public class FollowRelationshipRepositoryPanache
    extends AbstractPanacheRepository<FollowRelationshipEntity, FollowRelationshipEntityKey>
    implements FollowRelationshipRepository {

  @Inject EntityUtils entityUtils;

  @Override
  public boolean isFollowing(UUID currentUserId, UUID followedUserId) {
    Long count = em.createQuery(
            "SELECT COUNT(e) FROM FollowRelationshipEntity e WHERE e.primaryKey.user.id = :currentUserId and e.primaryKey.followed.id = :followedUserId",
            Long.class)
        .setParameter("currentUserId", currentUserId)
        .setParameter("followedUserId", followedUserId)
        .getSingleResult();
    return count > 0;
  }

  @Override
  public void save(FollowRelationship followRelationship) {
    final var userEntity = findUserEntityById(followRelationship.getUser().getId());
    final var userToFollowEntity = findUserEntityById(followRelationship.getFollowed().getId());
    FollowRelationshipEntity entity = new FollowRelationshipEntity(userEntity, userToFollowEntity);
    em.persist(entity);
    em.flush();
  }

  @Override
  public Optional<FollowRelationship> findByUsers(User loggedUser, User followedUser) {
    return findUsersFollowedEntityByUsers(loggedUser, followedUser)
        .map(this::followingRelationship);
  }

  @Override
  public void remove(FollowRelationship followRelationship) {
    final var usersFollowedEntity =
        findUsersFollowedEntityByUsers(
                followRelationship.getUser(), followRelationship.getFollowed())
            .orElseThrow();
    em.remove(em.merge(usersFollowedEntity));
  }

  private Optional<FollowRelationshipEntity> findUsersFollowedEntityByUsers(
      User loggedUser, User followedUser) {
    final var loggedUserEntity = findUserEntityById(loggedUser.getId());
    final var followedEntity = findUserEntityById(followedUser.getId());
    final var key = usersFollowedKey(loggedUserEntity, followedEntity);
    return Optional.ofNullable(em.find(FollowRelationshipEntity.class, key));
  }

  private FollowRelationship followingRelationship(
      FollowRelationshipEntity followRelationshipEntity) {
    final var user = entityUtils.user(followRelationshipEntity.getUser());
    final var followed = entityUtils.user(followRelationshipEntity.getFollowed());
    return new FollowRelationship(user, followed);
  }

  private FollowRelationshipEntityKey usersFollowedKey(UserEntity user, UserEntity followed) {
    final var primaryKey = new FollowRelationshipEntityKey();
    primaryKey.setUser(user);
    primaryKey.setFollowed(followed);
    return primaryKey;
  }
}
