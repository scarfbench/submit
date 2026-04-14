package org.example.realworldapi.domain.model.user;

import org.example.realworldapi.domain.validator.ModelValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("usermodelbuilder")
public class UserModelBuilder {

    @Autowired
    private ModelValidator modelValidator;

    public User build(String username, String email, String password) {
        return modelValidator.validate(
                new User(UUID.randomUUID(), username, email, password, null, null));
    }

    public User build(
            UUID id, String username, String bio, String image, String password, String email) {
        return modelValidator.validate(new User(id, username, email, password, bio, image));
    }
}
