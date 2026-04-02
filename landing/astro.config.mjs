import { defineConfig } from 'astro/config';
import node from '@astrojs/node';

function normalizeBasePath(basePath = '/') {
  const trimmed = basePath.trim();

  if (!trimmed || trimmed === '/') {
    return '/';
  }

  const withoutEdgeSlashes = trimmed.replace(/^\/+|\/+$/g, '');
  return withoutEdgeSlashes ? `/${withoutEdgeSlashes}` : '/';
}

const site = process.env.PUBLIC_SITE_URL?.trim() || 'https://biciradar.es';
const base = normalizeBasePath(process.env.PUBLIC_BASE_PATH ?? '/');

export default defineConfig({
  adapter: node({
    mode: 'standalone',
  }),
  site,
  base,
});
