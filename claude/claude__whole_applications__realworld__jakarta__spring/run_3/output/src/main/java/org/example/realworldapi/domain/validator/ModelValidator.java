package org.example.realworldapi.domain.validator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.example.realworldapi.domain.exception.ModelValidationException;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ModelValidator {

    private final Validator validator;

    public ModelValidator(Validator validator) {
        this.validator = validator;
    }

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
