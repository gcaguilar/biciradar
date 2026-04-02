import { Resend } from 'resend';
import { betaUserEmailContent } from '../content/marketing/beta-email-content';
import type { BetaLeadRecord } from './beta-form';
import { siteConfig } from './site-config';

const androidDelayMs = 2 * 60 * 1000;

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

function getInternalRecipients() {
  const configured = process.env.BETA_NOTIFY_TO?.trim() || siteConfig.contactEmail;
  return configured
    .split(',')
    .map((value) => value.trim())
    .filter(Boolean);
}

function getInternalMessageSecret() {
  return process.env.BETA_NOTIFY_SECRET?.trim() || '';
}

function escapeHtml(value: string) {
  return value
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}

function isAndroidFlow(record: BetaLeadRecord) {
  return record.operatingSystem === 'android' || record.operatingSystem === 'both';
}

function buildUserEmail(record: BetaLeadRecord) {
  const content = betaUserEmailContent[record.locale];
  const androidFlow = isAndroidFlow(record);
  const subject =
    record.operatingSystem === 'ios'
      ? content.iosSubject
      : record.operatingSystem === 'both'
        ? content.bothSubject
        : content.androidSubject;
  const headline =
    record.operatingSystem === 'ios'
      ? content.iosHeadline
      : record.operatingSystem === 'both'
        ? content.bothHeadline
        : content.androidHeadline;
  const intro =
    record.operatingSystem === 'ios'
      ? content.iosBody
      : record.operatingSystem === 'both'
        ? content.bothBody
        : content.androidBody;

  const links = [
    record.operatingSystem !== 'android'
      ? { href: siteConfig.appStoreUrl, label: content.appStoreLabel }
      : null,
    androidFlow
      ? { href: siteConfig.playStoreUrl, label: content.playStoreLabel }
      : null,
  ].filter(Boolean) as Array<{ href: string; label: string }>;

  const html = `
    <div style="font-family:Arial,sans-serif;line-height:1.6;color:#0f172a">
      <p>${escapeHtml(content.greeting)}</p>
      <h1 style="font-size:24px;line-height:1.2;margin:0 0 16px">${escapeHtml(headline)}</h1>
      <p>${escapeHtml(intro)}</p>
      ${
        androidFlow
          ? `<p>${escapeHtml(content.delayedHint)}</p>`
          : ''
      }
      <div style="margin:24px 0">
        ${links
          .map(
            (link) => `
              <p style="margin:0 0 12px">
                <a href="${escapeHtml(link.href)}" style="display:inline-block;background:#0f172a;color:#ffffff;padding:12px 18px;border-radius:999px;text-decoration:none;font-weight:700">
                  ${escapeHtml(link.label)}
                </a>
              </p>
            `,
          )
          .join('')}
      </div>
      <p>${escapeHtml(content.supportLine)}</p>
      <p>${escapeHtml(content.closing)}<br />${escapeHtml(content.signature)}</p>
    </div>
  `;

  const text = [
    content.greeting,
    '',
    headline,
    intro,
    androidFlow ? content.delayedHint : '',
    ...links.map((link) => `${link.label}: ${link.href}`),
    '',
    content.supportLine,
    '',
    content.closing,
    content.signature,
  ]
    .filter(Boolean)
    .join('\n');

  return {
    subject,
    html,
    text,
    scheduledAt: androidFlow ? new Date(Date.now() + androidDelayMs).toISOString() : undefined,
  };
}

function buildInternalNotification(record: BetaLeadRecord) {
  const androidFlow = isAndroidFlow(record);
  const recipients = getInternalRecipients();
  const secret = getInternalMessageSecret();
  const payload = {
    kind: 'biciradar_beta_lead',
    version: 1,
    id: record.id,
    createdAt: record.createdAt,
    email: record.email,
    locale: record.locale,
    operatingSystem: record.operatingSystem,
    city: record.city,
    bikeSystem: record.bikeSystem,
    frequency: record.frequency,
    interest: record.interest || '',
    widgets: record.widgets || '',
    smartwatch: record.smartwatch || '',
    consent: record.consent,
    pageKind: record.pageKind || '',
    pagePath: record.pagePath || '',
    cityPageKey: record.cityPageKey || '',
    referrer: record.referrer || '',
    userAgent: record.userAgent || '',
    ip: record.ip || '',
    utm: record.utm,
    n8nAction: androidFlow ? 'add_google_group' : 'none',
    scheduledUserEmail: androidFlow ? 'scheduled_2m' : 'immediate',
    ...(secret ? { secret } : {}),
  };

  const subject = `[BiciRadar Beta Lead] ${record.operatingSystem.toUpperCase()} · ${record.email}`;
  const serialized = JSON.stringify(payload, null, 2);
  const html = `<pre style="font-family:ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, Liberation Mono, monospace;white-space:pre-wrap;line-height:1.5;color:#0f172a">${escapeHtml(serialized)}</pre>`;
  const text = serialized;

  return {
    to: recipients,
    subject,
    html,
    text,
  };
}

export async function sendBetaSignupEmails(record: BetaLeadRecord) {
  const resend = getResendClient();
  const from = getFromAddress();

  if (!resend || !from) {
    console.error('Beta email skipped because RESEND_API_KEY or RESEND_FROM is missing.');
    return;
  }

  const userEmail = buildUserEmail(record);
  const internalEmail = buildInternalNotification(record);

  const jobs = [
    resend.emails.send({
      from,
      to: [record.email],
      subject: userEmail.subject,
      html: userEmail.html,
      text: userEmail.text,
      scheduledAt: userEmail.scheduledAt,
    }),
  ];

  if (internalEmail.to.length > 0) {
    jobs.push(
      resend.emails.send({
        from,
        to: internalEmail.to,
        subject: internalEmail.subject,
        html: internalEmail.html,
        text: internalEmail.text,
        replyTo: record.email,
      }),
    );
  }

  const results = await Promise.allSettled(jobs);
  results.forEach((result, index) => {
    if (result.status === 'rejected') {
      const kind = index === 0 ? 'user' : 'internal';
      console.error(`Failed to send ${kind} beta email`, result.reason);
    }
  });
}
