import type { APIRoute } from 'astro';
import { orderedCityKeys } from '../content/marketing';
import { absolutePageUrl, resolveSiteUrl } from '../utils/site';
import { locales } from '../lib/i18n';
import { getCityPath, getHomePath } from '../lib/routes';

export const prerender = true;

const marketingRoutes = locales.flatMap((locale) => [
  {
    path: getHomePath(locale),
    changefreq: 'weekly',
    priority: locale === 'es' ? '1.0' : '0.9',
  },
  ...orderedCityKeys.map((cityKey) => ({
    path: getCityPath(locale, cityKey),
    changefreq: 'weekly',
    priority: locale === 'es' ? '0.8' : '0.7',
  })),
]);

const routes = [
  ...marketingRoutes,
  {
    path: '/biciradar-complemento-app-oficial',
    changefreq: 'monthly',
    priority: '0.5',
  },
];

export const GET: APIRoute = ({ site }) => {
  const siteUrl = resolveSiteUrl(site);
  const urls = routes
    .map(
      (route) => `  <url>
    <loc>${absolutePageUrl(route.path, siteUrl, import.meta.env.BASE_URL)}</loc>
    <changefreq>${route.changefreq}</changefreq>
    <priority>${route.priority}</priority>
  </url>`,
    )
    .join('\n');

  const body = `<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
${urls}
</urlset>`;

  return new Response(body, {
    headers: {
      'Content-Type': 'application/xml; charset=utf-8',
    },
  });
};
