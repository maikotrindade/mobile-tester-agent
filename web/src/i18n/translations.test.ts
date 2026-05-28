import { describe, it, expect } from 'vitest';
import { en, fr } from './translations';

function collectKeys(obj: unknown, prefix = ''): string[] {
  if (!obj || typeof obj !== 'object') return [prefix];
  const out: string[] = [];
  for (const [k, v] of Object.entries(obj as Record<string, unknown>)) {
    const next = prefix ? `${prefix}.${k}` : k;
    if (v && typeof v === 'object') out.push(...collectKeys(v, next));
    else out.push(next);
  }
  return out.sort();
}

describe('translations', () => {
  it('en and fr have identical key sets', () => {
    expect(collectKeys(fr)).toEqual(collectKeys(en));
  });

  it('every leaf is a non-empty string', () => {
    for (const dict of [en, fr]) {
      for (const key of collectKeys(dict)) {
        const val = key.split('.').reduce<unknown>(
          (acc, k) => (acc as Record<string, unknown>)[k],
          dict,
        );
        expect(typeof val).toBe('string');
        expect((val as string).length).toBeGreaterThan(0);
      }
    }
  });
});
