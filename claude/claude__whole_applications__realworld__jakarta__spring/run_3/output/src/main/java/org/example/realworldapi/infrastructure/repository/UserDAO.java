package org.example.realworldapi.infrastructure.repository;

import jakarta.persistence.TypedQuery;
import org.example.realworldapi.domain.model.user.User;
import org.example.realworldapi.domain.model.user.UserRepository;
import org.example.realworldapi.infrastructure.repository.entity.EntityUtils;
import org.example.realworldapi.infrastructure.repository.entity.UserEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserDAO extends AbstractDAO<UserEntity, UUID> implements UserRepository {

    private final EntityUtils entityUtils;

    public UserDAO(EntityUtils entityUtils) {
        this.entityUtils = entityUtils;
    }

    @Override
    public void save(User user) {
        em.persist(new UserEntity(user));
    }

    @Override
    public boolean existsByUsername(String username) {
        String jpql = "SELECT u FROM UserEntity u WHERE UPPER(u.username) = :username";
        TypedQuery<UserEntity> query = em.createQuery(jpql, UserEntity.class);
        query.setParameter("username", username.toUpperCase().trim());
        return !query.getResultList().isEmpty();
    }

    @Override
    public boolean existsByEmail(String email) {
        String jpql = "SELECT u FROM UserEntity u WHERE UPPER(u.email) = :email";
        TypedQuery<UserEntity> query = em.createQuery(jpql, UserEntity.class);
        query.setParameter("email", email.toUpperCase().trim());
        return !query.getResultList().isEmpty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String jpql = "SELECT u FROM UserEntity u WHERE UPPER(u.email) = :email";
        TypedQuery<UserEntity> query = em.createQuery(jpql, UserEntity.class);
        query.setParameter("email", email.toUpperCase().trim());
        List<UserEntity> resultList = query.getResultList();
        if (!resultList.isEmpty()) {
            return Optional.of(entityUtils.user(resultList.get(0)));
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findUserById(UUID id) {
        UserEntity u = findUserEntityById(id);
        if (u != null) {
            return Optional.of(entityUtils.user(u));
        }
        return Optional.empty();
    }

    @Override
    public boolean existsUsername(UUID excludeId, String username) {
        String jpql = "SELECT u FROM UserEntity u WHERE UPPER(u.username) = :username and u.id != :excludeId";
        TypedQuery<UserEntity> query = em.createQuery(jpql, UserEntity.class);
        query.setParameter("username", username.toUpperCase().trim());
        query.setParameter("excludeId", excludeId);
        return !query.getResultList().isEmpty();
    }

    @Override
    public boolean existsEmail(UUID excludeId, String email) {
        String jpql = "SELECT u FROM UserEntity u WHERE UPPER(u.email) = :email and u.id != :excludeId";
        TypedQuery<UserEntity> query = em.createQuery(jpql, UserEntity.class);
        query.setParameter("email", email.toUpperCase().trim());
        query.setParameter("excludeId", excludeId);
        return !query.getResultList().isEmpty();
    }

    @Override
    public void update(User user) {
        UserEntity userEntity = new UserEntity(user);
        em.merge(userEntity);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String jpql = "SELECT u FROM UserEntity u WHERE UPPER(u.username) = :username";
        TypedQuery<UserEntity> query = em.createQuery(jpql, UserEntity.class);
        query.setParameter("username", username.toUpperCase().trim());
        List<UserEntity> resultList = query.getResultList();
        if (!resultList.isEmpty()) {
            return Optional.of(entityUtils.user(resultList.get(0)));
        }
        return Optional.empty();
    }
}
