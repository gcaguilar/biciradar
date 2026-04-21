import { defineMiddleware } from 'astro:middleware';
import TurndownService from 'turndown';
import { getAgentDiscoveryLinkValues, isHomepagePath } from './lib/agent-discovery';
import { normalizeBasePath } from './utils/paths';

const turndownService = new TurndownService();

export const onRequest = defineMiddleware(async (context, next) => {
  if (!context.request) {
    return next();
  }
  const requestUrl = new URL(context.request.url);
  const path = requestUrl.pathname;
  const response = await next();
  const normalizedBase = normalizeBasePath(import.meta.env.BASE_URL);
  const wellKnownPrefix =
    normalizedBase === '/' ? '/.well-known/' : `${normalizedBase}/.well-known/`;

  if (path.startsWith(wellKnownPrefix)) {
    if (path.endsWith('/api-catalog')) {
      response.headers.set('Content-Type', 'application/linkset+json');
    } else if (
      path.endsWith('/oauth-protected-resource') ||
      path.endsWith('/agent-skills/index.json')
    ) {
      response.headers.set('Content-Type', 'application/json');
    }
  }

  const contentType = response.headers.get('Content-Type') || '';
  if (contentType.includes('text/html') && isHomepagePath(path, import.meta.env.BASE_URL)) {
    for (const value of getAgentDiscoveryLinkValues(import.meta.env.BASE_URL)) {
      response.headers.append('Link', value);
    }
  }

  const acceptHeader = context.request.headers.get('Accept') || '';
  const wantsMarkdown = acceptHeader.includes('text/markdown');
  const isHtmlResponse = contentType.includes('text/html');
  const isSafeMethod = context.request.method === 'GET' || context.request.method === 'HEAD';

  if (!wantsMarkdown || !isHtmlResponse || !isSafeMethod) {
    if (isHtmlResponse) {
      response.headers.set('Vary', appendVaryHeader(response.headers.get('Vary'), 'Accept'));
    }
    return response;
  }

  const html = await response.clone().text();
  const markdown = turndownService.turndown(html);
  const markdownHeaders = new Headers(response.headers);
  const tokenCount = markdown.trim() ? markdown.trim().split(/\s+/u).length : 0;

  markdownHeaders.set('Content-Type', 'text/markdown; charset=utf-8');
  markdownHeaders.set('x-markdown-tokens', String(tokenCount));
  markdownHeaders.set('Vary', appendVaryHeader(markdownHeaders.get('Vary'), 'Accept'));
  markdownHeaders.delete('Content-Length');
  markdownHeaders.delete('Content-Encoding');
  markdownHeaders.delete('ETag');

  return new Response(context.request.method === 'HEAD' ? null : markdown, {
    status: response.status,
    statusText: response.statusText,
    headers: markdownHeaders,
  });
});

function appendVaryHeader(currentValue: string | null, value: string) {
  if (!currentValue) {
    return value;
  }

  const entries = currentValue
    .split(',')
    .map((entry) => entry.trim())
    .filter(Boolean);
  const entriesLower = entries.map((entry) => entry.toLowerCase());
  const valueLower = value.toLowerCase();

  return entriesLower.includes(valueLower) ? currentValue : `${currentValue}, ${value}`;
}
