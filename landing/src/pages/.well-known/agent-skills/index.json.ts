import type { APIRoute } from 'astro';
import { getAgentSkillsIndex } from '../../../lib/well-known';

export const GET: APIRoute = ({ site }) =>
  Response.json(getAgentSkillsIndex(site, import.meta.env.BASE_URL), {
    headers: {
      'Content-Type': 'application/json; charset=utf-8',
    },
  });
