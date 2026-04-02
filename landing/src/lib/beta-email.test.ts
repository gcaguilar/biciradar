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
    process.env.RESEND_BETA_NOTIFY_TO = 'ops@biciradar.app';
  });

  afterEach(() => {
    vi.clearAllMocks();
    delete process.env.RESEND_API_KEY;
    delete process.env.RESEND_FROM;
    delete process.env.RESEND_BETA_NOTIFY_TO;
  });

  it('sends a team notification with system and platform in subject', async () => {
    const resendModule = await import('resend');
    const resendSend = (resendModule as any).__resendSend as ReturnType<typeof vi.fn>;

    await sendBetaSignupEmails(buildRecord('android'));

    expect(resendSend).toHaveBeenCalledTimes(1);
    expect(resendSend).toHaveBeenCalledWith(
      expect.objectContaining({
        to: ['ops@biciradar.app'],
        subject: 'BiciRadar beta · user@example.com · BiciMAD · Android',
        text: expect.stringContaining('user@example.com'),
      }),
    );
    const payload = resendSend.mock.calls[0][0] as { html: string; headers: Record<string, string> };
    expect(payload.html).toContain('BiciMAD');
    expect(payload.headers['Content-Language']).toBe('en');
  });

  it('uses the form locale for Content-Language (e.g. Catalan)', async () => {
    const resendModule = await import('resend');
    const resendSend = (resendModule as any).__resendSend as ReturnType<typeof vi.fn>;

    await sendBetaSignupEmails(buildRecord('ios', 'ca'));

    const payload = resendSend.mock.calls[0][0] as { headers: Record<string, string> };
    expect(payload.headers['Content-Language']).toBe('ca');
  });
});
