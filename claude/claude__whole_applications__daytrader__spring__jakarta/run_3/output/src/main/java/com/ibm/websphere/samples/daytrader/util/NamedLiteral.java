package com.ibm.websphere.samples.daytrader.util;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Named;

/**
 * AnnotationLiteral implementation for @Named qualifier.
 */
public class NamedLiteral extends AnnotationLiteral<Named> implements Named {
    private final String value;

    public NamedLiteral(String value) {
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }
}
