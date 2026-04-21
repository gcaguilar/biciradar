import { defaultLocale, locales } from './i18n';
import { normalizeBasePath, withBase } from '../utils/paths';
import { hasMcpServerCardConfig, hasOAuthDiscoveryConfig } from './well-known';

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
    ['service-desc', '/api/openapi.json', 'application/openapi+json'],
    ['service-doc', '/api/docs', 'text/html'],
    ['describedby', '/llms.txt', 'text/plain'],
    ['service-meta', '/.well-known/agent-skills/index.json', 'application/json'],
  ] as const;

  const optionalTargets: Array<readonly [string, string, string]> = [];

  if (hasOAuthDiscoveryConfig()) {
    optionalTargets.push(
      ['service-meta', '/.well-known/oauth-protected-resource', 'application/json'],
      ['service-meta', '/.well-known/oauth-authorization-server', 'application/json'],
      ['service-meta', '/.well-known/openid-configuration', 'application/json'],
    );
  }

  if (hasMcpServerCardConfig()) {
    optionalTargets.push(['service-meta', '/.well-known/mcp/server-card.json', 'application/json']);
  }

  return [...discoveryTargets, ...optionalTargets].map(
    ([rel, path, type]) => `<${withBase(path, basePath)}>; rel="${rel}"; type="${type}"`,
  );
}
