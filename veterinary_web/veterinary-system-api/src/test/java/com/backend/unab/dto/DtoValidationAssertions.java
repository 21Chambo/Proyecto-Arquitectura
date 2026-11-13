package com.backend.unab.dto;

import jakarta.validation.Validation;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Shared boundary assertions for the input cases formerly validated by services. */
public final class DtoValidationAssertions {
    private DtoValidationAssertions() { }
    public static void assertRequired(Object payload, String field) {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertTrue(factory.getValidator().validate(payload).stream()
                    .anyMatch(error -> error.getPropertyPath().toString().equals(field)),
                    "Expected a declarative constraint on " + field);
        }
    }
}
