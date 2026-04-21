import type { APIRoute } from 'astro';
import { getApiCatalog } from '../../lib/well-known';

export const GET: APIRoute = ({ site }) =>
  Response.json(getApiCatalog(site, import.meta.env.BASE_URL), {
    headers: {
      'Content-Type':
        'application/linkset+json; profile="https://www.rfc-editor.org/info/rfc9727"',
    },
  });
