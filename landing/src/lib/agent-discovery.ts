import { defaultLocale, locales } from './i18n';
import { normalizeBasePath, withBase } from '../utils/paths';

const localizedHomeLocales = locales.filter((locale) => locale !== defaultLocale);

function escapeRegExp(value: string) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

export function isHomepagePath(pathname: string, basePath = '/') {
  const normalizedBase = normalizeBasePath(basePath);
  if (pathname === normalizedBase || pathname === `${normalizedBase}/`) {
    return true;
  }

  if (!localizedHomeLocales.length) {
    return false;
  }

  const basePrefix = normalizedBase === '/' ? '' : escapeRegExp(normalizedBase);
  const localePattern = localizedHomeLocales.map(escapeRegExp).join('|');
  return new RegExp(`^${basePrefix}/(?:${localePattern})/?$`).test(pathname);
}

export function getAgentDiscoveryLinkValues(basePath = '/') {
  const discoveryTargets = [
    ['api-catalog', '/.well-known/api-catalog', 'application/linkset+json'],
    ['service-doc', '/api/docs', 'text/html'],
    ['describedby', '/llms.txt', 'text/plain'],
    ['oauth-protected-resource', '/.well-known/oauth-protected-resource', 'application/json'],
    ['agent-skills', '/.well-known/agent-skills/index.json', 'application/json'],
  ] as const;

  return discoveryTargets.map(
    ([rel, path, type]) => `<${withBase(path, basePath)}>; rel="${rel}"; type="${type}"`,
  );
}
