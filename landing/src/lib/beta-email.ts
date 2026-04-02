import { Resend } from 'resend';
import { betaUserEmailContent } from '../content/marketing/beta-email-content';
import type { BetaLeadRecord } from './beta-form';
import { localeToHtmlLang } from './i18n';
import { siteConfig } from './site-config';

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

  const links: Array<{ href: string; label: string }> = [];
  if (androidFlow) {
    links.push({ href: siteConfig.googleTestersGroupUrl, label: content.googleGroupLabel });
  }
  if (record.operatingSystem !== 'android') {
    links.push({ href: siteConfig.appStoreUrl, label: content.appStoreLabel });
  }
  if (androidFlow) {
    links.push({ href: siteConfig.playStoreUrl, label: content.playStoreLabel });
  }

  const html = `
    <div lang="${record.locale}" style="font-family:Arial,sans-serif;line-height:1.6;color:#0f172a">
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

  try {
    await resend.emails.send({
      from,
      to: [record.email],
      subject: userEmail.subject,
      html: userEmail.html,
      text: userEmail.text,
      headers: {
        'Content-Language': localeToHtmlLang[record.locale],
      },
    });
  } catch (error) {
    console.error('Failed to send beta email', error);
  }
}
