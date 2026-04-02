import type { APIRoute } from 'astro';
import { absolutePageUrl, resolveSiteUrl } from '../utils/site';

export const prerender = true;

const routes = [
  {
    path: '/',
    changefreq: 'weekly',
    priority: '1.0',
  },
  {
    path: '/biciradar-complemento-app-oficial',
    changefreq: 'weekly',
    priority: '0.8',
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
