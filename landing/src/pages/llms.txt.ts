import type { APIRoute } from 'astro';
import { absolutePageUrl, resolveSiteUrl } from '../utils/site';

export const prerender = true;

export const GET: APIRoute = ({ site }) => {
  const siteUrl = resolveSiteUrl(site);
  const homeUrl = absolutePageUrl('/', siteUrl, import.meta.env.BASE_URL);
  const complementUrl = absolutePageUrl(
    '/biciradar-complemento-app-oficial',
    siteUrl,
    import.meta.env.BASE_URL,
  );

  const body = `# BiciRadar

> Official public site for BiciRadar, a shared-bike availability app built around official city open-data feeds.

## Canonical site
- ${homeUrl}

## Key pages
- Home: ${homeUrl}
- Product reference: ${complementUrl}

## Product scope
- Real-time availability for bikes and free docks in public bike-share stations.
- Favorites, alerts, widgets, watch surfaces, and quick access for daily commuting.
- Public-facing product overview and FAQ content for end users.

## Platforms
- iPhone and iOS
- Android
- Apple Watch
- Wear OS

## Cities and systems
- Madrid: BiciMAD
- Barcelona: Bicing
- Zaragoza: Bizi
- Valencia: Valenbisi
- Sevilla: Sevici
- More GBFS-compatible systems when official feeds are available

## Data sources
- Official GBFS feeds and public city open-data APIs when available.
- BiciRadar is intended as a fast companion layer on top of official public-bike services, not a replacement for the institutional app.

## Official distribution
- App Store: https://apps.apple.com/es/app/biciradar/id6760931316
- Google Play: https://play.google.com/store/apps/details?id=com.gcaguilar.biciradar

## Source of truth
- Public website: ${homeUrl}
- Repository: https://github.com/gcaguilar/biciradar
`;

  return new Response(body, {
    headers: {
      'Content-Type': 'text/plain; charset=utf-8',
    },
  });
};
