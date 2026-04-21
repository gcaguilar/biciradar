# Link Headers

The homepage advertises machine-readable resources with RFC 8288 `Link` response headers.

## Relations
- `api-catalog` -> `/.well-known/api-catalog`
- `service-doc` -> `/api/docs`
- `describedby` -> `/llms.txt`
- `oauth-protected-resource` -> `/.well-known/oauth-protected-resource`
- `agent-skills` -> `/.well-known/agent-skills/index.json`

## Notes
- HTML remains the default browser response.
- Discovery headers are attached to the root homepage and localized homepage routes.
