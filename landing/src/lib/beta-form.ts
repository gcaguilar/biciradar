import type { CityKey } from '../content/marketing/types';
import { normalizeLocale, type Locale } from './i18n';
import { parseCityKey } from './beta-city-system';
import { isValidEmail } from './is-valid-email';

export interface BetaLeadRecord {
  locale: Locale;
  email: string;
  operatingSystem: 'ios' | 'android' | 'both';
  /** Set when the signup comes from a city landing (hidden `cityPageKey`). */
  city?: CityKey;
  consent: boolean;
  pagePath?: string;
  pageKind?: string;
  cityPageKey?: string;
  referrer?: string;
  userAgent?: string;
  ip?: string;
  utm: Record<string, string>;
}

export interface ValidationSuccess {
  ok: true;
  data: BetaLeadRecord;
}

export interface ValidationFailure {
  ok: false;
  status: number;
  message: string;
}

export type ValidationResult = ValidationSuccess | ValidationFailure;

const utmKeys = [
  'utm_source',
  'utm_medium',
  'utm_campaign',
  'utm_content',
  'utm_term',
] as const;

const allowedOs = new Set(['ios', 'android', 'both']);

function readValue(formData: FormData, key: string) {
  const value = formData.get(key);
  return typeof value === 'string' ? value.trim() : '';
}

function getClientIp(headers: Headers) {
  return headers.get('x-forwarded-for')?.split(',')[0]?.trim() || '';
}

export function validateBetaLead(formData: FormData, headers: Headers): ValidationResult {
  const honeypot = readValue(formData, 'company');
  if (honeypot) {
    return { ok: false, status: 400, message: 'Invalid submission.' };
  }

  const startedAt = Number(readValue(formData, 'startedAt'));
  if (Number.isFinite(startedAt) && startedAt > 0 && Date.now() - startedAt < 1200) {
    return { ok: false, status: 429, message: 'Please try again.' };
  }

  const operatingSystem = readValue(formData, 'operatingSystem');
  if (!operatingSystem || !allowedOs.has(operatingSystem)) {
    return { ok: false, status: 400, message: 'Missing required fields.' };
  }

  const cityPageKeyRaw = readValue(formData, 'cityPageKey');
  const city = parseCityKey(cityPageKeyRaw) ?? undefined;

  const email = readValue(formData, 'email');
  if (!email) {
    return { ok: false, status: 400, message: 'Missing required fields.' };
  }
  if (!isValidEmail(email)) {
    return { ok: false, status: 400, message: 'Invalid email.' };
  }

  const consent = ['on', 'true', '1', 'yes'].includes(readValue(formData, 'consent').toLowerCase());
  if (!consent) {
    return { ok: false, status: 400, message: 'Consent required.' };
  }

  const locale = normalizeLocale(readValue(formData, 'locale'));
  const utm = Object.fromEntries(
    utmKeys
      .map((key) => [key, readValue(formData, key)] as const)
      .filter(([, value]) => Boolean(value)),
  );

  const data: BetaLeadRecord = {
    locale,
    email,
    operatingSystem: operatingSystem as BetaLeadRecord['operatingSystem'],
    ...(city ? { city } : {}),
    consent,
    pagePath: readValue(formData, 'pagePath') || undefined,
    pageKind: readValue(formData, 'pageKind') || undefined,
    cityPageKey: readValue(formData, 'cityPageKey') || undefined,
    referrer: headers.get('referer') || undefined,
    userAgent: headers.get('user-agent') || undefined,
    ip: getClientIp(headers) || undefined,
    utm,
  };

  return { ok: true, data };
}

async function sendWebhook(record: BetaLeadRecord) {
  const webhookUrl = process.env.BETA_FORM_WEBHOOK_URL?.trim();
  if (!webhookUrl) {
    return;
  }

  const headers: Record<string, string> = {
    'content-type': 'application/json',
  };

  const bearer = process.env.BETA_FORM_WEBHOOK_BEARER?.trim();
  if (bearer) {
    headers.authorization = `Bearer ${bearer}`;
  }

  const response = await fetch(webhookUrl, {
    method: 'POST',
    headers,
    body: JSON.stringify(record),
  });

  if (!response.ok) {
    throw new Error(`Webhook failed with ${response.status}`);
  }
}

/** Optional webhook only; lead data is not written to disk. */
export async function forwardBetaLead(record: BetaLeadRecord) {
  try {
    await sendWebhook(record);
  } catch (error) {
    console.error('Failed to send beta webhook', error);
  }
}
