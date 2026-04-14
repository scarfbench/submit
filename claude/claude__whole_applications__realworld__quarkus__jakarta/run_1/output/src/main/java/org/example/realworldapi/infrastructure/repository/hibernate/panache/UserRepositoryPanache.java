package org.example.realworldapi.infrastructure.repository.hibernate.panache;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;
import org.example.realworldapi.domain.model.user.User;
import org.example.realworldapi.domain.model.user.UserRepository;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.EntityUtils;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.UserEntity;

@ApplicationScoped
public class UserRepositoryPanache extends AbstractPanacheRepository implements UserRepository {

  @jakarta.inject.Inject private EntityUtils entityUtils;

  @Override
  public void save(User user) {
    getEm().persist(new UserEntity(user));
  }

  @Override
  public boolean existsBy(String field, String value) {
    Long count =
        getEm().createQuery(
                "select count(u) from UserEntity u where upper(u." + field + ") = :value",
                Long.class)
            .setParameter("value", value.toUpperCase().trim())
            .getSingleResult();
    return count > 0;
  }

  @Override
  public Optional<User> findByEmail(String email) {
    var results =
        getEm().createQuery("from UserEntity where upper(email) = :email", UserEntity.class)
            .setParameter("email", email.toUpperCase().trim())
            .getResultList();
    return results.stream().findFirst().map(entityUtils::user);
  }

  @Override
  public Optional<User> findUserById(UUID id) {
    UserEntity entity = getEm().find(UserEntity.class, id);
    return Optional.ofNullable(entity).map(entityUtils::user);
  }

  @Override
  public boolean existsUsername(UUID excludeId, String username) {
    Long count =
        getEm().createQuery(
                "select count(u) from UserEntity u where u.id != :excludeId and upper(u.username) = :username",
                Long.class)
            .setParameter("excludeId", excludeId)
            .setParameter("username", username.toUpperCase().trim())
            .getSingleResult();
    return count > 0;
  }

  @Override
  public boolean existsEmail(UUID excludeId, String email) {
    Long count =
        getEm().createQuery(
                "select count(u) from UserEntity u where u.id != :excludeId and upper(u.email) = :email",
                Long.class)
            .setParameter("excludeId", excludeId)
            .setParameter("email", email.toUpperCase().trim())
            .getSingleResult();
    return count > 0;
  }

  @Override
  public void update(User user) {
    final var userEntity = getEm().find(UserEntity.class, user.getId());
    userEntity.update(user);
    getEm().merge(userEntity);
  }

  @Override
  public Optional<User> findByUsername(String username) {
    var results =
        getEm().createQuery("from UserEntity where upper(username) = :username", UserEntity.class)
            .setParameter("username", username.toUpperCase().trim())
            .getResultList();
    return results.stream().findFirst().map(entityUtils::user);
  }
}
