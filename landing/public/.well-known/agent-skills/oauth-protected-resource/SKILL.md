# OAuth Protected Resource Metadata

`/.well-known/oauth-protected-resource` publishes OAuth protected resource metadata for the BiciRadar landing API surface.

## Resource
- Resource identifier: `https://biciradar.es/api/beta-signup`
- Supported scope: `beta:signup`
- Bearer token transport: `Authorization` header

## Notes
- The metadata intentionally avoids advertising an authorization server that is not implemented in this repository.
