package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.exception.UserNotFoundException;
import org.example.realworldapi.domain.feature.FindUserById;
import org.example.realworldapi.domain.model.user.User;
import org.example.realworldapi.domain.model.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FindUserByIdImpl implements FindUserById {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User handle(UUID id) {
        return userRepository.findUserById(id).orElseThrow(UserNotFoundException::new);
    }
}
