package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.IsFollowingUser;
import org.example.realworldapi.domain.model.user.FollowRelationshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class IsFollowingUserImpl implements IsFollowingUser {

    @Autowired
    private FollowRelationshipRepository usersFollowedRepository;

    @Override
    public boolean handle(UUID currentUserId, UUID followedUserId) {
        return usersFollowedRepository.isFollowing(currentUserId, followedUserId);
    }
}
