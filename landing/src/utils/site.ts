import { normalizeBasePath, withBase } from './paths';

export const DEFAULT_SITE_URL = 'https://biciradar.es';

export function resolveSiteUrl(site?: URL | string) {
  if (site instanceof URL) {
    return site;
  }

  if (typeof site === 'string' && site.trim()) {
    return new URL(site);
  }

  return new URL(DEFAULT_SITE_URL);
}

export function absoluteUrl(path: string, site?: URL | string, basePath = '/') {
  return new URL(withBase(path, basePath), resolveSiteUrl(site)).toString();
}

export function withBasePage(path: string, basePath = '/') {
  const normalizedBase = normalizeBasePath(basePath);
  const trimmed = path.trim();

  if (!trimmed || trimmed === '/') {
    return normalizedBase === '/' ? '/' : `${normalizedBase}/`;
  }

  const normalizedPath = trimmed.replace(/^\/+|\/+$/g, '');
  return normalizedBase === '/'
    ? `/${normalizedPath}/`
    : `${normalizedBase}/${normalizedPath}/`;
}

export function absolutePageUrl(
  path: string,
  site?: URL | string,
  basePath = '/',
) {
  return new URL(withBasePage(path, basePath), resolveSiteUrl(site)).toString();
}
