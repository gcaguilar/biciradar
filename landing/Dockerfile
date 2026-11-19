# Build the Astro static output as an unprivileged user.
FROM node:26.13.1-alpine3.24@sha256:6bd9ab43c0ec0190ca7b2d193906cb8b7edf6e7f4afa43ddeccd6452710bffb5 AS build

WORKDIR /app
# The pinned base image ships zlib 1.3.2-r1; apply the patched 1.4-r0 from the
# Alpine 3.24 repositories before dropping privileges.
RUN apk upgrade --no-cache zlib
RUN chown 1000:1000 /app

COPY --chown=1000:1000 package.json package-lock.json ./
ENV HOME=/home/node
USER 1000:1000
RUN npm ci --ignore-scripts --no-audit --no-fund

# Copy only the files Astro needs; keep the image independent of local secrets
# and build leftovers even if .dockerignore is accidentally weakened.
COPY --chown=1000:1000 astro.config.mjs tsconfig.json ./
COPY --chown=1000:1000 public ./public
COPY --chown=1000:1000 src ./src
RUN npm run build

# Runtime: nginx 1.30.7 listens on 8080 as UID/GID 101 with a rootless config.
FROM nginx:1.30.7-alpine3.24-slim@sha256:e2fc266695cef82e3a686c7ed628d1f09c2a7bb5ffb2a718ae7f7c821f5583c1

LABEL org.opencontainers.image.title="BiciRadar landing" \
      org.opencontainers.image.description="Static BiciRadar landing site served by rootless nginx" \
      org.opencontainers.image.source="https://github.com/gcaguilar/biciradar" \
      org.opencontainers.image.licenses="AGPL-3.0-only"

# The official image starts as root and writes to /run, /var/log and /var/cache.
# Replace its config so PID, logs and temp files stay under /tmp, then drop to
# UID 101. Same fix as the build stage: upgrade the vulnerable zlib.
COPY --chown=101:101 nginx.conf /etc/nginx/nginx.conf
RUN apk upgrade --no-cache zlib
COPY --chown=101:101 default.conf /etc/nginx/conf.d/default.conf
COPY --from=build --chown=101:101 /app/dist /usr/share/nginx/html

USER 101:101
# Non-root nginx cannot bind port 80; reverse proxies must target 8080.
EXPOSE 8080
STOPSIGNAL SIGQUIT

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD ["wget", "-q", "-O", "/dev/null", "http://127.0.0.1:8080/"]
