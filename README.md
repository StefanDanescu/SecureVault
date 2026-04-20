# SecureVault - Zero-Knowledge Encrypted Password Manager

A secure, local-first password manager built with JavaFX that uses industry-standard encryption to protect your credentials.

## Features

- **Zero-Knowledge Architecture**: Your master password is never stored - only used to derive the encryption key
- **Strong Encryption**: AES-256-GCM authenticated encryption
- **Secure Key Derivation**: PBKDF2-HMAC-SHA256 with 600,000 iterations (OWASP 2023 recommendation)
- **Password Generator**: Cryptographically secure random password generation
- **Password Strength Meter**: Entropy-based strength calculation with pattern detection
- **Auto-Lock**: Automatic vault locking after configurable inactivity period
- **Clipboard Auto-Clear**: Passwords are cleared from clipboard after a timeout
- **Categories**: Organize passwords into customizable categories
- **Search**: Real-time search across all entries
- **Import/Export**: CSV import/export for migration

## Requirements

- Java 21 or later
- Maven 3.8 or later

## Security Design

### Cryptographic Choices

| Component | Algorithm | Details |
|-----------|-----------|---------|
| Key Derivation | PBKDF2-HMAC-SHA256 | 600,000 iterations |
| Encryption | AES-256-GCM | 96-bit IV, 128-bit auth tag |
| Random | SecureRandom | For salts, IVs, passwords |
| Salt | 256-bit | Unique per vault |

### Vault File Format

```json
{
  "version": 1,
  "salt": "base64-encoded-32-byte-salt",
  "iterations": 600000,
  "encryptedData": "base64-encoded-iv-plus-ciphertext"
}
```

## Project Structure

```
SecureVault/
├── pom.xml
├── src/main/java/com/securevault/
│   ├── App.java                      # Application entry point
│   ├── crypto/                       # Cryptographic services
│   ├── model/                        # Data models
│   ├── service/                      # Business logic
│   ├── controller/                   # JavaFX controllers
│   └── util/                         # Utilities
├── src/main/resources/
│   ├── css/styles.css                # Dark theme stylesheet
│   └── fxml/                         # UI layouts
└── src/test/java/                    # Unit tests
```

## License

MIT License
