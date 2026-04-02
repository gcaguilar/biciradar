import { describe, expect, it } from 'vitest';
import { isValidEmail } from './is-valid-email';

describe('isValidEmail', () => {
  it('accepts typical Gmail addresses', () => {
    expect(isValidEmail('castellaguillermo@gmail.com')).toBe(true);
    expect(isValidEmail('user.name+tag@gmail.com')).toBe(true);
  });

  it('rejects obvious non-emails', () => {
    expect(isValidEmail('')).toBe(false);
    expect(isValidEmail('not-an-email')).toBe(false);
    expect(isValidEmail('@nodomain.com')).toBe(false);
    expect(isValidEmail('spaces in@mail.com')).toBe(false);
  });

  it('rejects overlong strings', () => {
    const local = 'a'.repeat(200);
    expect(isValidEmail(`${local}@x.co`)).toBe(false);
  });
});
