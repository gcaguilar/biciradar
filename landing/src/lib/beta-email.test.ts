import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { BetaLeadRecord } from './beta-form';
import { sendBetaSignupEmails } from './beta-email';

vi.mock('resend', () => {
  const send = vi.fn(async () => ({ data: { id: 'fake-email-id' } }));
  class Resend {
    emails = {
      send,
    };
  }

  return { Resend, __resendSend: send };
});

function buildRecord(
  operatingSystem: BetaLeadRecord['operatingSystem'],
  locale: BetaLeadRecord['locale'] = 'en',
): BetaLeadRecord {
  return {
    locale,
    email: 'user@example.com',
    operatingSystem,
    city: 'madrid',
    consent: true,
    utm: {},
  };
}

describe('sendBetaSignupEmails', () => {
  beforeEach(() => {
    process.env.RESEND_API_KEY = 'resend_test_key';
    process.env.RESEND_FROM = 'BiciRadar <team@biciradar.app>';
  });

  afterEach(() => {
    vi.clearAllMocks();
    delete process.env.RESEND_API_KEY;
    delete process.env.RESEND_FROM;
  });

  it('sends a user confirmation email with app links', async () => {
    const resendModule = await import('resend');
    const resendSend = (resendModule as any).__resendSend as ReturnType<typeof vi.fn>;

    const outcome = await sendBetaSignupEmails(buildRecord('android'));
    expect(outcome.ok).toBe(true);

    expect(resendSend).toHaveBeenCalledTimes(1);
    expect(resendSend).toHaveBeenCalledWith(
      expect.objectContaining({
        to: ['user@example.com'],
        subject: 'Your BiciRadar download links',
      }),
    );
    const payload = resendSend.mock.calls[0][0] as { text: string; headers: Record<string, string> };
    expect(payload.text).toContain('Google Play');
    expect(payload.text).not.toContain('groups.google.com/g/testers-biciradar');
    expect(payload.headers['Content-Language']).toBe('en');
  });

  it('uses the form locale for Content-Language (e.g. Catalan)', async () => {
    const resendModule = await import('resend');
    const resendSend = (resendModule as any).__resendSend as ReturnType<typeof vi.fn>;

    const outcome = await sendBetaSignupEmails(buildRecord('ios', 'ca'));
    expect(outcome.ok).toBe(true);

    const payload = resendSend.mock.calls[0][0] as { headers: Record<string, string> };
    expect(payload.headers['Content-Language']).toBe('ca');
  });

  it('returns ok: false when Resend responds with error (SDK does not throw)', async () => {
    const resendModule = await import('resend');
    const resendSend = (resendModule as any).__resendSend as ReturnType<typeof vi.fn>;
    resendSend.mockResolvedValueOnce({
      data: null,
      error: { message: 'Domain not verified', name: 'validation_error', statusCode: 403 },
    });

    const outcome = await sendBetaSignupEmails(buildRecord('both'));

    expect(outcome.ok).toBe(false);
    if (!outcome.ok) {
      expect(outcome.reason).toContain('Domain not verified');
    }
  });
});
