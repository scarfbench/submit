package org.example.realworldapi.infrastructure.repository.hibernate.panache;

import jakarta.enterprise.context.ApplicationScoped;
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
public class FollowRelationshipRepositoryPanache extends AbstractPanacheRepository
    implements FollowRelationshipRepository {

  @jakarta.inject.Inject private EntityUtils entityUtils;

  @Override
  public boolean isFollowing(UUID currentUserId, UUID followedUserId) {
    Long count =
        getEm().createQuery(
                "select count(f) from FollowRelationshipEntity f where f.primaryKey.user.id = :currentUserId and f.primaryKey.followed.id = :followedUserId",
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
    getEm().persist(new FollowRelationshipEntity(userEntity, userToFollowEntity));
    getEm().flush();
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
    getEm().remove(usersFollowedEntity);
  }

  private Optional<FollowRelationshipEntity> findUsersFollowedEntityByUsers(
      User loggedUser, User followedUser) {
    final var loggedUserEntity = findUserEntityById(loggedUser.getId());
    final var followedEntity = findUserEntityById(followedUser.getId());
    var key = usersFollowedKey(loggedUserEntity, followedEntity);
    var entity = getEm().find(FollowRelationshipEntity.class, key);
    return Optional.ofNullable(entity);
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
