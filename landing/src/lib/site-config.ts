import type { Locale } from '../content/marketing/types';
import type { CityKey } from '../content/marketing/types';

const APP_STORE_APP_ID = '6760931316';

const appStoreUrlByLocale: Record<Locale, string> = {
  es: `https://apps.apple.com/es/app/biciradar/id${APP_STORE_APP_ID}`,
  en: `https://apps.apple.com/us/app/biciradar/id${APP_STORE_APP_ID}`,
  ca: `https://apps.apple.com/es/app/biciradar/id${APP_STORE_APP_ID}`,
  gl: `https://apps.apple.com/es/app/biciradar/id${APP_STORE_APP_ID}`,
  eu: `https://apps.apple.com/es/app/biciradar/id${APP_STORE_APP_ID}`,
};

const cityToSystem: Record<CityKey, string> = {
  madrid: 'bicimad',
  barcelona: 'bicing',
  sevilla: 'sevici',
  valencia: 'valenbisi',
  zaragoza: 'bizi',
  murcia: 'murcia',
  bilbao: 'bilbao',
  leon: 'leon',
  valladolid: 'valladolid',
  palma: 'palma',
  las_palmas: 'laspalmas',
  a_coruna: 'acoruna',
  gijon: 'gijon',
  vitoria_gasteiz: 'vitoria',
  pamplona: 'pamplona',
  castellon: 'castellon',
  santander: 'santander',
  girona: 'girona',
  gran_canaria: 'grancanaria',
};

const PLAY_PACKAGE = 'com.gcaguilar.biciradar';

const playStoreHl: Record<Locale, string> = {
  es: 'es',
  en: 'en',
  ca: 'ca',
  gl: 'gl',
  eu: 'eu',
};

export function appStoreUrlForLocale(locale: Locale): string {
  return appStoreUrlByLocale[locale];
}

export function playStoreUrlForLocale(locale: Locale): string {
  return `https://play.google.com/store/apps/details?id=${PLAY_PACKAGE}&hl=${playStoreHl[locale]}`;
}

export function cityToDeepLink(cityKey: CityKey, locale: Locale): string {
  const system = cityToSystem[cityKey];
  return `biciradar://${system}?hl=${playStoreHl[locale]}`;
}

export { cityToSystem };

export const siteConfig = {
  siteName: 'BiciRadar',
  supportEmail: 'soporte@biciradar.es',
  contactEmail: 'hola@biciradar.es',
  appStoreUrl: appStoreUrlByLocale.es,
  playStoreUrl: playStoreUrlForLocale('es'),
  githubUrl: 'https://github.com/gcaguilar/biciradar',
} as const;
