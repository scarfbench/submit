package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.FindUserById;
import org.example.realworldapi.domain.feature.FindUserByUsername;
import org.example.realworldapi.domain.feature.FollowUserByUsername;
import org.example.realworldapi.domain.model.user.FollowRelationship;
import org.example.realworldapi.domain.model.user.FollowRelationshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FollowUserByUsernameImpl implements FollowUserByUsername {

    @Autowired
    private FindUserById findUserById;
    @Autowired
    private FindUserByUsername findUserByUsername;
    @Autowired
    private FollowRelationshipRepository usersFollowedRepository;

    @Override
    public FollowRelationship handle(UUID loggedUserId, String username) {
        final var loggedUser = findUserById.handle(loggedUserId);
        final var userToFollow = findUserByUsername.handle(username);
        final var followingRelationship = new FollowRelationship(loggedUser, userToFollow);
        usersFollowedRepository.save(followingRelationship);
        return followingRelationship;
    }
}
