import type { APIRoute } from 'astro';
import { sendBetaSignupEmails } from '../../lib/beta-email';
import { storeBetaLead, validateBetaLead } from '../../lib/beta-form';
import { getThankYouPath } from '../../lib/routes';
import { verifyTurnstileFromRequest } from '../../lib/turnstile';

export const prerender = false;

function wantsJson(request: Request) {
  return request.headers.get('accept')?.includes('application/json');
}

function buildRedirectPath(request: Request, locale: Parameters<typeof getThankYouPath>[0], operatingSystem: string) {
  const redirectUrl = new URL(getThankYouPath(locale), request.url);
  if (operatingSystem) {
    redirectUrl.searchParams.set('os', operatingSystem);
  }

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

  await storeBetaLead(validation.data);
  await sendBetaSignupEmails(validation.data);
  const redirectPath = buildRedirectPath(
    request,
    validation.data.locale,
    validation.data.operatingSystem,
  );

  if (wantsJson(request)) {
    return Response.json({
      ok: true,
      redirectPath,
      city: validation.data.city,
      bikeSystem: validation.data.bikeSystem,
    });
  }

  return Response.redirect(new URL(redirectPath, request.url), 303);
};
