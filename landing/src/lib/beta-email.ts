import { Resend } from 'resend';
import type { BetaLeadRecord } from './beta-form';
import { bikeSystemLabelForCity } from './beta-city-system';
import { localeToHtmlLang, normalizeLocale } from './i18n';
import { appStoreUrlForLocale, playStoreUrlForLocale, siteConfig } from './site-config';

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

function buildUserConfirmation(record: BetaLeadRecord) {
  const locale = normalizeLocale(record.locale);
  const appStoreUrl = appStoreUrlForLocale(locale);
  const playStoreUrl = playStoreUrlForLocale(locale);
  const supportEmail = siteConfig.contactEmail;
  const system = record.city ? bikeSystemLabelForCity(record.city) : null;
  const os = OS_LABEL[record.operatingSystem];
  const isEnglish = locale === 'en';
  const subject = isEnglish ? 'Your BiciRadar download links' : 'Te enviamos los enlaces de BiciRadar';
  const intro = isEnglish
    ? 'Thanks for leaving your email. Here are your BiciRadar download links.'
    : 'Gracias por dejarnos tu correo. Aquí tienes los enlaces para descargar BiciRadar.';
  const osMessage =
    record.operatingSystem === 'ios'
      ? isEnglish
        ? `For iPhone, you can already download the app here: ${appStoreUrl}`
        : `Si usas iPhone, ya puedes descargar la app desde aquí: ${appStoreUrl}`
      : record.operatingSystem === 'android'
        ? isEnglish
          ? `For Android, you can already download the app from Google Play: ${playStoreUrl}`
          : `Si usas Android, ya puedes descargar la app desde Google Play: ${playStoreUrl}`
        : isEnglish
          ? `For iPhone, use the App Store (${appStoreUrl}). For Android, use Google Play (${playStoreUrl}).`
          : `Si usas ambos, en iPhone usa App Store (${appStoreUrl}) y en Android usa Google Play (${playStoreUrl}).`;
  const details = [
    ...(system
      ? [isEnglish ? `Detected bike system page: ${system}` : `Sistema detectado de tu página: ${system}`]
      : []),
    isEnglish ? `Selected platform: ${os}` : `Plataforma seleccionada: ${os}`,
  ];
  const closing = isEnglish
    ? `If you have any questions, write to ${supportEmail}.`
    : `Si tienes cualquier duda, escríbenos a ${supportEmail}.`;
  const text = [intro, '', osMessage, ...details, '', closing].join('\n');
  const html = `<div style="font-family:system-ui,sans-serif;font-size:14px;line-height:1.6"><p>${escapeHtml(intro)}</p><p>${escapeHtml(
    osMessage,
  )}</p><ul>${details.map((line) => `<li>${escapeHtml(line)}</li>`).join('')}</ul><p>${escapeHtml(closing)}</p></div>`;

  return { subject, text, html };
}

export type SendBetaSignupEmailResult =
  | { ok: true; skipped: true }
  | { ok: true; skipped?: false }
  | { ok: false; reason: string };

/** When `RESEND_API_KEY` is unset, skips send and returns ok (local/dev). Otherwise requires a valid sender and a successful Resend response (`error` is checked — the SDK does not throw). */
export async function sendBetaSignupEmails(record: BetaLeadRecord): Promise<SendBetaSignupEmailResult> {
  const apiKeyConfigured = Boolean(process.env.RESEND_API_KEY?.trim());
  const resend = getResendClient();
  const from = getFromAddress();

  if (!apiKeyConfigured) {
    return { ok: true, skipped: true };
  }

  if (!resend || !from) {
    const reason = 'Email notify misconfigured: RESEND_FROM is missing or invalid.';
    console.error(reason);
    return { ok: false, reason };
  }

  const locale = normalizeLocale(record.locale);

  try {
    const userEmail = buildUserConfirmation(record);
    const { error: userError } = await resend.emails.send({
      from,
      to: [record.email],
      subject: userEmail.subject,
      html: userEmail.html,
      text: userEmail.text,
      headers: {
        'Content-Language': localeToHtmlLang[locale],
      },
    });
    if (userError) {
      console.error('Resend rejected beta user confirmation email', userError);
      return { ok: false, reason: userError.message || 'Email provider rejected the message.' };
    }
    return { ok: true };
  } catch (error) {
    console.error('Failed to send beta user confirmation email', error);
    return {
      ok: false,
      reason: error instanceof Error ? error.message : 'Unexpected error while sending email.',
    };
  }
}
