import packageJson from '../../package.json';
import { absolutePageUrl, absoluteUrl, resolveSiteUrl } from '../utils/site';

const DEFAULT_SCOPES = ['openid', 'profile', 'email'];
const DEFAULT_GRANT_TYPES = ['authorization_code', 'refresh_token'];
const DEFAULT_RESPONSE_TYPES = ['code'];
const DEFAULT_TOKEN_AUTH_METHODS = ['client_secret_basic'];
const DEFAULT_CODE_CHALLENGE_METHODS = ['S256'];
const DEFAULT_ID_TOKEN_ALGS = ['RS256'];
const DEFAULT_BEARER_METHODS = ['header'];

function trimTrailingSlash(value: string) {
  return value.replace(/\/$/, '');
}

function getEnv(name: string) {
  return process.env[name]?.trim() || '';
}

function getListEnv(name: string, fallback: string[]) {
  const value = getEnv(name);
  if (!value) {
    return fallback;
  }

  const items = value
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean);
  return items.length ? items : fallback;
}

export function getConfiguredOAuthDiscovery() {
  const issuer = trimTrailingSlash(getEnv('PUBLIC_OAUTH_ISSUER'));
  const authorizationEndpoint = getEnv('PUBLIC_OAUTH_AUTHORIZATION_ENDPOINT');
  const tokenEndpoint = getEnv('PUBLIC_OAUTH_TOKEN_ENDPOINT');
  const jwksUri = getEnv('PUBLIC_OAUTH_JWKS_URI');

  if (!issuer || !authorizationEndpoint || !tokenEndpoint || !jwksUri) {
    return null;
  }

  return {
    issuer,
    authorizationEndpoint,
    tokenEndpoint,
    jwksUri,
  };
}

export function hasOAuthDiscoveryConfig() {
  return getConfiguredOAuthDiscovery() !== null;
}

export function getConfiguredMcpServerEndpoint() {
  return getEnv('PUBLIC_MCP_SERVER_ENDPOINT');
}

export function hasMcpServerCardConfig() {
  return Boolean(getConfiguredMcpServerEndpoint());
}

export function getOAuthDiscoveryMetadata(site?: URL | string, basePath = '/') {
  const siteUrl = resolveSiteUrl(site);
  const configuredOAuth = getConfiguredOAuthDiscovery();

  if (!configuredOAuth) {
    return null;
  }

  const { issuer, authorizationEndpoint, tokenEndpoint, jwksUri } = configuredOAuth;
  const registrationEndpoint = getEnv('PUBLIC_OAUTH_REGISTRATION_ENDPOINT');
  const revocationEndpoint = getEnv('PUBLIC_OAUTH_REVOCATION_ENDPOINT');
  const introspectionEndpoint = getEnv('PUBLIC_OAUTH_INTROSPECTION_ENDPOINT');
  const deviceAuthorizationEndpoint = getEnv('PUBLIC_OAUTH_DEVICE_AUTHORIZATION_ENDPOINT');
  const userinfoEndpoint = getEnv('PUBLIC_OPENID_USERINFO_ENDPOINT');
  const endSessionEndpoint = getEnv('PUBLIC_OPENID_END_SESSION_ENDPOINT');
  const scopesSupported = getListEnv('PUBLIC_OAUTH_SCOPES_SUPPORTED', DEFAULT_SCOPES);
  const grantTypesSupported = getListEnv('PUBLIC_OAUTH_GRANT_TYPES_SUPPORTED', DEFAULT_GRANT_TYPES);
  const responseTypesSupported = getListEnv(
    'PUBLIC_OAUTH_RESPONSE_TYPES_SUPPORTED',
    DEFAULT_RESPONSE_TYPES,
  );
  const tokenEndpointAuthMethodsSupported = getListEnv(
    'PUBLIC_OAUTH_TOKEN_ENDPOINT_AUTH_METHODS_SUPPORTED',
    DEFAULT_TOKEN_AUTH_METHODS,
  );
  const codeChallengeMethodsSupported = getListEnv(
    'PUBLIC_OAUTH_CODE_CHALLENGE_METHODS_SUPPORTED',
    DEFAULT_CODE_CHALLENGE_METHODS,
  );
  const idTokenSigningAlgValuesSupported = getListEnv(
    'PUBLIC_OPENID_ID_TOKEN_SIGNING_ALG_VALUES_SUPPORTED',
    DEFAULT_ID_TOKEN_ALGS,
  );
  const protectedResource = absoluteUrl('/api', siteUrl, basePath);

  const authorizationServerMetadata = {
    issuer,
    authorization_endpoint: authorizationEndpoint,
    token_endpoint: tokenEndpoint,
    jwks_uri: jwksUri,
    grant_types_supported: grantTypesSupported,
    response_types_supported: responseTypesSupported,
    token_endpoint_auth_methods_supported: tokenEndpointAuthMethodsSupported,
    code_challenge_methods_supported: codeChallengeMethodsSupported,
    scopes_supported: scopesSupported,
    protected_resources: [protectedResource],
    ...(registrationEndpoint ? { registration_endpoint: registrationEndpoint } : {}),
    ...(revocationEndpoint ? { revocation_endpoint: revocationEndpoint } : {}),
    ...(introspectionEndpoint ? { introspection_endpoint: introspectionEndpoint } : {}),
    ...(deviceAuthorizationEndpoint
      ? { device_authorization_endpoint: deviceAuthorizationEndpoint }
      : {}),
  };

  const openIdConfiguration = {
    ...authorizationServerMetadata,
    userinfo_endpoint: userinfoEndpoint || absoluteUrl('/oauth/userinfo', siteUrl, basePath),
    subject_types_supported: ['public'],
    id_token_signing_alg_values_supported: idTokenSigningAlgValuesSupported,
    ...(endSessionEndpoint ? { end_session_endpoint: endSessionEndpoint } : {}),
  };

  const protectedResourceMetadata = {
    resource: protectedResource,
    authorization_servers: [issuer],
    scopes_supported: scopesSupported,
    bearer_methods_supported: DEFAULT_BEARER_METHODS,
    resource_name: 'BiciRadar Public API',
    resource_documentation: absolutePageUrl('/api/docs', siteUrl, basePath),
    jwks_uri: jwksUri,
  };

  return {
    authorizationServerMetadata,
    openIdConfiguration,
    protectedResourceMetadata,
  };
}

