import type { APIRoute } from 'astro';
import { getOAuthDiscoveryMetadata } from '../../lib/well-known';

export const GET: APIRoute = ({ site }) => {
  const metadata = getOAuthDiscoveryMetadata(site, import.meta.env.BASE_URL);

  if (!metadata) {
    return new Response(null, { status: 404 });
  }

  return Response.json(metadata.openIdConfiguration, {
    headers: {
      'Content-Type': 'application/json; charset=utf-8',
    },
  });
};
