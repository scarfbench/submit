package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.IsFollowingUser;
import org.example.realworldapi.domain.model.user.FollowRelationshipRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class IsFollowingUserImpl implements IsFollowingUser {

    private final FollowRelationshipRepository usersFollowedRepository;

    public IsFollowingUserImpl(FollowRelationshipRepository usersFollowedRepository) {
        this.usersFollowedRepository = usersFollowedRepository;
    }

    @Override
    public boolean handle(UUID currentUserId, UUID followedUserId) {
        return usersFollowedRepository.isFollowing(currentUserId, followedUserId);
    }
}
