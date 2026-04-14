package org.example.realworldapi.infrastructure.repository.hibernate.panache;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import org.example.realworldapi.domain.model.user.User;
import org.example.realworldapi.domain.model.user.UserRepository;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.EntityUtils;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.UserEntity;

@ApplicationScoped
public class UserRepositoryPanache extends AbstractPanacheRepository<UserEntity, UUID>
    implements UserRepository {

  @Inject EntityUtils entityUtils;

  @Override
  public void save(User user) {
    em.persist(new UserEntity(user));
  }

  @Override
  public boolean existsBy(String field, String value) {
    String jpql = "SELECT COUNT(e) FROM UserEntity e WHERE upper(e." + field + ") = ?1";
    Long count = em.createQuery(jpql, Long.class)
        .setParameter(1, value.toUpperCase().trim())
        .getSingleResult();
    return count > 0;
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return em.createQuery("FROM UserEntity WHERE upper(email) = ?1", UserEntity.class)
        .setParameter(1, email.toUpperCase().trim())
        .getResultStream()
        .findFirst()
        .map(entityUtils::user);
  }

  @Override
  public Optional<User> findUserById(UUID id) {
    return Optional.ofNullable(em.find(UserEntity.class, id)).map(entityUtils::user);
  }

  @Override
  public boolean existsUsername(UUID excludeId, String username) {
    Long count = em.createQuery(
            "SELECT COUNT(e) FROM UserEntity e WHERE e.id != :excludeId and upper(e.username) = :username",
            Long.class)
        .setParameter("excludeId", excludeId)
        .setParameter("username", username.toUpperCase().trim())
        .getSingleResult();
    return count > 0;
  }

  @Override
  public boolean existsEmail(UUID excludeId, String email) {
    Long count = em.createQuery(
            "SELECT COUNT(e) FROM UserEntity e WHERE e.id != :excludeId and upper(e.email) = :email",
            Long.class)
        .setParameter("excludeId", excludeId)
        .setParameter("email", email.toUpperCase().trim())
        .getSingleResult();
    return count > 0;
  }

  @Override
  public void update(User user) {
    final var userEntity = em.find(UserEntity.class, user.getId());
    userEntity.update(user);
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return em.createQuery("FROM UserEntity WHERE upper(username) = ?1", UserEntity.class)
        .setParameter(1, username.toUpperCase().trim())
        .getResultStream()
        .findFirst()
        .map(entityUtils::user);
  }
}
