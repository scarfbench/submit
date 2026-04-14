package org.example.realworldapi.domain.validator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.example.realworldapi.domain.exception.ModelValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ModelValidator {

    @Autowired
    private Validator validator;

    public <T> T validate(T model) {
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(model);

        if (!constraintViolations.isEmpty()) {
            final var messages =
                    constraintViolations.stream()
                            .map(ConstraintViolation::getMessage)
                            .collect(Collectors.toList());
            throw new ModelValidationException(messages);
        }

        return model;
    }
}
