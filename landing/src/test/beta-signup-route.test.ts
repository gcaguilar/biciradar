import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { POST } from '../pages/api/beta-signup';

vi.mock('resend', () => {
  const send = vi.fn(async () => ({ data: { id: 'fake-email-id' } }));
  class Resend {
    emails = {
      send,
    };
  }

  return { Resend, __resendSend: send };
});

vi.mock('../lib/beta-form', async () => {
  const actual = await vi.importActual<typeof import('../lib/beta-form')>('../lib/beta-form');
  return {
    ...actual,
    storeBetaLead: vi.fn(async () => undefined),
  };
});

function buildRequest(operatingSystem: 'android' | 'both', locale = 'en') {
  const formData = new FormData();
  formData.set('email', 'user@example.com');
  formData.set('operatingSystem', operatingSystem);
  formData.set('city', 'Barcelona');
  formData.set('bikeSystem', 'Bicing');
  formData.set('frequency', 'daily');
  formData.set('consent', 'true');
  formData.set('locale', locale);
  formData.set('startedAt', String(Date.now() - 10_000));
  formData.set('pagePath', '/beta');

  return new Request('https://biciradar.app/api/beta-signup', {
    method: 'POST',
    headers: {
      accept: 'application/json',
    },
    body: formData,
  });
}

describe('POST /api/beta-signup', () => {
  beforeEach(() => {
    process.env.RESEND_API_KEY = 'resend_test_key';
    process.env.RESEND_FROM = 'BiciRadar <team@biciradar.app>';
  });

  afterEach(() => {
    vi.clearAllMocks();
    delete process.env.RESEND_API_KEY;
    delete process.env.RESEND_FROM;
  });

  it('calls Resend with Android content when user selects Android', async () => {
    const resendModule = await import('resend');
    const resendSend = (resendModule as any).__resendSend as ReturnType<typeof vi.fn>;

    const response = await POST({ request: buildRequest('android') } as any);
    const body = await response.json();

    expect(response.status).toBe(200);
    expect(body.ok).toBe(true);
    expect(body.redirectPath).toContain('os=android');
    expect(resendSend).toHaveBeenCalledTimes(1);
    const payload = resendSend.mock.calls[0][0] as { html: string; subject: string };
    expect(payload.subject).toMatch(/testers group/i);
    expect(payload.html).toContain('groups.google.com/g/testers-biciradar');
    expect(payload.html).toContain('play.google.com');
  });

  it('calls Resend with Android+iOS content when user selects both platforms', async () => {
    const resendModule = await import('resend');
    const resendSend = (resendModule as any).__resendSend as ReturnType<typeof vi.fn>;

    const response = await POST({ request: buildRequest('both') } as any);
    const body = await response.json();

    expect(response.status).toBe(200);
    expect(body.ok).toBe(true);
    expect(body.redirectPath).toContain('os=both');
    expect(resendSend).toHaveBeenCalledTimes(1);
    const payload = resendSend.mock.calls[0][0] as { html: string; subject: string };
    expect(payload.subject).toMatch(/testers group|installing BiciRadar/i);
    expect(payload.html).toContain('groups.google.com/g/testers-biciradar');
    expect(payload.html).toContain('apps.apple.com');
    expect(payload.html).toContain('play.google.com');
  });

  it('sends email copy in the locale submitted with the form', async () => {
    const resendModule = await import('resend');
    const resendSend = (resendModule as any).__resendSend as ReturnType<typeof vi.fn>;

    const response = await POST({ request: buildRequest('android', 'ca') } as any);
    expect(response.status).toBe(200);

    const payload = resendSend.mock.calls[0][0] as { subject: string; headers: Record<string, string> };
    expect(payload.subject).toBe('Pas següent: uneix-te al grup de testers de BiciRadar');
    expect(payload.headers['Content-Language']).toBe('ca');
  });

  it('rejects invalid emails and does not call Resend', async () => {
    const resendModule = await import('resend');
    const resendSend = (resendModule as any).__resendSend as ReturnType<typeof vi.fn>;
    const request = buildRequest('android');
    const invalidFormData = await request.formData();
    invalidFormData.set('email', 'not-an-email');

    const response = await POST({
      request: new Request('https://biciradar.app/api/beta-signup', {
        method: 'POST',
        headers: { accept: 'application/json' },
        body: invalidFormData,
      }),
    } as any);
    const body = await response.json();

    expect(response.status).toBe(400);
    expect(body.ok).toBe(false);
    expect(body.message).toBe('Invalid email.');
    expect(resendSend).not.toHaveBeenCalled();
  });
});
