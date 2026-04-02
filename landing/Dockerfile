# Coolify / Docker: run Astro Node adapter (serves static + /api/*). Do not use a static-only nginx image.
# Set env in Coolify: PUBLIC_SITE_URL, RESEND_API_KEY, RESEND_FROM, RESEND_BETA_NOTIFY_TO (team inbox for beta form), TURNSTILE_* as needed.

FROM node:22-alpine AS build
WORKDIR /app
RUN apk add --no-cache curl
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM node:22-alpine
WORKDIR /app
ENV NODE_ENV=production
ENV HOST=0.0.0.0
ENV PORT=3000
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh
COPY package.json package-lock.json ./
RUN npm ci --omit=dev
COPY --from=build /app/dist ./dist
# Last layer: curl for Coolify probes. Prefer health check URL http://127.0.0.1:<PORT>/ (not localhost) to avoid ::1 vs IPv4 "connection refused".
RUN apk add --no-cache curl wget ca-certificates \
  && test -x /usr/bin/curl \
  && /usr/bin/curl --version
EXPOSE 3000
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=5 \
  CMD node -e "require('http').get('http://127.0.0.1:'+(process.env.PORT||3000)+'/',r=>process.exit(r.statusCode>=200&&r.statusCode<500?0:1)).on('error',()=>process.exit(1))"
ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["node", "./dist/server/entry.mjs"]
