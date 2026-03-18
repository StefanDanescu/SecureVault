package com.securevault.service;

/**
 * Password strength calculator using entropy estimation.
 * 
 * Calculates password strength based on character set entropy
 * and detects common weakness patterns.
 */
public class PasswordStrength {

    public enum Strength {
        VERY_WEAK(0, "Very Weak", "#e74c3c"),      // Red
        WEAK(1, "Weak", "#e74c3c"),                 // Red
        FAIR(2, "Fair", "#e74c3c"),                 // Red
        STRONG(3, "Strong", "#f1c40f"),             // Yellow
        VERY_STRONG(4, "Very Strong", "#2ecc71");   // Green

        private final int level;
        private final String label;
        private final String color;

        Strength(int level, String label, String color) {
            this.level = level;
            this.label = label;
            this.color = color;
        }

        public int getLevel() {
            return level;
        }

        public String getLabel() {
            return label;
        }

        public String getColor() {
            return color;
        }
    }

    /**
     * Result of password strength evaluation.
     */
    public static class StrengthResult {
        private final Strength strength;
        private final double entropy;
        private final String feedback;

        public StrengthResult(Strength strength, double entropy, String feedback) {
            this.strength = strength;
            this.entropy = entropy;
            this.feedback = feedback;
        }

        public Strength getStrength() {
            return strength;
        }

        public double getEntropy() {
            return entropy;
        }

        public String getFeedback() {
            return feedback;
        }

        public double getPercentage() {
            // Map entropy to a 0-100 percentage (128 bits = 100%)
            return Math.min(100, (entropy / 128.0) * 100);
        }
    }

    /**
     * Calculates the strength of a password.
     * 
     * @param password The password to evaluate
     * @return The strength result
     */
    public StrengthResult calculate(String password) {
        if (password == null || password.isEmpty()) {
            return new StrengthResult(Strength.VERY_WEAK, 0, "Password is empty");
        }

        double entropy = calculateEntropy(password);
        String feedback = generateFeedback(password, entropy);
        Strength strength = entropyToStrength(entropy);

        return new StrengthResult(strength, entropy, feedback);
    }

    /**
     * Calculates the entropy of a password in bits.
     */
    private double calculateEntropy(String password) {
        int poolSize = 0;

        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSymbol = password.matches(".*[^a-zA-Z0-9].*");

        if (hasLower) poolSize += 26;
        if (hasUpper) poolSize += 26;
        if (hasDigit) poolSize += 10;
        if (hasSymbol) poolSize += 32;

        if (poolSize == 0) poolSize = 26; // Fallback

        // Entropy = length * log2(poolSize)
        double entropy = password.length() * (Math.log(poolSize) / Math.log(2));

        // Penalize common patterns
        entropy -= detectPatternPenalties(password);

        return Math.max(0, entropy);
    }

    /**
     * Detects common patterns and returns penalty bits.
     */
    private double detectPatternPenalties(String password) {
        double penalty = 0;
        String lower = password.toLowerCase();

        // Sequential characters
        if (containsSequence(lower)) {
            penalty += 10;
        }

        // Repeated characters
        if (lower.matches(".*(.)\\1{2,}.*")) {
            penalty += 10;
        }

        // Common words/patterns
        String[] commonPatterns = {"password", "123456", "qwerty", "admin", "letmein", "welcome"};
        for (String pattern : commonPatterns) {
            if (lower.contains(pattern)) {
                penalty += 20;
            }
        }

        return penalty;
    }

    /**
     * Checks for sequential characters (abc, 123, etc.)
     */
    private boolean containsSequence(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            char c1 = password.charAt(i);
            char c2 = password.charAt(i + 1);
            char c3 = password.charAt(i + 2);
            if (c2 == c1 + 1 && c3 == c2 + 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts entropy bits to strength level.
     */
    private Strength entropyToStrength(double entropy) {
        if (entropy < 28) return Strength.VERY_WEAK;
        if (entropy < 36) return Strength.WEAK;
        if (entropy < 60) return Strength.FAIR;
        if (entropy < 80) return Strength.STRONG;
        return Strength.VERY_STRONG;
    }

    /**
     * Generates feedback for improving the password.
     */
    private String generateFeedback(String password, double entropy) {
        if (password.length() < 8) {
            return "Use at least 8 characters";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Add uppercase letters";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Add lowercase letters";
        }
        if (!password.matches(".*\\d.*")) {
            return "Add numbers";
        }
        if (!password.matches(".*[^a-zA-Z0-9].*")) {
            return "Add symbols";
        }
        if (entropy < 60) {
            return "Consider making it longer";
        }
        return "Strong password!";
    }
}
