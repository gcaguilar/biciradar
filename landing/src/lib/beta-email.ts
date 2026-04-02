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
  const payload = {
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
  };

  const subject = `[BiciRadar Beta Lead] ${record.operatingSystem.toUpperCase()} · ${record.email}`;
  const html = `
    <div style="font-family:Arial,sans-serif;line-height:1.6;color:#0f172a">
      <h1 style="font-size:22px;line-height:1.2;margin:0 0 16px">Nuevo lead de BiciRadar</h1>
      <p><strong>n8n_action:</strong> ${escapeHtml(payload.n8nAction)}</p>
      <p><strong>scheduled_user_email:</strong> ${escapeHtml(payload.scheduledUserEmail)}</p>
      <table style="border-collapse:collapse;margin:20px 0;width:100%">
        <tbody>
          ${Object.entries(payload)
            .map(([key, value]) => {
              const rendered =
                typeof value === 'object'
                  ? escapeHtml(JSON.stringify(value))
                  : escapeHtml(String(value));
              return `<tr>
                <td style="border:1px solid #cbd5e1;padding:8px;font-weight:700;vertical-align:top">${escapeHtml(key)}</td>
                <td style="border:1px solid #cbd5e1;padding:8px;vertical-align:top">${rendered}</td>
              </tr>`;
            })
            .join('')}
        </tbody>
      </table>
      <p><strong>payload_json</strong></p>
      <pre style="white-space:pre-wrap;background:#f8fafc;border:1px solid #cbd5e1;padding:12px;border-radius:12px">${escapeHtml(
        JSON.stringify(payload, null, 2),
      )}</pre>
    </div>
  `;

  const text = [
    'Nuevo lead de BiciRadar',
    `n8n_action: ${payload.n8nAction}`,
    `scheduled_user_email: ${payload.scheduledUserEmail}`,
    '',
    ...Object.entries(payload).map(([key, value]) =>
      `${key}: ${typeof value === 'object' ? JSON.stringify(value) : String(value)}`,
    ),
    '',
    'payload_json:',
    JSON.stringify(payload, null, 2),
  ].join('\n');

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
