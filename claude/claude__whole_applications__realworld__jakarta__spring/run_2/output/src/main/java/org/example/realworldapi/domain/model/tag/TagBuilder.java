package org.example.realworldapi.domain.model.tag;

import org.example.realworldapi.domain.validator.ModelValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TagBuilder {
    @Autowired
    private ModelValidator modelValidator;

    public Tag build(String name) {
        return modelValidator.validate(new Tag(UUID.randomUUID(), name));
    }

    public Tag build(UUID id, String name) {
        return modelValidator.validate(new Tag(id, name));
    }
}
