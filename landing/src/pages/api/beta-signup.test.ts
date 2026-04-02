import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { POST } from './beta-signup';

vi.mock('resend', () => {
  const send = vi.fn(async () => ({ data: { id: 'fake-email-id' } }));
  class Resend {
    emails = {
      send,
    };
  }

  return { Resend, __resendSend: send };
});

vi.mock('../../lib/beta-form', async () => {
  const actual = await vi.importActual<typeof import('../../lib/beta-form')>('../../lib/beta-form');
  return {
    ...actual,
    storeBetaLead: vi.fn(async () => undefined),
  };
});

function buildRequest(operatingSystem: 'android' | 'both') {
  const formData = new FormData();
  formData.set('email', 'user@example.com');
  formData.set('operatingSystem', operatingSystem);
  formData.set('city', 'Barcelona');
  formData.set('bikeSystem', 'Bicing');
  formData.set('frequency', 'daily');
  formData.set('consent', 'true');
  formData.set('locale', 'en');
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
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-04-02T12:00:00.000Z'));
    process.env.RESEND_API_KEY = 'resend_test_key';
    process.env.RESEND_FROM = 'BiciRadar <team@biciradar.app>';
    process.env.BETA_NOTIFY_TO = 'ops@biciradar.app';
    process.env.BETA_NOTIFY_SECRET = 'landing-shared-secret';
  });

  afterEach(() => {
    vi.useRealTimers();
    vi.clearAllMocks();
    delete process.env.RESEND_API_KEY;
    delete process.env.RESEND_FROM;
    delete process.env.BETA_NOTIFY_TO;
    delete process.env.BETA_NOTIFY_SECRET;
  });

  it('calls Resend with Android content when user selects Android', async () => {
    const resendModule = await import('resend');
    const resendSend = (resendModule as any).__resendSend as ReturnType<typeof vi.fn>;

    const response = await POST({ request: buildRequest('android') } as any);
    const body = await response.json();

    expect(response.status).toBe(200);
    expect(body.ok).toBe(true);
    expect(body.redirectPath).toContain('os=android');
    expect(resendSend).toHaveBeenCalledTimes(2);
    expect(resendSend).toHaveBeenNthCalledWith(
      1,
      expect.objectContaining({
        subject: expect.stringContaining('Android beta access'),
        html: expect.stringContaining('Open Google Play'),
        scheduledAt: '2026-04-02T12:02:00.000Z',
      }),
    );
    expect(resendSend).toHaveBeenNthCalledWith(
      2,
      expect.objectContaining({
        text: expect.stringContaining('"secret": "landing-shared-secret"'),
      }),
    );
  });

  it('calls Resend with Android+iOS content when user selects both platforms', async () => {
    const resendModule = await import('resend');
    const resendSend = (resendModule as any).__resendSend as ReturnType<typeof vi.fn>;

    const response = await POST({ request: buildRequest('both') } as any);
    const body = await response.json();

    expect(response.status).toBe(200);
    expect(body.ok).toBe(true);
    expect(body.redirectPath).toContain('os=both');
    expect(resendSend).toHaveBeenCalledTimes(2);
    expect(resendSend).toHaveBeenNthCalledWith(
      1,
      expect.objectContaining({
        subject: expect.stringContaining('access is already in motion'),
        html: expect.stringContaining('Open App Store'),
      }),
    );
    expect(resendSend).toHaveBeenNthCalledWith(
      1,
      expect.objectContaining({
        html: expect.stringContaining('Open Google Play'),
      }),
    );
    expect(resendSend).toHaveBeenNthCalledWith(
      2,
      expect.objectContaining({
        text: expect.stringContaining('"secret": "landing-shared-secret"'),
      }),
    );
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
