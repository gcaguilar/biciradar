# Apple Signing Secrets Setup

## Required Secrets

These secrets must be configured in GitHub repository settings (Settings → Secrets and variables → Actions):

| Secret Name | Description |
|------------|-------------|
| `APPLE_SIGNING_CERTIFICATE_P12_BASE64` | Base64-encoded P12 certificate (Apple Distribution) |
| `APPLE_SIGNING_CERTIFICATE_PASSWORD` | Password for the P12 certificate |
| `APPLE_PROVISIONING_PROFILE_BASE64` | Base64-encoded App Store provisioning profile (.mobileprovision) |
| `APPLE_KEYCHAIN_PASSWORD` | Password for temporary keychain (optional, defaults to `bizi-temporary-keychain-password`) |
| `APPLE_TEAM_ID` | Apple Developer Team ID (e.g., `B87AWPVZWB`) |

## How to Generate P12 Certificate (Linux/macOS)

### Prerequisites
- `distribution.cer` - Apple Distribution certificate
- `apple_distribution.key` - Private key (if separate from certificate)

### Generate P12 with OpenSSL 3.x

OpenSSL 3.x changed the default encryption algorithm, which is incompatible with macOS Security framework. Use SHA1-3DES algorithms:

```bash
# Navigate to certificate directory
cd apple-cert

# Create P12 with legacy-compatible algorithms
openssl pkcs12 -export \
  -in distribution.cer \
  -inkey apple_distribution.key \
  -passout pass:YOUR_PASSWORD \
  -out signing-certificate.p12 \
  -keypbe PBE-SHA1-3DES \
  -certpbe PBE-SHA1-3DES \
  -macalg SHA1

# Encode to base64
base64 -w 0 signing-certificate.p12
```

### Alternative: Using -legacy flag

```bash
openssl pkcs12 -export -legacy \
  -in distribution.cer \
  -inkey apple_distribution.key \
  -passout pass:YOUR_PASSWORD \
  -out signing-certificate.p12

base64 -w 0 signing-certificate.p12
```

## How to Generate Provisioning Profile Base64

```bash
# Download .mobileprovision from Apple Developer Portal
# Encode to base64
base64 -w 0 Profile.mobileprovision
```

## Troubleshooting

### Error: "MAC verification failed during PKCS12 import"

This error occurs when the P12 uses OpenSSL 3.x default algorithms (AES-256-CBC) instead of legacy algorithms compatible with macOS Security framework.

**Solution:** Recreate the P12 using SHA1-3DES algorithms as shown above.

### Error: "SecKeychainItemImport: MAC verification failed (wrong password?)"

1. Verify the password matches exactly
2. Ensure no special characters in password that might cause encoding issues
3. Recreate the P12 with legacy-compatible algorithms

## References

- [Stack Overflow: MAC verification failed during PKCS12 import](https://stackoverflow.com/a/77273121)
- [GitHub Discussion: P12 import error](https://github.com/orgs/codemagic-ci-cd/discussions/2805)
- [OpenSSL 3.x PKCS12 changes](https://www.openssl.org/docs/man3.0/man1/openssl-pkcs12.html)
