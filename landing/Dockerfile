# Build the Astro static output as an unprivileged user.
FROM node:26.13.1-alpine3.24@sha256:6bd9ab43c0ec0190ca7b2d193906cb8b7edf6e7f4afa43ddeccd6452710bffb5 AS build

WORKDIR /app
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

# Runtime: nginx-unprivileged listens on 8080 as UID/GID 101.
FROM nginxinc/nginx-unprivileged:1.30.6-alpine-slim@sha256:5d8bb9437d74a600ec5dbf90c035451c724dc33eac8d3a71078f6f8612c345f6

LABEL org.opencontainers.image.title="BiciRadar landing" \
      org.opencontainers.image.description="Static BiciRadar landing site served by rootless nginx" \
      org.opencontainers.image.source="https://github.com/gcaguilar/biciradar" \
      org.opencontainers.image.licenses="AGPL-3.0-only"

COPY --chown=101:101 default.conf /etc/nginx/conf.d/default.conf
COPY --from=build --chown=101:101 /app/dist /usr/share/nginx/html

USER 101:101
# Non-root nginx cannot bind port 80; reverse proxies must target 8080.
EXPOSE 8080
STOPSIGNAL SIGQUIT

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD ["wget", "-q", "-O", "/dev/null", "http://127.0.0.1:8080/"]
