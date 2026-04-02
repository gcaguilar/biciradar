# Coolify / Docker: run Astro Node adapter (serves static + /api/*). Do not use a static-only nginx image.
# Set env in Coolify: PUBLIC_SITE_URL, RESEND_*, TURNSTILE_* as needed.

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
COPY package.json package-lock.json ./
RUN apk add --no-cache curl
RUN npm ci --omit=dev
COPY --from=build /app/dist ./dist
EXPOSE 3000
# 127.0.0.1 avoids localhost → ::1 (IPv6) while Node listens on 0.0.0.0 (IPv4) → connection refused.
# Coolify: set health check URL to http://127.0.0.1:<PORT>/ (match PORT; curl is installed for Coolify's probe).
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=5 \
  CMD node -e "require('http').get('http://127.0.0.1:'+(process.env.PORT||3000)+'/',r=>process.exit(r.statusCode>=200&&r.statusCode<500?0:1)).on('error',()=>process.exit(1))"
CMD ["node", "./dist/server/entry.mjs"]
