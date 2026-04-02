import { normalizeLocale, type Locale } from './i18n';

const VERIFY_URL = 'https://challenges.cloudflare.com/turnstile/v0/siteverify';

const messages: Record<Locale, string> = {
  es: 'Completa la verificación de seguridad e inténtalo de nuevo.',
  en: 'Complete the security verification and try again.',
  ca: 'Completa la verificació de seguretat i torna-ho a provar.',
  gl: 'Completa a verificación de seguridade e téntao de novo.',
  eu: 'Osatu segurtasun egiaztapena eta saiatu berriro.',
};

function readFormValue(formData: FormData, key: string) {
  const value = formData.get(key);
  return typeof value === 'string' ? value.trim() : '';
}

function turnstileMessage(locale: Locale) {
  return messages[locale] ?? messages.en;
}

function getClientIp(headers: Headers) {
  return (
    headers.get('cf-connecting-ip')?.trim() ||
    headers.get('x-forwarded-for')?.split(',')[0]?.trim() ||
    ''
  );
}

/**
 * When `TURNSTILE_SECRET_KEY` is unset, verification is skipped (local dev).
 * In production, set both `PUBLIC_TURNSTILE_SITE_KEY` and `TURNSTILE_SECRET_KEY`.
 */
export async function verifyTurnstileFromRequest(
  formData: FormData,
  headers: Headers,
): Promise<{ ok: true } | { ok: false; status: number; message: string }> {
  const secret = process.env.TURNSTILE_SECRET_KEY?.trim();
  if (!secret) {
    return { ok: true };
  }

  const locale = normalizeLocale(readFormValue(formData, 'locale'));
  const message = turnstileMessage(locale);

  const token = readFormValue(formData, 'cf-turnstile-response');
  if (!token) {
    return { ok: false, status: 400, message };
  }

  const body = new URLSearchParams();
  body.set('secret', secret);
  body.set('response', token);
  const ip = getClientIp(headers);
  if (ip) {
    body.set('remoteip', ip);
  }

  const response = await fetch(VERIFY_URL, {
    method: 'POST',
    headers: { 'content-type': 'application/x-www-form-urlencoded' },
    body,
  });

  const data = (await response.json()) as { success?: boolean };
  if (!data.success) {
    return { ok: false, status: 400, message };
  }

  return { ok: true };
}
