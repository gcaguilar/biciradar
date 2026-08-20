# Docker: build Astro static output and serve it with nginx.

FROM node:26-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:1.30.4-alpine
COPY --from=build /app/dist /usr/share/nginx/html
RUN apk add --no-cache wget
EXPOSE 80
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=5 \
  CMD wget -qO- http://127.0.0.1/ >/dev/null || exit 1
