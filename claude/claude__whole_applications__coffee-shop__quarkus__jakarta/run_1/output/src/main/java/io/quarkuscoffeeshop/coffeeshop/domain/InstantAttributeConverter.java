package io.quarkuscoffeeshop.coffeeshop.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * JPA AttributeConverter for java.time.Instant to java.sql.Timestamp.
 *
 * EclipseLink does not natively support java.time.Instant mapping to SQL TIMESTAMP,
 * so this converter handles the conversion explicitly.
 *
 * The autoApply=true means this converter will be automatically applied to all
 * Instant fields in all entities without needing @Convert annotations.
 */
@Converter(autoApply = true)
public class InstantAttributeConverter implements AttributeConverter<Instant, Timestamp> {

    @Override
    public Timestamp convertToDatabaseColumn(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    @Override
    public Instant convertToEntityAttribute(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
