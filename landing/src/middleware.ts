import { defineMiddleware } from 'astro:middleware';
import TurndownService from 'turndown';

const turndownService = new TurndownService();

export const onRequest = defineMiddleware(async (context, next) => {
  // Handle case where context.request, context.request.url, or context.request.url.pathname might be undefined during static generation
  if (!context.request || !context.request.url || !context.request.url.pathname) {
    return next();
  }
  
  const path = context.request.url.pathname;

  // Set correct Content-Type for well-known paths (intercept request)
  if (path.startsWith('/.well-known/')) {
    // Continue processing to get the response
    const response = await next();
    
    if (path.endsWith('/api-catalog')) {
      response.headers.set('Content-Type', 'application/linkset+json');
    } else if (path.endsWith('/oauth-authorization-server') || 
               path.endsWith('/oauth-protected-resource') ||
               path.endsWith('/mcp/server-card.json') ||
               path.endsWith('/agent-skills/index.json')) {
      response.headers.set('Content-Type', 'application/json');
    }
    
    return response;
  }

  // For all other paths, check if the client accepts markdown
  const acceptHeader = context.request.headers.get('Accept') || '';
  const wantsMarkdown = acceptHeader.includes('text/markdown');

  // Only convert HTML responses to markdown
  const contentType = context.response.headers.get('Content-Type') || '';
  if (!wantsMarkdown || !contentType.includes('text/html')) {
    return next();
  }

  // Get the response from next middleware/handler
  const response = await next();

  // Clone the response to read its body
  const clonedResponse = response.clone();
  const html = await clonedResponse.text();

  // Convert HTML to markdown
  const markdown = turndownService.turndown(html);

  // Return a new response with markdown content
  return new Response(markdown, {
    status: response.status,
    headers: {
      'Content-Type': 'text/markdown; charset=utf-8',
      // Optionally add a header for markdown token count if available
      // 'x-markdown-tokens': String(markdown.split(/\s+/).length), // rough estimate
    },
  });
});