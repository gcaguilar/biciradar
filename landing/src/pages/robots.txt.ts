import type { APIRoute } from 'astro';
import { absoluteUrl, resolveSiteUrl } from '../utils/site';

export const prerender = true;

export const GET: APIRoute = ({ site }) => {
  const siteUrl = resolveSiteUrl(site);
  const sitemapUrl = absoluteUrl('/sitemap.xml', siteUrl, import.meta.env.BASE_URL);
  const body = [
    'User-agent: *',
    'Allow: /',
    '',
    'Content-Signal: ai-train=no, search=yes, ai-input=no',
    '',
    `Sitemap: ${sitemapUrl}`
  ].join('\n');

  return new Response(body, {
    headers: {
      'Content-Type': 'text/plain; charset=utf-8',
    },
  });
};
