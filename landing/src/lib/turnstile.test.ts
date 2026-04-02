import { afterEach, describe, expect, it, vi } from 'vitest';
import { verifyTurnstileFromRequest } from './turnstile';

describe('verifyTurnstileFromRequest', () => {
  afterEach(() => {
    delete process.env.TURNSTILE_SECRET_KEY;
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  it('skips verification when TURNSTILE_SECRET_KEY is unset', async () => {
    const formData = new FormData();
    formData.set('cf-turnstile-response', '');
    const result = await verifyTurnstileFromRequest(formData, new Headers());
    expect(result).toEqual({ ok: true });
  });

  it('rejects empty token when secret is configured', async () => {
    process.env.TURNSTILE_SECRET_KEY = 'test_secret';
    const formData = new FormData();
    formData.set('locale', 'es');
    formData.set('cf-turnstile-response', '');
    const result = await verifyTurnstileFromRequest(formData, new Headers());
    expect(result.ok).toBe(false);
    if (!result.ok) {
      expect(result.status).toBe(400);
      expect(result.message).toContain('verificación');
    }
  });

  it('accepts when Cloudflare siteverify returns success', async () => {
    process.env.TURNSTILE_SECRET_KEY = 'test_secret';
    vi.stubGlobal(
      'fetch',
      vi.fn(async () =>
        Response.json({
          success: true,
        }),
      ),
    );

    const formData = new FormData();
    formData.set('locale', 'en');
    formData.set('cf-turnstile-response', 'valid-token');

    const result = await verifyTurnstileFromRequest(formData, new Headers());
    expect(result).toEqual({ ok: true });
    expect(fetch).toHaveBeenCalledWith(
      'https://challenges.cloudflare.com/turnstile/v0/siteverify',
      expect.objectContaining({ method: 'POST' }),
    );
  });

  it('rejects when Cloudflare siteverify returns failure', async () => {
    process.env.TURNSTILE_SECRET_KEY = 'test_secret';
    vi.stubGlobal(
      'fetch',
      vi.fn(async () =>
        Response.json({
          success: false,
          'error-codes': ['invalid-input-response'],
        }),
      ),
    );

    const formData = new FormData();
    formData.set('locale', 'en');
    formData.set('cf-turnstile-response', 'bad-token');

    const result = await verifyTurnstileFromRequest(formData, new Headers());
    expect(result.ok).toBe(false);
  });
});
