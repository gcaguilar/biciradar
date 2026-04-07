import type { APIRoute } from 'astro';
import { sendBetaSignupEmails } from '../../lib/beta-email';
import type { BetaLeadRecord } from '../../lib/beta-form';
import { forwardBetaLead, validateBetaLead } from '../../lib/beta-form';
import { getThankYouPath } from '../../lib/routes';
import { verifyTurnstileFromRequest } from '../../lib/turnstile';

export const prerender = false;

function wantsJson(request: Request) {
  return request.headers.get('accept')?.includes('application/json');
}

function buildRedirectPath(
  request: Request,
  locale: Parameters<typeof getThankYouPath>[0],
  operatingSystem: BetaLeadRecord['operatingSystem'],
) {
  const redirectUrl = new URL(getThankYouPath(locale), request.url);
  redirectUrl.searchParams.set('os', operatingSystem);
  return `${redirectUrl.pathname}${redirectUrl.search}`;
}

export const POST: APIRoute = async ({ request }) => {
  const formData = await request.formData();

  const turnstile = await verifyTurnstileFromRequest(formData, request.headers);
  if (!turnstile.ok) {
    if (wantsJson(request)) {
      return Response.json({ ok: false, message: turnstile.message }, { status: turnstile.status });
    }
    const fallbackPath = String(formData.get('pagePath') || '/');
    return Response.redirect(new URL(fallbackPath, request.url), 303);
  }

  const validation = validateBetaLead(formData, request.headers);

  if (!validation.ok) {
    if (wantsJson(request)) {
      return Response.json(
        {
          ok: false,
          message: validation.message,
        },
        { status: validation.status },
      );
    }

    const fallbackPath = String(formData.get('pagePath') || '/');
    return Response.redirect(new URL(fallbackPath, request.url), 303);
  }

  await forwardBetaLead(validation.data);

  const emailResult = await sendBetaSignupEmails(validation.data);
  const redirectPath = buildRedirectPath(request, validation.data.locale, validation.data.operatingSystem);
  const emailWarning = !emailResult.ok ? emailResult.reason : undefined;

  if (emailWarning) {
    console.error('Beta signup accepted with email notification failure', {
      reason: emailWarning,
      pagePath: validation.data.pagePath || null,
      operatingSystem: validation.data.operatingSystem,
      locale: validation.data.locale,
    });
  }

  if (wantsJson(request)) {
    return Response.json({
      ok: true,
      redirectPath,
      ...(emailWarning ? { warning: emailWarning } : {}),
    });
  }

  return Response.redirect(new URL(redirectPath, request.url), 303);
};
