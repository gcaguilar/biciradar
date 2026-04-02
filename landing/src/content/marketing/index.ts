import { cityDefinitions, orderedCityKeys } from './cities';
import { ca } from './locales/ca';
import { en } from './locales/en';
import { es } from './locales/es';
import { eu } from './locales/eu';
import { gl } from './locales/gl';
import type { CityKey, Locale, LocaleContent } from './types';

export * from './cities';
export * from './types';

export const marketingContent: Record<Locale, LocaleContent> = {
  es,
  en,
  ca,
  gl,
  eu,
};

export function getLocaleContent(locale: Locale) {
  return marketingContent[locale];
}

export function getLocalizedCityDefinition(locale: Locale, cityKey: CityKey) {
  const city = cityDefinitions[cityKey];
  return {
    ...city,
    name: city.names[locale],
  };
}

export function getAllCityDefinitions(locale: Locale) {
  return orderedCityKeys.map((cityKey) => getLocalizedCityDefinition(locale, cityKey));
}
