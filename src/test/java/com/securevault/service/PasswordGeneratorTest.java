package com.securevault.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PasswordGenerator.
 */
class PasswordGeneratorTest {

    private PasswordGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new PasswordGenerator();
    }

    @Test
    void testGenerateWithAllOptions() {
        // Act
        String password = generator.generate(16, true, true, true, true);

        // Assert
        assertEquals(16, password.length());
        assertTrue(password.matches(".*[A-Z].*"), "Should contain uppercase");
        assertTrue(password.matches(".*[a-z].*"), "Should contain lowercase");
        assertTrue(password.matches(".*[0-9].*"), "Should contain numbers");
        assertTrue(password.matches(".*[^A-Za-z0-9].*"), "Should contain symbols");
    }

    @Test
    void testGenerateUppercaseOnly() {
        // Act
        String password = generator.generate(12, true, false, false, false);

        // Assert
        assertEquals(12, password.length());
        assertTrue(password.matches("[A-Z]+"));
    }

    @Test
    void testGenerateLowercaseOnly() {
        // Act
        String password = generator.generate(12, false, true, false, false);

        // Assert
        assertEquals(12, password.length());
        assertTrue(password.matches("[a-z]+"));
    }

    @Test
    void testGenerateNumbersOnly() {
        // Act
        String password = generator.generate(12, false, false, true, false);

        // Assert
        assertEquals(12, password.length());
        assertTrue(password.matches("[0-9]+"));
    }

    @Test
    void testGenerateProducesUniquePasswords() {
        // Act
        String password1 = generator.generate(20);
        String password2 = generator.generate(20);

        // Assert
        assertNotEquals(password1, password2);
    }

    @Test
    void testGenerateDefaultLength() {
        // Act
        String password = generator.generate(8);

        // Assert
        assertEquals(8, password.length());
    }
}
