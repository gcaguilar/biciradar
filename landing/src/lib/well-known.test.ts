import { afterEach, describe, expect, it, vi } from 'vitest';
import {
  getAgentSkillsIndex,
  getApiCatalog,
  getMcpServerCard,
  hasMcpServerCardConfig,
  hasOAuthDiscoveryConfig,
  getOAuthDiscoveryMetadata,
} from './well-known';

afterEach(() => {
  vi.unstubAllEnvs();
});

describe('getApiCatalog', () => {
  it('publishes the public API, docs, and always-available agent metadata resources', () => {
    const body = getApiCatalog('https://biciradar.es', '/');
    const entry = body.linkset[0];

    expect(entry.anchor).toBe('https://biciradar.es/');
    expect(entry.item[0]?.href).toBe('https://biciradar.es/api/beta-signup');
    expect(entry['service-desc'][0]?.href).toBe('https://biciradar.es/api/openapi.json');
    expect(entry['service-doc'][0]?.href).toBe('https://biciradar.es/api/docs/');
    expect(entry.describedby[0]?.href).toBe('https://biciradar.es/llms.txt');
    expect(entry['service-meta'].map((item) => item.href)).toEqual([
      'https://biciradar.es/.well-known/agent-skills/index.json',
    ]);
  });

  it('publishes optional OAuth and MCP metadata only when explicitly configured', () => {
    vi.stubEnv('PUBLIC_OAUTH_ISSUER', 'https://auth.biciradar.es');
    vi.stubEnv('PUBLIC_OAUTH_AUTHORIZATION_ENDPOINT', 'https://auth.biciradar.es/authorize');
    vi.stubEnv('PUBLIC_OAUTH_TOKEN_ENDPOINT', 'https://auth.biciradar.es/token');
    vi.stubEnv('PUBLIC_OAUTH_JWKS_URI', 'https://auth.biciradar.es/jwks.json');
    vi.stubEnv('PUBLIC_MCP_SERVER_ENDPOINT', 'https://agents.biciradar.es/mcp');

    const body = getApiCatalog('https://biciradar.es', '/');
    const serviceMeta = body.linkset[0]['service-meta'].map((item) => item.href);

    expect(serviceMeta).toContain('https://biciradar.es/.well-known/oauth-protected-resource');
    expect(serviceMeta).toContain('https://biciradar.es/.well-known/oauth-authorization-server');
    expect(serviceMeta).toContain('https://biciradar.es/.well-known/openid-configuration');
    expect(serviceMeta).toContain('https://biciradar.es/.well-known/mcp/server-card.json');
  });
});

describe('getOAuthDiscoveryMetadata', () => {
  it('returns null when OAuth discovery is not configured', () => {
    const body = getOAuthDiscoveryMetadata('https://biciradar.es', '/');

    expect(body).toBeNull();
    expect(hasOAuthDiscoveryConfig()).toBe(false);
  });

  it('accepts deployment-specific endpoint overrides from env vars', () => {
    vi.stubEnv('PUBLIC_OAUTH_ISSUER', 'https://auth.biciradar.es');
    vi.stubEnv('PUBLIC_OAUTH_AUTHORIZATION_ENDPOINT', 'https://auth.biciradar.es/authorize');
    vi.stubEnv('PUBLIC_OAUTH_TOKEN_ENDPOINT', 'https://auth.biciradar.es/token');
    vi.stubEnv('PUBLIC_OAUTH_JWKS_URI', 'https://auth.biciradar.es/jwks.json');
    vi.stubEnv('PUBLIC_OAUTH_SCOPES_SUPPORTED', 'openid, profile, biciradar.read');

    const body = getOAuthDiscoveryMetadata('https://biciradar.es', '/');

    expect(body?.authorizationServerMetadata.issuer).toBe('https://auth.biciradar.es');
    expect(body?.authorizationServerMetadata.scopes_supported).toEqual([
      'openid',
      'profile',
      'biciradar.read',
    ]);
    expect(body?.protectedResourceMetadata.authorization_servers).toEqual([
      'https://auth.biciradar.es',
    ]);
    expect(hasOAuthDiscoveryConfig()).toBe(true);
  });
});

describe('getAgentSkillsIndex', () => {
  it('includes only the always-available skills by default', () => {
    const body = getAgentSkillsIndex('https://biciradar.es', '/');

    expect(body.skills.map((skill) => skill.id)).toEqual([
      'link-headers',
      'markdown-negotiation',
    ]);
  });

  it('adds optional skills only when the related capabilities are configured', () => {
    vi.stubEnv('PUBLIC_OAUTH_ISSUER', 'https://auth.biciradar.es');
    vi.stubEnv('PUBLIC_OAUTH_AUTHORIZATION_ENDPOINT', 'https://auth.biciradar.es/authorize');
    vi.stubEnv('PUBLIC_OAUTH_TOKEN_ENDPOINT', 'https://auth.biciradar.es/token');
    vi.stubEnv('PUBLIC_OAUTH_JWKS_URI', 'https://auth.biciradar.es/jwks.json');
    vi.stubEnv('PUBLIC_MCP_SERVER_ENDPOINT', 'https://agents.biciradar.es/mcp');

    const body = getAgentSkillsIndex('https://biciradar.es', '/');

    expect(body.skills.map((skill) => skill.id)).toEqual([
      'link-headers',
      'markdown-negotiation',
      'oauth-discovery',
      'mcp-server-card',
    ]);
  });
});

describe('getMcpServerCard', () => {
  it('returns null when no MCP endpoint is configured', () => {
    const body = getMcpServerCard('https://biciradar.es', '/');

    expect(body).toBeNull();
    expect(hasMcpServerCardConfig()).toBe(false);
  });

  it('publishes the server card with a configurable transport endpoint', () => {
    vi.stubEnv('PUBLIC_MCP_SERVER_ENDPOINT', 'https://agents.biciradar.es/mcp');

    const body = getMcpServerCard('https://biciradar.es', '/');

    expect(body?.serverInfo.name).toBe('biciradar');
    expect(body?.transport).toEqual({
      type: 'streamable-http',
      endpoint: 'https://agents.biciradar.es/mcp',
    });
    expect(hasMcpServerCardConfig()).toBe(true);
  });
});
