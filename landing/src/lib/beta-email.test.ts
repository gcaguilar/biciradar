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

function buildRecord(operatingSystem: BetaLeadRecord['operatingSystem']): BetaLeadRecord {
  return {
    id: 'lead-1',
    createdAt: '2026-04-02T12:00:00.000Z',
    locale: 'en',
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

  it('sends Android user email delayed and includes Play Store content', async () => {
    const resendModule = await import('resend');
    const resendSend = (resendModule as any).__resendSend as ReturnType<typeof vi.fn>;

    await sendBetaSignupEmails(buildRecord('android'));

    expect(resendSend).toHaveBeenCalledTimes(2);
    expect(resendSend).toHaveBeenNthCalledWith(
      1,
      expect.objectContaining({
        to: ['user@example.com'],
        subject: expect.stringContaining('Android beta access'),
        scheduledAt: '2026-04-02T12:02:00.000Z',
        html: expect.stringContaining('Open Google Play'),
        text: expect.stringContaining('Open Google Play'),
      }),
    );
    expect(resendSend).toHaveBeenNthCalledWith(
      2,
      expect.objectContaining({
        text: expect.stringContaining('"secret": "landing-shared-secret"'),
      }),
    );
  });

  it('sends Android+iOS user email with both store links and delayed schedule', async () => {
    const resendModule = await import('resend');
    const resendSend = (resendModule as any).__resendSend as ReturnType<typeof vi.fn>;

    await sendBetaSignupEmails(buildRecord('both'));

    expect(resendSend).toHaveBeenCalledTimes(2);
    expect(resendSend).toHaveBeenNthCalledWith(
      1,
      expect.objectContaining({
        to: ['user@example.com'],
        scheduledAt: '2026-04-02T12:02:00.000Z',
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
});
