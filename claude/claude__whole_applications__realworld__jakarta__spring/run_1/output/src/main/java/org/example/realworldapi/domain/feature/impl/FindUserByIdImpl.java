package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.exception.UserNotFoundException;
import org.example.realworldapi.domain.feature.FindUserById;
import org.example.realworldapi.domain.model.user.User;
import org.example.realworldapi.domain.model.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FindUserByIdImpl implements FindUserById {

    private final UserRepository userRepository;

    public FindUserByIdImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User handle(UUID id) {
        return userRepository.findUserById(id).orElseThrow(UserNotFoundException::new);
    }
}
