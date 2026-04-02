import type { CityKey, Locale } from './types';

export interface CityDefinition {
  key: CityKey;
  slug: string;
  system: string;
  names: Record<Locale, string>;
}

export const cityDefinitions: Record<CityKey, CityDefinition> = {
  madrid: {
    key: 'madrid',
    slug: 'madrid-bicimad',
    system: 'BiciMAD',
    names: {
      es: 'Madrid',
      en: 'Madrid',
      ca: 'Madrid',
      gl: 'Madrid',
      eu: 'Madril',
    },
  },
  barcelona: {
    key: 'barcelona',
    slug: 'barcelona-bicing',
    system: 'Bicing',
    names: {
      es: 'Barcelona',
      en: 'Barcelona',
      ca: 'Barcelona',
      gl: 'Barcelona',
      eu: 'Bartzelona',
    },
  },
  sevilla: {
    key: 'sevilla',
    slug: 'sevilla-sevici',
    system: 'Sevici',
    names: {
      es: 'Sevilla',
      en: 'Seville',
      ca: 'Sevilla',
      gl: 'Sevilla',
      eu: 'Sevilla',
    },
  },
  valencia: {
    key: 'valencia',
    slug: 'valencia-valenbisi',
    system: 'Valenbisi',
    names: {
      es: 'Valencia',
      en: 'Valencia',
      ca: 'Valencia',
      gl: 'Valencia',
      eu: 'Valentzia',
    },
  },
  zaragoza: {
    key: 'zaragoza',
    slug: 'zaragoza-bizi',
    system: 'Bizi',
    names: {
      es: 'Zaragoza',
      en: 'Zaragoza',
      ca: 'Saragossa',
      gl: 'Zaragoza',
      eu: 'Zaragoza',
    },
  },
};

export const orderedCityKeys = Object.keys(cityDefinitions) as CityKey[];

export function getCityBySlug(slug: string) {
  return orderedCityKeys
    .map((key) => cityDefinitions[key])
    .find((city) => city.slug === slug);
}
