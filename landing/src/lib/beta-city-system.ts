import { cityKeys, type CityKey } from '../content/marketing/types';

export const CITY_TO_BIKE_SYSTEM: Record<CityKey, { slug: string; label: string }> = {
  madrid: { slug: 'bicimad', label: 'BiciMAD' },
  barcelona: { slug: 'bicing', label: 'Bicing' },
  sevilla: { slug: 'sevici', label: 'Sevici' },
  valencia: { slug: 'valenbisi', label: 'Valenbisi' },
  zaragoza: { slug: 'bizi', label: 'Bizi' },
};

export function parseCityKey(value: string): CityKey | null {
  return cityKeys.includes(value as CityKey) ? (value as CityKey) : null;
}

export function bikeSystemLabelForCity(city: CityKey): string {
  return CITY_TO_BIKE_SYSTEM[city].label;
}
