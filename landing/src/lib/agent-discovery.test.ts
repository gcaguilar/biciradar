import { describe, expect, it } from 'vitest';
import { getAgentDiscoveryLinkValues, isHomepagePath } from './agent-discovery';

describe('isHomepagePath', () => {
  it('matches root and localized homepage routes at site root', () => {
    expect(isHomepagePath('/')).toBe(true);
    expect(isHomepagePath('/en')).toBe(true);
    expect(isHomepagePath('/en/')).toBe(true);
    expect(isHomepagePath('/es')).toBe(false);
    expect(isHomepagePath('/madrid-bicimad')).toBe(false);
  });

  it('matches root and localized homepage routes under a base path', () => {
    expect(isHomepagePath('/biciradar', '/biciradar')).toBe(true);
    expect(isHomepagePath('/biciradar/', '/biciradar')).toBe(true);
    expect(isHomepagePath('/biciradar/en', '/biciradar')).toBe(true);
    expect(isHomepagePath('/biciradar/en/', '/biciradar')).toBe(true);
    expect(isHomepagePath('/biciradar/es', '/biciradar')).toBe(false);
    expect(isHomepagePath('/en', '/biciradar')).toBe(false);
  });
});

describe('getAgentDiscoveryLinkValues', () => {
  it('prefixes discovery targets with the configured base path', () => {
    expect(getAgentDiscoveryLinkValues('/biciradar')).toContain(
      '</biciradar/.well-known/api-catalog>; rel="api-catalog"; type="application/linkset+json"',
    );
  });
});
