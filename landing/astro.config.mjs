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

/** Trust X-Forwarded-* from the reverse proxy so Astro.url.origin matches the browser Origin on POST (CSRF check). */
function buildSecurityAllowedDomains(siteUrlString) {
  try {
    const u = new URL(siteUrlString);
    const protocol = u.protocol === 'https:' ? 'https' : 'http';
    const { hostname } = u;
    if (!hostname) {
      return [];
    }
    const patterns = [{ hostname, protocol }];
    const isLocal =
      hostname === 'localhost' || hostname === '127.0.0.1' || hostname.endsWith('.localhost');
    if (!isLocal) {
      patterns.push({ hostname: `**.${hostname}`, protocol });
    }
    return patterns;
  } catch {
    return [];
  }
}

export default defineConfig({
  adapter: node({
    mode: 'standalone',
  }),
  site,
  base,
  security: {
    allowedDomains: buildSecurityAllowedDomains(site),
  },
});
