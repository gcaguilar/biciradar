import { getAllCityDefinitions, getLocaleContent, type CityKey, type Locale } from '../content/marketing';
import { localeNames, locales } from './i18n';
import { getCityPath, resolvePagePath, type RouteContext } from './routes';

export function getLanguageSwitchLinks(route: RouteContext) {
  return locales.map((locale) => {
    const localizedRoute: RouteContext =
      route.kind === 'city'
        ? { kind: 'city', locale, cityKey: route.cityKey as CityKey }
        : { kind: route.kind, locale };

    return {
      href: resolvePagePath(localizedRoute),
      label: localeNames[locale],
      localeLabel: getLocaleContent(locale).localeLabel,
      current: locale === route.locale,
    };
  });
}

export function getLocalizedCityLinks(locale: Locale) {
  return getAllCityDefinitions(locale).map((city) => ({
    key: city.key,
    href: getCityPath(locale, city.key),
    label: `${city.name} · ${city.system}`,
  }));
}
