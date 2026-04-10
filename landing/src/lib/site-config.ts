import type { Locale } from '../content/marketing/types';

const APP_STORE_APP_ID = '6760931316';

const appStoreUrlByLocale: Record<Locale, string> = {
  es: `https://apps.apple.com/es/app/biciradar/id${APP_STORE_APP_ID}`,
  en: `https://apps.apple.com/us/app/biciradar/id${APP_STORE_APP_ID}`,
  ca: `https://apps.apple.com/es/app/biciradar/id${APP_STORE_APP_ID}`,
  gl: `https://apps.apple.com/es/app/biciradar/id${APP_STORE_APP_ID}`,
  eu: `https://apps.apple.com/es/app/biciradar/id${APP_STORE_APP_ID}`,
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

export const siteConfig = {
  siteName: 'BiciRadar',
  supportEmail: 'soporte@biciradar.es',
  contactEmail: 'hola@biciradar.es',
  appStoreUrl: appStoreUrlByLocale.es,
  playStoreUrl: playStoreUrlForLocale('es'),
  githubUrl: 'https://github.com/gcaguilar/biciradar',
} as const;
