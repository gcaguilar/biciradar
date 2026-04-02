import { defaultLocale, locales, type Locale } from '../content/marketing/types';

export { defaultLocale, locales };
export type { Locale };

export const localeToHtmlLang: Record<Locale, string> = {
  es: 'es',
  en: 'en',
  ca: 'ca',
  gl: 'gl',
  eu: 'eu',
};

export const localeToOgLocale: Record<Locale, string> = {
  es: 'es_ES',
  en: 'en_US',
  ca: 'ca_ES',
  gl: 'gl_ES',
  eu: 'eu_ES',
};

export const localeNames: Record<Locale, string> = {
  es: 'Castellano',
  en: 'English',
  ca: 'Català',
  gl: 'Galego',
  eu: 'Euskera',
};

export function isLocale(value: string | undefined): value is Locale {
  return Boolean(value && locales.includes(value as Locale));
}

export function normalizeLocale(value: string | undefined): Locale {
  return isLocale(value) ? value : defaultLocale;
}
