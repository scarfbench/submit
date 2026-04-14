package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.FindUserById;
import org.example.realworldapi.domain.feature.FindUserByUsername;
import org.example.realworldapi.domain.feature.UnfollowUserByUsername;
import org.example.realworldapi.domain.model.user.FollowRelationshipRepository;

import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class UnfollowUserByUsernameImpl implements UnfollowUserByUsername {

    private final FindUserById findUserById;
    private final FindUserByUsername findUserByUsername;
    private final FollowRelationshipRepository followRelationshipRepository;
    public UnfollowUserByUsernameImpl(FindUserById findUserById, FindUserByUsername findUserByUsername, FollowRelationshipRepository followRelationshipRepository) {
        this.findUserById = findUserById;
        this.findUserByUsername = findUserByUsername;
        this.followRelationshipRepository = followRelationshipRepository;
    }

    @Override
    public void handle(UUID loggedUserId, String username) {
        final var loggedUser = findUserById.handle(loggedUserId);
        final var userToUnfollow = findUserByUsername.handle(username);
        final var followingRelationship =
                followRelationshipRepository.findByUsers(loggedUser, userToUnfollow).orElseThrow();
        followRelationshipRepository.remove(followingRelationship);
    }
}
