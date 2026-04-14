package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.FindUserById;
import org.example.realworldapi.domain.feature.FindUserByUsername;
import org.example.realworldapi.domain.feature.UnfollowUserByUsername;
import org.example.realworldapi.domain.model.user.FollowRelationshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UnfollowUserByUsernameImpl implements UnfollowUserByUsername {

    @Autowired
    private FindUserById findUserById;
    @Autowired
    private FindUserByUsername findUserByUsername;
    @Autowired
    private FollowRelationshipRepository followRelationshipRepository;

    @Override
    public void handle(UUID loggedUserId, String username) {
        final var loggedUser = findUserById.handle(loggedUserId);
        final var userToUnfollow = findUserByUsername.handle(username);
        final var followingRelationship =
                followRelationshipRepository.findByUsers(loggedUser, userToUnfollow).orElseThrow();
        followRelationshipRepository.remove(followingRelationship);
    }
}
