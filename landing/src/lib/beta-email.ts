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

export async function sendBetaSignupEmails(record: BetaLeadRecord) {
  const resend = getResendClient();
  const from = getFromAddress();
  const to = getNotifyTo();

  if (!resend || !from) {
    console.error('Beta notify email skipped because RESEND_API_KEY or RESEND_FROM is missing.');
    return;
  }

  if (!to) {
    console.error('Beta notify email skipped because RESEND_BETA_NOTIFY_TO is missing.');
    return;
  }

  const { subject, html, text } = buildTeamNotification(record);
  const locale = normalizeLocale(record.locale);

  try {
    await resend.emails.send({
      from,
      to: [to],
      subject,
      html,
      text,
      headers: {
        'Content-Language': localeToHtmlLang[locale],
      },
    });
  } catch (error) {
    console.error('Failed to send beta notify email', error);
  }
}
