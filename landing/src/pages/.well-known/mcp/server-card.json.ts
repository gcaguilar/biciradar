import type { APIRoute } from 'astro';
import { getMcpServerCard } from '../../../lib/well-known';

export const GET: APIRoute = ({ site }) => {
  const serverCard = getMcpServerCard(site, import.meta.env.BASE_URL);

  if (!serverCard) {
    return new Response(null, { status: 404 });
  }

  return Response.json(serverCard, {
    headers: {
      'Content-Type': 'application/json; charset=utf-8',
    },
  });
};
