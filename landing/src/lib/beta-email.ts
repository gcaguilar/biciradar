import { Resend } from 'resend';
import type { BetaLeadRecord } from './beta-form';
import { bikeSystemLabelForCity } from './beta-city-system';
import { localeToHtmlLang, normalizeLocale } from './i18n';

function getResendClient() {
  const apiKey = process.env.RESEND_API_KEY?.trim();
  if (!apiKey) {
    return null;
  }

  return new Resend(apiKey);
}

function getFromAddress() {
  return process.env.RESEND_FROM?.trim() || '';
}

function getNotifyTo() {
  return process.env.RESEND_BETA_NOTIFY_TO?.trim() || '';
}

function escapeHtml(value: string) {
  return value
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}

const OS_LABEL: Record<BetaLeadRecord['operatingSystem'], string> = {
  ios: 'iOS',
  android: 'Android',
  both: 'iOS + Android',
};

function buildTeamNotification(record: BetaLeadRecord) {
  const system = record.city ? bikeSystemLabelForCity(record.city) : null;
  const os = OS_LABEL[record.operatingSystem];
  const locale = normalizeLocale(record.locale);
  const subject = system
    ? `BiciRadar beta · ${record.email} · ${system} · ${os}`
    : `BiciRadar beta · ${record.email} · ${os}`;

  const lines: string[] = [
    `Email: ${record.email}`,
    ...(system ? [`Sistema de bici: ${system}`] : ['Sistema de bici: (sin ciudad en página)']),
    `Plataforma: ${os}`,
    `Idioma formulario: ${locale}`,
    `Consentimiento: sí`,
  ];

  if (record.pagePath) {
    lines.push(`Página: ${record.pagePath}`);
  }
  if (record.referrer) {
    lines.push(`Referrer: ${record.referrer}`);
  }
  if (record.userAgent) {
    lines.push(`User-Agent: ${record.userAgent}`);
  }
  if (record.ip) {
    lines.push(`IP: ${record.ip}`);
  }
  if (Object.keys(record.utm).length > 0) {
    lines.push(`UTM: ${JSON.stringify(record.utm)}`);
  }

  const text = lines.join('\n');
  const html = `<pre style="font-family:system-ui,sans-serif;font-size:14px;line-height:1.5">${escapeHtml(text)}</pre>`;

  return { subject, html, text };
}

export type SendBetaSignupEmailResult =
  | { ok: true; skipped: true }
  | { ok: true; skipped?: false }
  | { ok: false; reason: string };

/** When `RESEND_API_KEY` is unset, skips send and returns ok (local/dev). Otherwise requires from, to, and a successful Resend response (`error` is checked — the SDK does not throw). */
export async function sendBetaSignupEmails(record: BetaLeadRecord): Promise<SendBetaSignupEmailResult> {
  const apiKeyConfigured = Boolean(process.env.RESEND_API_KEY?.trim());
  const resend = getResendClient();
  const from = getFromAddress();
  const to = getNotifyTo();

  if (!apiKeyConfigured) {
    return { ok: true, skipped: true };
  }

  if (!resend || !from) {
    const reason = 'Email notify misconfigured: RESEND_FROM is missing or invalid.';
    console.error(reason);
    return { ok: false, reason };
  }

  if (!to) {
    const reason = 'Email notify misconfigured: RESEND_BETA_NOTIFY_TO is missing.';
    console.error(reason);
    return { ok: false, reason };
  }

  const { subject, html, text } = buildTeamNotification(record);
  const locale = normalizeLocale(record.locale);

  try {
    const { error } = await resend.emails.send({
      from,
      to: [to],
      subject,
      html,
      text,
      headers: {
        'Content-Language': localeToHtmlLang[locale],
      },
    });

    if (error) {
      console.error('Resend rejected beta notify email', error);
      return { ok: false, reason: error.message || 'Email provider rejected the message.' };
    }

    return { ok: true };
  } catch (error) {
    console.error('Failed to send beta notify email', error);
    return {
      ok: false,
      reason: error instanceof Error ? error.message : 'Unexpected error while sending email.',
    };
  }
}
