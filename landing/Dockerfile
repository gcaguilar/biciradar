# Coolify / Docker: run Astro Node adapter (serves static + /api/*). Do not use a static-only nginx image.
# Set env in Coolify: PUBLIC_SITE_URL, RESEND_*, TURNSTILE_* as needed.

FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM node:22-alpine
WORKDIR /app
ENV NODE_ENV=production
ENV HOST=0.0.0.0
COPY package.json package-lock.json ./
RUN npm ci --omit=dev
COPY --from=build /app/dist ./dist
EXPOSE 8080
CMD ["node", "./dist/server/entry.mjs"]
