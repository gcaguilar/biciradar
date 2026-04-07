import { isValidEmail } from './is-valid-email';

export interface BetaFormMessages {
  required: string;
  email: string;
  consent: string;
  turnstile: string;
  server: string;
  loading: string;
  success: string;
  submit: string;
}

interface BetaFormConfig {
  messages: BetaFormMessages;
  turnstileSiteKey: string;
}

declare global {
  interface Window {
    turnstile?: {
      render: (
        container: HTMLElement,
        options: {
          sitekey: string;
          callback: (token: string) => void;
          'expired-callback': () => void;
          'error-callback': () => void;
        },
      ) => string;
      reset: (widgetId: string) => void;
    };
    BiciRadarAnalytics?: {
      getUtm?: () => Record<string, string>;
      track?: (name: string, props?: Record<string, string>) => void;
    };
  }
}

function readConfig(): BetaFormConfig | null {
  const el = document.querySelector('script[type="application/json"][data-beta-form-config]');
  if (!el?.textContent?.trim()) return null;
  try {
    return JSON.parse(el.textContent) as BetaFormConfig;
  } catch {
    return null;
  }
}

/** Client behaviour for the marketing beta signup form (bundled). */
export function mountBetaForm(): void {
  const config = readConfig();
  if (!config) return;

  const { messages, turnstileSiteKey } = config;
  const form = document.querySelector('[data-beta-form]');
  if (!(form instanceof HTMLFormElement)) return;

  const statusNode = form.querySelector('[data-form-status]');
  const submitButton = form.querySelector('[data-beta-submit]');
  const consentCheckbox = form.querySelector('[data-beta-consent]');
  const submitLabel = form.querySelector('[data-submit-label]');
  const startedAt = form.querySelector('[data-started-at]');
  const utmFields = form.querySelectorAll('[data-utm-field]');
  const tokenInput = form.querySelector('[data-turnstile-response]');
  const turnstileContainer = form.querySelector('[data-turnstile-container]');
  const turnstileOn = Boolean(turnstileSiteKey);
  let startedTracked = false;
  let turnstileReady = !turnstileOn;
  let turnstileWidgetId: string | null = null;

  if (startedAt instanceof HTMLInputElement) {
    startedAt.value = String(Date.now());
  }

  const setStatus = (message = '', tone = 'default') => {
    if (!(statusNode instanceof HTMLElement)) return;
    statusNode.textContent = message;
    statusNode.dataset.tone = tone;
  };

  const syncSubmitEnabled = () => {
    if (!(submitButton instanceof HTMLButtonElement)) return;
    const consentOk = consentCheckbox instanceof HTMLInputElement && consentCheckbox.checked;
    const captchaOk = !turnstileOn || turnstileReady;
    submitButton.disabled = !(consentOk && captchaOk);
  };

  const loadTurnstile = () => {
    if (!turnstileOn || !(turnstileContainer instanceof HTMLElement)) return;
    if (document.querySelector('script[data-biciradar-turnstile]')) return;
    const script = document.createElement('script');
    script.src = 'https://challenges.cloudflare.com/turnstile/v0/api.js';
    script.async = true;
    script.defer = true;
    script.dataset.biciradarTurnstile = '1';
    script.onload = () => {
      const turnstile = window.turnstile;
      if (!turnstile || !(turnstileContainer instanceof HTMLElement)) return;
      turnstileWidgetId = turnstile.render(turnstileContainer, {
        sitekey: turnstileSiteKey,
        callback: (token) => {
          if (tokenInput instanceof HTMLInputElement) tokenInput.value = token;
          turnstileReady = true;
          syncSubmitEnabled();
          setStatus('', 'default');
        },
        'expired-callback': () => {
          if (tokenInput instanceof HTMLInputElement) tokenInput.value = '';
          turnstileReady = false;
          syncSubmitEnabled();
        },
        'error-callback': () => {
          if (tokenInput instanceof HTMLInputElement) tokenInput.value = '';
          turnstileReady = false;
          syncSubmitEnabled();
          setStatus(messages.turnstile, 'error');
        },
      });
    };
    document.head.appendChild(script);
  };

  const scheduleTurnstileLoad = () => {
    if (!turnstileOn || !(turnstileContainer instanceof HTMLElement)) return;
    const observeTarget = form.closest('section') ?? form;
    if (!('IntersectionObserver' in window)) {
      loadTurnstile();
      return;
    }
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries.some((entry) => entry.isIntersecting)) {
          observer.disconnect();
          loadTurnstile();
        }
      },
      { rootMargin: '280px 0px', threshold: 0.01 },
    );
    observer.observe(observeTarget);
  };

  scheduleTurnstileLoad();
  syncSubmitEnabled();
  if (consentCheckbox instanceof HTMLInputElement) {
    consentCheckbox.addEventListener('change', syncSubmitEnabled);
  }

  const syncUtm = () => {
    const utm = window.BiciRadarAnalytics?.getUtm?.() || {};
    utmFields.forEach((field) => {
      if (field instanceof HTMLInputElement && field.dataset.utmField) {
        field.value = utm[field.dataset.utmField] || '';
      }
    });
  };

  const clearCustomValidity = (field: Element) => {
    if (field instanceof HTMLInputElement || field instanceof HTMLSelectElement) {
      field.setCustomValidity('');
    }
  };

  const validateField = (field: Element) => {
    if (!(field instanceof HTMLInputElement || field instanceof HTMLSelectElement)) return;
    field.setCustomValidity('');

    if (field.name === 'consent' && field instanceof HTMLInputElement && !field.checked) {
      field.setCustomValidity(messages.consent);
      return;
    }

    if (field.required && !field.value) {
      field.setCustomValidity(messages.required);
      return;
    }

    if (field.type === 'email' && field.value) {
      const trimmed = field.value.trim();
      if (!isValidEmail(trimmed)) {
        field.setCustomValidity(messages.email);
      }
    }
  };

  form.querySelectorAll('input, select').forEach((field) => {
    if (field instanceof HTMLInputElement && field.name === 'email') {
      field.addEventListener('blur', () => {
        field.value = field.value.trim();
      });
    }
    field.addEventListener('input', () => clearCustomValidity(field));
    field.addEventListener('change', () => clearCustomValidity(field));
    field.addEventListener('invalid', () => validateField(field));
    field.addEventListener(
      'focus',
      () => {
        if (!startedTracked) {
          startedTracked = true;
          window.BiciRadarAnalytics?.track?.('beta_form_start');
        }
      },
      { once: true },
    );
  });

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    syncUtm();

    const emailInput = form.querySelector('input[name="email"]');
    if (emailInput instanceof HTMLInputElement) {
      emailInput.value = emailInput.value.trim();
    }

    let valid = true;
    form.querySelectorAll('input, select').forEach((field) => {
      validateField(field);
      if (
        (field instanceof HTMLInputElement || field instanceof HTMLSelectElement) &&
        !field.checkValidity()
      ) {
        valid = false;
      }
    });

    if (!valid) {
      form.reportValidity();
      return;
    }

    window.BiciRadarAnalytics?.track?.('beta_form_submit', {
      page_kind: form.dataset.pageKind || '',
      city_page_key: form.dataset.cityPageKey || '',
    });

    if (submitButton instanceof HTMLButtonElement) {
      submitButton.disabled = true;
    }
    if (submitLabel instanceof HTMLElement) {
      submitLabel.textContent = messages.loading;
    }
    setStatus(messages.loading);

    let submitFailed = false;
    try {
      const response = await fetch(form.action, {
        method: 'POST',
        headers: {
          Accept: 'application/json',
        },
        body: new FormData(form),
      });

      const payload = (await response.json().catch(() => null)) as {
        ok?: boolean;
        message?: string;
        redirectPath?: string;
        warning?: string;
      } | null;

      if (!response.ok || !payload?.ok) {
        submitFailed = true;
        const errorMessage = payload?.message || messages.server;
        setStatus(errorMessage, 'error');
        window.BiciRadarAnalytics?.track?.('beta_form_error', {
          message: errorMessage,
        });
        return;
      }

      setStatus(messages.success, 'success');
      const osField = form.querySelector('[name="operatingSystem"]');
      window.BiciRadarAnalytics?.track?.('beta_form_success', {
        city_page_key: form.dataset.cityPageKey || '',
        os: osField instanceof HTMLSelectElement ? osField.value : '',
      });
      if (payload?.warning) {
        window.BiciRadarAnalytics?.track?.('beta_form_warning', {
          message: payload.warning,
        });
      }

      const redirectUrl = new URL(
        payload.redirectPath || form.dataset.redirectPath || '/',
        window.location.origin,
      );
      const utm = window.BiciRadarAnalytics?.getUtm?.() || {};
      Object.entries(utm).forEach(([key, value]) => {
        if (key.startsWith('utm_') && value) {
          redirectUrl.searchParams.set(key, value);
        }
      });
      window.location.assign(redirectUrl.toString());
    } catch (error) {
      submitFailed = true;
      console.error(error);
      setStatus(messages.server, 'error');
      window.BiciRadarAnalytics?.track?.('beta_form_error', {
        message: messages.server,
      });
    } finally {
      if (submitFailed && turnstileOn && window.turnstile && turnstileWidgetId != null) {
        try {
          window.turnstile.reset(turnstileWidgetId);
        } catch (resetError) {
          console.error(resetError);
        }
      }
      if (submitFailed && turnstileOn) {
        turnstileReady = false;
        if (tokenInput instanceof HTMLInputElement) tokenInput.value = '';
      }
      syncSubmitEnabled();
      if (submitLabel instanceof HTMLElement) {
        submitLabel.textContent = messages.submit;
      }
    }
  });
}
