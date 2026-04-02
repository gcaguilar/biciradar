import type { CityKey, Locale } from '../content/marketing/types';
import { cityDefinitions, getCityBySlug } from '../content/marketing/cities';
import { defaultLocale } from './i18n';

export type PageKind = 'home' | 'thank-you' | 'city';

export interface RouteContext {
  kind: PageKind;
  locale: Locale;
  cityKey?: CityKey;
}

const localizedStaticSlugs: Record<Locale, { thankYou: string }> = {
  es: { thankYou: 'gracias-beta' },
  en: { thankYou: 'beta-thanks' },
  ca: { thankYou: 'gracies-beta' },
  gl: { thankYou: 'grazas-beta' },
  eu: { thankYou: 'beta-eskerrak' },
};

export function getHomePath(locale: Locale) {
  return locale === defaultLocale ? '/' : `/${locale}/`;
}

export function getThankYouPath(locale: Locale) {
  const slug = localizedStaticSlugs[locale].thankYou;
  return locale === defaultLocale ? `/${slug}/` : `/${locale}/${slug}/`;
}

export function getCityPath(locale: Locale, cityKey: CityKey) {
  const slug = cityDefinitions[cityKey].slug;
  return locale === defaultLocale ? `/${slug}/` : `/${locale}/${slug}/`;
}

export function resolvePagePath(route: RouteContext) {
  if (route.kind === 'home') {
    return getHomePath(route.locale);
  }

  if (route.kind === 'thank-you') {
    return getThankYouPath(route.locale);
  }

  if (!route.cityKey) {
    throw new Error('City route requires cityKey');
  }

  return getCityPath(route.locale, route.cityKey);
}

export function getLocalizedStaticSlug(locale: Locale, key: 'thankYou') {
  return localizedStaticSlugs[locale][key];
}

export function resolveRouteContext(
  locale: Locale,
  slug?: string,
): RouteContext | null {
  if (!slug) {
    return { kind: 'home', locale };
  }

  if (slug === localizedStaticSlugs[locale].thankYou) {
    return { kind: 'thank-you', locale };
  }

  const city = getCityBySlug(slug);
  if (city) {
    return { kind: 'city', locale, cityKey: city.key };
  }

  return null;
}