export function getApiCatalog(site?: URL | string, basePath = '/') {
  const siteUrl = resolveSiteUrl(site);
  const homeUrl = absolutePageUrl('/', siteUrl, basePath);
  const betaSignupUrl = absoluteUrl('/api/beta-signup', siteUrl, basePath);
  const serviceMeta = [
    {
      href: absoluteUrl('/.well-known/agent-skills/index.json', siteUrl, basePath),
      type: 'application/json',
    },
  ];

  if (hasOAuthDiscoveryConfig()) {
    serviceMeta.push(
      {
        href: absoluteUrl('/.well-known/oauth-protected-resource', siteUrl, basePath),
        type: 'application/json',
      },
      {
        href: absoluteUrl('/.well-known/oauth-authorization-server', siteUrl, basePath),
        type: 'application/json',
      },
      {
        href: absoluteUrl('/.well-known/openid-configuration', siteUrl, basePath),
        type: 'application/json',
      },
    );
  }

  if (hasMcpServerCardConfig()) {
    serviceMeta.push({
      href: absoluteUrl('/.well-known/mcp/server-card.json', siteUrl, basePath),
      type: 'application/json',
    });
  }

  return {
    linkset: [
      {
        anchor: homeUrl,
        item: [
          {
            href: betaSignupUrl,
            type: 'multipart/form-data',
            title: 'BiciRadar beta signup API',
          },
        ],
        'service-desc': [
          {
            href: absoluteUrl('/api/openapi.json', siteUrl, basePath),
            type: 'application/openapi+json',
          },
        ],
        'service-doc': [
          {
            href: absolutePageUrl('/api/docs', siteUrl, basePath),
            type: 'text/html',
          },
        ],
        describedby: [
          {
            href: absoluteUrl('/llms.txt', siteUrl, basePath),
            type: 'text/plain',
          },
        ],
        'service-meta': serviceMeta,
      },
    ],
  };
}

type AgentSkill = {
  id: string;
  title: string;
  href: string;
  output?: {
    header: string;
    relations: string[];
  };
  accepts?: string[];
  contentType?: string;
  openidConfiguration?: string;
  protectedResource?: string;
};

export function getAgentSkillsIndex(site?: URL | string, basePath = '/') {
  const siteUrl = resolveSiteUrl(site);
  const rootUrl = absolutePageUrl('/', siteUrl, basePath);
  const skills: AgentSkill[] = [
    {
      id: 'link-headers',
      title: 'Homepage Link headers for agent discovery',
      href: rootUrl,
      output: {
        header: 'Link',
        relations: ['api-catalog', 'service-desc', 'service-doc', 'describedby', 'service-meta'],
      },
    },
    {
      id: 'markdown-negotiation',
      title: 'Markdown representation for HTML pages',
      href: rootUrl,
      accepts: ['text/markdown'],
      contentType: 'text/markdown; charset=utf-8',
    },
  ];

  if (hasOAuthDiscoveryConfig()) {
    skills.push({
      id: 'oauth-discovery',
      title: 'OAuth and OpenID Connect discovery metadata',
      href: absoluteUrl('/.well-known/oauth-authorization-server', siteUrl, basePath),
      openidConfiguration: absoluteUrl('/.well-known/openid-configuration', siteUrl, basePath),
      protectedResource: absoluteUrl('/.well-known/oauth-protected-resource', siteUrl, basePath),
    });
  }

  if (hasMcpServerCardConfig()) {
    skills.push({
      id: 'mcp-server-card',
      title: 'MCP server discovery card',
      href: absoluteUrl('/.well-known/mcp/server-card.json', siteUrl, basePath),
    });
  }

  return {
    version: '1.0',
    skills,
  };
}

export function getMcpServerCard(site?: URL | string, basePath = '/') {
  const siteUrl = resolveSiteUrl(site);
  const endpoint = getConfiguredMcpServerEndpoint();

  if (!endpoint) {
    return null;
  }

  return {
    $schema: 'https://modelcontextprotocol.io/schemas/server-card/v1.0',
    version: '1.0',
    protocolVersion: '2025-06-18',
    serverInfo: {
      name: 'biciradar',
      title: 'BiciRadar MCP',
      version: packageJson.version,
    },
    description:
      'Discovery metadata for the BiciRadar MCP surface, including transport endpoint and declared server capabilities.',
    documentationUrl: absolutePageUrl('/api/docs', siteUrl, basePath),
    transport: {
      type: 'streamable-http',
      endpoint,
    },
    capabilities: {
      tools: {},
      resources: {},
      prompts: {},
    },
  };
}
