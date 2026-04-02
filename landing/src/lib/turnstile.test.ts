import { afterEach, describe, expect, it, vi } from 'vitest';
import { verifyTurnstileFromRequest } from './turnstile';

describe('verifyTurnstileFromRequest', () => {
  afterEach(() => {
    delete process.env.TURNSTILE_SECRET_KEY;
    delete process.env.TURNSTILE_SUBMIT_REMOTE_IP;
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

  it('does not send remoteip by default even when X-Forwarded-For is set', async () => {
    process.env.TURNSTILE_SECRET_KEY = 'test_secret';
    delete process.env.TURNSTILE_SUBMIT_REMOTE_IP;
    const fetchMock = vi.fn(async () => Response.json({ success: true }));
    vi.stubGlobal('fetch', fetchMock);

    const formData = new FormData();
    formData.set('locale', 'en');
    formData.set('cf-turnstile-response', 'valid-token');
    const headers = new Headers({ 'x-forwarded-for': '203.0.113.10, 10.0.0.1' });

    await verifyTurnstileFromRequest(formData, headers);

    const init = fetchMock.mock.calls[0][1] as RequestInit;
    const body = init.body as URLSearchParams;
    expect(body.get('remoteip')).toBeNull();
  });

  it('sends remoteip when TURNSTILE_SUBMIT_REMOTE_IP is true', async () => {
    process.env.TURNSTILE_SECRET_KEY = 'test_secret';
    process.env.TURNSTILE_SUBMIT_REMOTE_IP = 'true';
    const fetchMock = vi.fn(async () => Response.json({ success: true }));
    vi.stubGlobal('fetch', fetchMock);

    const formData = new FormData();
    formData.set('locale', 'en');
    formData.set('cf-turnstile-response', 'valid-token');
    const headers = new Headers({ 'x-forwarded-for': '203.0.113.10' });

    await verifyTurnstileFromRequest(formData, headers);

    const init = fetchMock.mock.calls[0][1] as RequestInit;
    const body = init.body as URLSearchParams;
    expect(body.get('remoteip')).toBe('203.0.113.10');
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
