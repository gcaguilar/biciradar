# BiciRadar landing

Astro static site for <https://biciradar.es>. The normal deployment is GitHub
Pages (`.github/workflows/deploy-landing.yml`); the Docker image is an
alternative Coolify/self-hosted target.

## Container image

The `Dockerfile` is a two-stage build:

- **build**: Node 26 builds `dist/` as the unprivileged `node` user. npm install
  scripts are disabled because the current dependency tree does not need them.
- **runtime**: the official `nginx:1.30.7-alpine3.24-slim` image serves the
  static output as UID/GID `101` and listens on port `8080`. `nginx.conf`
  replaces the root-based defaults so PID, logs and temp files live under
  `/tmp`. No packages are added; both stages upgrade the base image's zlib to
  the patched Alpine package.

Build and run locally:

```bash
docker build -t biciradar-landing .

docker run --rm \
  --name biciradar-landing \
  -p 127.0.0.1:8080:8080 \
  --read-only \
  --tmpfs /tmp:rw,noexec,nosuid,size=16m \
  --cap-drop ALL \
  --security-opt no-new-privileges:true \
  biciradar-landing
```

Open <http://127.0.0.1:8080/>.

The `--read-only` mode is supported because nginx writes its PID and temporary
files under `/tmp`; no other path needs to be writable.

## Coolify / reverse proxy

- Use container port **8080**, not 80.
- Use `http://127.0.0.1:8080/` as the health-check URL.
- Do not override the container user to `root` and do not add
  `NET_BIND_SERVICE`; the image is already rootless.
- Terminate TLS at the edge. The image sends HSTS and security headers, but the
  proxy must preserve them.

## Security notes

- Base images are pinned by digest. The Docker Dependabot ecosystem in
  `.github/dependabot.yml` updates them.
- Runtime: UID/GID `101`, no new runtime packages (only `apk upgrade zlib`),
  nginx version disclosure disabled, CSP and hardening headers enabled, only
  `GET`/`HEAD` allowed, dotfiles denied, request bodies limited, autoindex
  disabled.
- Build: unprivileged `node` user and `npm ci --ignore-scripts`.
- The CSP allows inline scripts because Astro emits an inline analytics loader,
  WebMCP bootstrap, and JSON-LD blocks. Tightening it further requires a
  nonce/hash pipeline.
- `.dockerignore` keeps VCS data, environment files, build output, tests,
  `node_modules`, design sources, and email previews out of the build context.
