package org.example.realworldapi.domain.model.tag;

import org.example.realworldapi.domain.validator.ModelValidator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TagBuilder {

    private final ModelValidator modelValidator;

    public TagBuilder(ModelValidator modelValidator) {
        this.modelValidator = modelValidator;
    }

    public Tag build(String name) {
        return modelValidator.validate(new Tag(UUID.randomUUID(), name));
    }

    public Tag build(UUID id, String name) {
        return modelValidator.validate(new Tag(id, name));
    }
}
