import type { CityKey, Locale } from '../content/marketing/types';
import { locales } from './i18n';
import { resolvePagePath, type RouteContext } from './routes';
import { absolutePageUrl } from '../utils/site';

export interface AlternateLink {
  hreflang: string;
  href: string;
}

export function buildAlternateLinks(
  route: RouteContext,
  site: URL | string,
  basePath = '/',
): AlternateLink[] {
  const links = locales.map((locale) => {
    const localizedRoute: RouteContext =
      route.kind === 'city'
        ? { kind: 'city', locale, cityKey: route.cityKey as CityKey }
        : { kind: route.kind, locale };

    return {
      hreflang: locale,
      href: absolutePageUrl(resolvePagePath(localizedRoute), site, basePath),
    };
  });

  return [
    ...links,
    {
      hreflang: 'x-default',
      href: absolutePageUrl(resolvePagePath({ ...route, locale: 'es' as Locale }), site, basePath),
    },
  ];
}
