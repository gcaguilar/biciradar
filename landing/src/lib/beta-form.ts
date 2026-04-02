import { appendFile, mkdir } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { dirname, join } from 'node:path';
import { randomUUID } from 'node:crypto';
import { normalizeLocale, type Locale } from './i18n';
import { isValidEmail } from './is-valid-email';

export interface BetaLeadRecord {
  id: string;
  createdAt: string;
  locale: Locale;
  email: string;
  operatingSystem: string;
  city: string;
  bikeSystem: string;
  frequency: string;
  interest?: string;
  widgets?: string;
  smartwatch?: string;
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

const requiredFields = [
  'email',
  'operatingSystem',
  'city',
  'bikeSystem',
  'frequency',
] as const;

const utmKeys = [
  'utm_source',
  'utm_medium',
  'utm_campaign',
  'utm_content',
  'utm_term',
] as const;

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

  for (const field of requiredFields) {
    if (!readValue(formData, field)) {
      return { ok: false, status: 400, message: 'Missing required fields.' };
    }
  }

  const email = readValue(formData, 'email');
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
    id: randomUUID(),
    createdAt: new Date().toISOString(),
    locale,
    email,
    operatingSystem: readValue(formData, 'operatingSystem'),
    city: readValue(formData, 'city'),
    bikeSystem: readValue(formData, 'bikeSystem'),
    frequency: readValue(formData, 'frequency'),
    interest: readValue(formData, 'interest') || undefined,
    widgets: readValue(formData, 'widgets') || undefined,
    smartwatch: readValue(formData, 'smartwatch') || undefined,
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

function getStoragePath() {
  return process.env.BETA_FORM_STORAGE_PATH?.trim() || join(tmpdir(), 'biciradar-beta-signups.ndjson');
}

async function persistToFile(record: BetaLeadRecord) {
  const storagePath = getStoragePath();
  await mkdir(dirname(storagePath), { recursive: true });
  await appendFile(storagePath, `${JSON.stringify(record)}\n`, 'utf8');
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

export async function storeBetaLead(record: BetaLeadRecord) {
  await persistToFile(record);

  try {
    await sendWebhook(record);
  } catch (error) {
    console.error('Failed to send beta webhook', error);
  }
}
