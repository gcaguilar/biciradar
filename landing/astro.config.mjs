import { defineConfig } from 'astro/config';
import react from '@astrojs/react';

function normalizeBasePath(basePath = '/') {
  const trimmed = basePath.trim();

  if (!trimmed || trimmed === '/') {
    return '/';
  }

  const withoutEdgeSlashes = trimmed.replace(/^\/+|\/+$/g, '');
  return withoutEdgeSlashes ? `/${withoutEdgeSlashes}` : '/';
}

const site = process.env.PUBLIC_SITE_URL?.trim() || undefined;
const base = normalizeBasePath(process.env.PUBLIC_BASE_PATH ?? '/');

export default defineConfig({
  integrations: [react()],
  output: 'static',
  site,
  base,
});
