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
    forwardBetaLead: vi.fn(async () => undefined),
  };
});

function buildRequest(operatingSystem: 'android' | 'both' | 'ios', locale = 'en') {
  const formData = new FormData();
  formData.set('email', 'user@example.com');
  formData.set('operatingSystem', operatingSystem);
  formData.set('cityPageKey', 'barcelona');
  formData.set('pageKind', 'city');
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
    process.env.RESEND_BETA_NOTIFY_TO = 'ops@biciradar.app';
  });

  afterEach(() => {
    vi.clearAllMocks();
    delete process.env.RESEND_API_KEY;
    delete process.env.RESEND_FROM;
    delete process.env.RESEND_BETA_NOTIFY_TO;
  });

  it('returns success and sends team notification email', async () => {
    const resendModule = await import('resend');
    const resendSend = (resendModule as any).__resendSend as ReturnType<typeof vi.fn>;

    const response = await POST({ request: buildRequest('android') } as any);
    const body = await response.json();

    expect(response.status).toBe(200);
    expect(body.ok).toBe(true);
    expect(body.redirectPath).toMatch(/beta-thanks/);
    expect(resendSend).toHaveBeenCalledTimes(1);
    const payload = resendSend.mock.calls[0][0] as { to: string[]; subject: string };
    expect(payload.to).toEqual(['ops@biciradar.app']);
    expect(payload.subject).toContain('user@example.com');
    expect(payload.subject).toContain('Bicing');
  });

  it('accepts signup from home without cityPageKey and omits system from subject', async () => {
    const resendModule = await import('resend');
    const resendSend = (resendModule as any).__resendSend as ReturnType<typeof vi.fn>;
    const formData = new FormData();
    formData.set('email', 'home@example.com');
    formData.set('operatingSystem', 'ios');
    formData.set('cityPageKey', '');
    formData.set('pageKind', 'home');
    formData.set('consent', 'true');
    formData.set('locale', 'en');
    formData.set('startedAt', String(Date.now() - 10_000));
    formData.set('pagePath', '/');

    const response = await POST({
      request: new Request('https://biciradar.app/api/beta-signup', {
        method: 'POST',
        headers: { accept: 'application/json' },
        body: formData,
      }),
    } as any);
    const body = await response.json();

    expect(response.status).toBe(200);
    expect(body.ok).toBe(true);
    const payload = resendSend.mock.calls[0][0] as { subject: string };
    expect(payload.subject).toBe('BiciRadar beta · home@example.com · iOS');
    expect(payload.subject).not.toContain('BiciMAD');
  });
});
