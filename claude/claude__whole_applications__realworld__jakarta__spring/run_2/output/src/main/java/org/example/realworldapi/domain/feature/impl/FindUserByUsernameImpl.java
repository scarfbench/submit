package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.exception.UserNotFoundException;
import org.example.realworldapi.domain.feature.FindUserByUsername;
import org.example.realworldapi.domain.model.user.User;
import org.example.realworldapi.domain.model.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FindUserByUsernameImpl implements FindUserByUsername {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User handle(String username) {
        return userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
    }
}
