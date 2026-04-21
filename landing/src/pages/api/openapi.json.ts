import type { APIRoute } from 'astro';
import { absolutePageUrl, absoluteUrl, resolveSiteUrl } from '../../utils/site';

export const prerender = true;

export const GET: APIRoute = ({ site }) => {
  const siteUrl = resolveSiteUrl(site);
  const serverUrl = siteUrl.toString().replace(/\/$/, '');

  const body = {
    openapi: '3.1.0',
    info: {
      title: 'BiciRadar Landing API',
      version: '1.0.0',
      description: 'Public landing-site endpoints used by the BiciRadar beta signup flow.',
    },
    servers: [{ url: serverUrl }],
    paths: {
      '/api/beta-signup': {
        post: {
          operationId: 'submitBetaSignup',
          summary: 'Submit a beta signup lead',
          description:
            'Accepts the landing-page waitlist form and returns either JSON or an HTTP redirect.',
          requestBody: {
            required: true,
            content: {
              'multipart/form-data': {
                schema: {
                  type: 'object',
                  additionalProperties: false,
                  required: ['email', 'operatingSystem', 'consent'],
                  properties: {
                    email: { type: 'string', format: 'email' },
                    operatingSystem: { type: 'string', enum: ['ios', 'android', 'both'] },
                    locale: { type: 'string', enum: ['es', 'en', 'ca', 'gl', 'eu'] },
                    pageKind: { type: 'string', enum: ['home', 'city'] },
                    pagePath: { type: 'string' },
                    cityPageKey: { type: 'string' },
                    startedAt: { type: 'string' },
                    company: { type: 'string' },
                    consent: { type: 'string', enum: ['on', 'true', '1', 'yes'] },
                    'cf-turnstile-response': { type: 'string' },
                    utm_source: { type: 'string' },
                    utm_medium: { type: 'string' },
                    utm_campaign: { type: 'string' },
                    utm_content: { type: 'string' },
                    utm_term: { type: 'string' },
                  },
                },
              },
            },
          },
          responses: {
            '200': {
              description: 'JSON success response when `Accept: application/json` is sent.',
              content: {
                'application/json': {
                  schema: {
                    type: 'object',
                    required: ['ok', 'redirectPath'],
                    properties: {
                      ok: { type: 'boolean', const: true },
                      redirectPath: { type: 'string' },
                      warning: { type: 'string' },
                    },
                  },
                },
              },
            },
            '303': {
              description: 'Browser redirect to the localized thank-you page.',
            },
            '400': {
              description: 'Validation or Turnstile failure for JSON clients.',
              content: {
                'application/json': {
                  schema: {
                    type: 'object',
                    required: ['ok', 'message'],
                    properties: {
                      ok: { type: 'boolean', const: false },
                      message: { type: 'string' },
                    },
                  },
                },
              },
            },
            '429': {
              description: 'Too many requests — Please try again.',
              content: {
                'application/json': {
                  schema: {
                    type: 'object',
                    required: ['ok', 'message'],
                    properties: {
                      ok: { type: 'boolean', const: false },
                      message: { type: 'string' },
                    },
                  },
                },
              },
            },
          },
        },
      },
    },
    externalDocs: {
      description: 'Human-readable service documentation',
      url: absolutePageUrl('/api/docs', siteUrl, import.meta.env.BASE_URL),
    },
    'x-links': [
      {
        rel: 'service-doc',
        href: absolutePageUrl('/api/docs', siteUrl, import.meta.env.BASE_URL),
      },
      {
        rel: 'describedby',
        href: absoluteUrl('/llms.txt', siteUrl, import.meta.env.BASE_URL),
      },
    ],
  };

  return Response.json(body, {
    headers: {
      'Content-Type': 'application/openapi+json; charset=utf-8',
    },
  });
};
