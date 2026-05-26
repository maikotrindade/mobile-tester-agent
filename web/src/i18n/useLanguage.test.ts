import { describe, it, expect, beforeEach, vi } from 'vitest';
import { getInitialLanguage, LANGUAGE_STORAGE_KEY } from './useLanguage';

describe('getInitialLanguage', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('returns the stored language when valid', () => {
    localStorage.setItem(LANGUAGE_STORAGE_KEY, 'fr');
    expect(getInitialLanguage()).toBe('fr');
  });

  it('falls back to navigator language when nothing is stored (fr)', () => {
    vi.spyOn(navigator, 'language', 'get').mockReturnValue('fr-FR');
    expect(getInitialLanguage()).toBe('fr');
  });

  it('falls back to en for non-French navigator language', () => {
    vi.spyOn(navigator, 'language', 'get').mockReturnValue('en-US');
    expect(getInitialLanguage()).toBe('en');
  });

  it('ignores garbage in storage and falls back', () => {
    localStorage.setItem(LANGUAGE_STORAGE_KEY, 'klingon');
    vi.spyOn(navigator, 'language', 'get').mockReturnValue('en-US');
    expect(getInitialLanguage()).toBe('en');
  });
});
