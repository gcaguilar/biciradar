import validator from 'validator';

const MAX_EMAIL_LENGTH = 254;

/**
 * Uses validator.js `isEmail` (RFC 5322–inspired rules, de facto standard on npm).
 * Pass a trimmed string; callers trim when reading forms.
 */
export function isValidEmail(email: string): boolean {
  if (!email || email.length > MAX_EMAIL_LENGTH) {
    return false;
  }

  return validator.isEmail(email, {
    allow_ip_domain: false,
    allow_utf8_local_part: false,
  });
}
