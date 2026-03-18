package com.securevault.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PasswordStrength.
 */
class PasswordStrengthTest {

    private PasswordStrength strengthCalculator;

    @BeforeEach
    void setUp() {
        strengthCalculator = new PasswordStrength();
    }

    @Test
    void testEmptyPassword() {
        // Act
        PasswordStrength.StrengthResult result = strengthCalculator.calculate("");

        // Assert
        assertEquals(PasswordStrength.Strength.VERY_WEAK, result.getStrength());
        assertEquals(0, result.getEntropy());
    }

    @Test
    void testNullPassword() {
        // Act
        PasswordStrength.StrengthResult result = strengthCalculator.calculate(null);

        // Assert
        assertEquals(PasswordStrength.Strength.VERY_WEAK, result.getStrength());
    }

    @Test
    void testWeakPassword() {
        // Act
        PasswordStrength.StrengthResult result = strengthCalculator.calculate("abc123");

        // Assert
        assertTrue(result.getStrength().getLevel() <= 1);
    }

    @Test
    void testStrongPassword() {
        // Act
        PasswordStrength.StrengthResult result = strengthCalculator.calculate("Kj#9mN$pQ2xR&wL5");

        // Assert
        assertTrue(result.getStrength().getLevel() >= 3);
    }

    @Test
    void testCommonPatternPenalty() {
        // Act
        PasswordStrength.StrengthResult withPattern = strengthCalculator.calculate("password123ABC");
        PasswordStrength.StrengthResult withoutPattern = strengthCalculator.calculate("xK9#mN$pQwRL");

        // Assert - password with common pattern should be weaker
        assertTrue(withPattern.getStrength().getLevel() < withoutPattern.getStrength().getLevel());
    }

    @Test
    void testSequentialCharactersPenalty() {
        // Act
        PasswordStrength.StrengthResult withSequence = strengthCalculator.calculate("abcdefghij");
        PasswordStrength.StrengthResult withoutSequence = strengthCalculator.calculate("xkmbzdriws");

        // Assert
        assertTrue(withSequence.getEntropy() < withoutSequence.getEntropy());
    }

    @Test
    void testPercentageCalculation() {
        // Act
        PasswordStrength.StrengthResult result = strengthCalculator.calculate("Kj#9mN$pQ2xR&wL5vB@3");

        // Assert
        assertTrue(result.getPercentage() > 0);
        assertTrue(result.getPercentage() <= 100);
    }

    @Test
    void testFeedbackProvided() {
        // Act
        PasswordStrength.StrengthResult result = strengthCalculator.calculate("short");

        // Assert
        assertNotNull(result.getFeedback());
        assertFalse(result.getFeedback().isEmpty());
    }
}
