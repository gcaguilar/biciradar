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
    id: 'lead-1',
    createdAt: '2026-04-02T12:00:00.000Z',
    locale,
    email: 'user@example.com',
    operatingSystem,
    city: 'Barcelona',
    bikeSystem: 'Bicing',
    frequency: 'daily',
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

  it('sends a single Android email with Google group and Play Store links', async () => {
    const resendModule = await import('resend');
    const resendSend = (resendModule as any).__resendSend as ReturnType<typeof vi.fn>;

    await sendBetaSignupEmails(buildRecord('android'));

    expect(resendSend).toHaveBeenCalledTimes(1);
    expect(resendSend).toHaveBeenCalledWith(
      expect.objectContaining({
        to: ['user@example.com'],
        subject: expect.stringContaining('testers group'),
        text: expect.stringContaining('groups.google.com/g/testers-biciradar'),
      }),
    );
    const payload = resendSend.mock.calls[0][0] as { html: string; headers: Record<string, string> };
    expect(payload.html).toContain('lang="en"');
    expect(payload.html).toContain('groups.google.com/g/testers-biciradar');
    expect(payload.html).toContain('play.google.com');
    expect(payload.headers['Content-Language']).toBe('en');
  });

  it('sends a single email for Android+iOS with group, App Store, and Play Store links', async () => {
    const resendModule = await import('resend');
    const resendSend = (resendModule as any).__resendSend as ReturnType<typeof vi.fn>;

    await sendBetaSignupEmails(buildRecord('both'));

    expect(resendSend).toHaveBeenCalledTimes(1);
    const payload = resendSend.mock.calls[0][0] as { html: string; subject: string };
    expect(payload.html).toContain('groups.google.com/g/testers-biciradar');
    expect(payload.html).toContain('apps.apple.com');
    expect(payload.html).toContain('play.google.com');
  });

  it('uses the form locale for copy and headers (e.g. Catalan for iOS)', async () => {
    const resendModule = await import('resend');
    const resendSend = (resendModule as any).__resendSend as ReturnType<typeof vi.fn>;

    await sendBetaSignupEmails(buildRecord('ios', 'ca'));

    const payload = resendSend.mock.calls[0][0] as { subject: string; html: string; headers: Record<string, string> };
    expect(payload.subject).toBe('Ja pots descarregar BiciRadar a l’App Store');
    expect(payload.html).toContain('lang="ca"');
    expect(payload.headers['Content-Language']).toBe('ca');
  });

  it('uses locale-matched store links (e.g. US App Store and hl=en for English)', async () => {
    const resendModule = await import('resend');
    const resendSend = (resendModule as any).__resendSend as ReturnType<typeof vi.fn>;

    await sendBetaSignupEmails(buildRecord('both', 'en'));

    const payload = resendSend.mock.calls[0][0] as { html: string };
    expect(payload.html).toContain('apps.apple.com/us/app/biciradar/');
    expect(payload.html).toContain('hl=en');
  });
});
