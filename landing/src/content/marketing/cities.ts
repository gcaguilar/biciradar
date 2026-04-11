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
  bilbao: {
    key: 'bilbao',
    slug: 'bilbao Bilbo',
    system: 'Bilbao',
    names: {
      es: 'Bilbao',
      en: 'Bilbao',
      ca: 'Bilbao',
      gl: 'Bilbao',
      eu: 'Bilbo',
    },
  },
  murcia: {
    key: 'murcia',
    slug: 'murcia-murcia',
    system: 'Murcia',
    names: {
      es: 'Murcia',
      en: 'Murcia',
      ca: 'Múrcia',
      gl: 'Murcia',
      eu: 'Murtzia',
    },
  },
  leon: {
    key: 'leon',
    slug: 'leon-leon',
    system: 'León',
    names: {
      es: 'León',
      en: 'León',
      ca: 'Lleó',
      gl: 'León',
      eu: 'León',
    },
  },
  valladolid: {
    key: 'valladolid',
    slug: 'valladolid-valladolid',
    system: 'Valladolid',
    names: {
      es: 'Valladolid',
      en: 'Valladolid',
      ca: 'Valladolid',
      gl: 'Valladolid',
      eu: 'Valladolid',
    },
  },
  palma: {
    key: 'palma',
    slug: 'palma-palma',
    system: 'Palma',
    names: {
      es: 'Palma de Mallorca',
      en: 'Palma de Mallorca',
      ca: 'Palma',
      gl: 'Palma',
      eu: 'Palma',
    },
  },
  las_palmas: {
    key: 'las_palmas',
    slug: 'las-palmas-gran-canaria',
    system: 'Las Palmas',
    names: {
      es: 'Las Palmas',
      en: 'Las Palmas',
      ca: 'Las Palmas',
      gl: 'Las Palmas',
      eu: 'Las Palmas',
    },
  },
  a_coruna: {
    key: 'a_coruna',
    slug: 'a-coruna-coruna',
    system: 'A Coruña',
    names: {
      es: 'A Coruña',
      en: 'A Coruña',
      ca: 'La Coruña',
      gl: 'A Coruña',
      eu: 'Coruña',
    },
  },
  gijon: {
    key: 'gijon',
    slug: 'gijon-gijon',
    system: 'Gijón',
    names: {
      es: 'Gijón',
      en: 'Gijón',
      ca: 'Gijón',
      gl: 'Xixón',
      eu: 'Gijón',
    },
  },
  vitoria_gasteiz: {
    key: 'vitoria_gasteiz',
    slug: 'vitoria-gasteiz',
    system: 'Vitoria',
    names: {
      es: 'Vitoria-Gasteiz',
      en: 'Vitoria-Gasteiz',
      ca: 'Vitòria-Gasteiz',
      gl: 'Vitoria-Gasteiz',
      eu: 'Gasteiz',
    },
  },
  pamplona: {
    key: 'pamplona',
    slug: 'pamplona-iran',
    system: 'Pamplona',
    names: {
      es: 'Pamplona',
      en: 'Pamplona',
      ca: 'Pamplona',
      gl: 'Pamplona',
      eu: 'Iruña',
    },
  },
  castellon: {
    key: 'castellon',
    slug: 'castellon-castellon',
    system: 'Castellón',
    names: {
      es: 'Castellón',
      en: 'Castellón',
      ca: 'Castelló',
      gl: 'Castellón',
      eu: 'Castelló',
    },
  },
  santander: {
    key: 'santander',
    slug: 'santander-tubilla',
    system: 'Santander',
    names: {
      es: 'Santander',
      en: 'Santander',
      ca: 'Santander',
      gl: 'Santander',
      eu: 'Santander',
    },
  },
  girona: {
    key: 'girona',
    slug: 'girona-girocleta',
    system: 'Girona',
    names: {
      es: 'Girona',
      en: 'Girona',
      ca: 'Girona',
      gl: 'Girona',
      eu: 'Girona',
    },
  },
  gran_canaria: {
    key: 'gran_canaria',
    slug: 'gran-canaria-gran-canaria',
    system: 'Gran Canaria',
    names: {
      es: 'Gran Canaria',
      en: 'Gran Canaria',
      ca: 'Gran Canària',
      gl: 'Gran Canaria',
      eu: 'Gran Canaria',
    },
  },
};

export const orderedCityKeys = Object.keys(cityDefinitions) as CityKey[];

export function getCityBySlug(slug: string) {
  return orderedCityKeys
    .map((key) => cityDefinitions[key])
    .find((city) => city.slug === slug);
}
