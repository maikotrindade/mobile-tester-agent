import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen, act } from '@testing-library/react';
import { LanguageProvider } from './LanguageContext';
import { useLanguage, LANGUAGE_STORAGE_KEY } from './useLanguage';

function Probe({ keyName, vars }: { keyName: string; vars?: Record<string, string | number> }) {
  const { t, language, toggleLanguage } = useLanguage();
  return (
    <div>
      <span data-testid="lang">{language}</span>
      <span data-testid="val">{t(keyName as never, vars)}</span>
      <button onClick={toggleLanguage}>toggle</button>
    </div>
  );
}

describe('LanguageProvider t()', () => {
  beforeEach(() => localStorage.clear());

  it('resolves nested keys', () => {
    localStorage.setItem(LANGUAGE_STORAGE_KEY, 'en');
    render(
      <LanguageProvider>
        <Probe keyName="nav.home" />
      </LanguageProvider>,
    );
    expect(screen.getByTestId('val').textContent).toBe('Home');
  });

  it('interpolates {variables}', () => {
    localStorage.setItem(LANGUAGE_STORAGE_KEY, 'en');
    render(
      <LanguageProvider>
        <Probe keyName="home.savedScenarios" vars={{ count: 3 }} />
      </LanguageProvider>,
    );
    expect(screen.getByTestId('val').textContent).toBe('Saved Scenarios (3)');
  });

  it('returns the key when not found', () => {
    localStorage.setItem(LANGUAGE_STORAGE_KEY, 'en');
    render(
      <LanguageProvider>
        <Probe keyName="does.not.exist" />
      </LanguageProvider>,
    );
    expect(screen.getByTestId('val').textContent).toBe('does.not.exist');
  });

  it('toggleLanguage flips en <-> fr and persists', () => {
    localStorage.setItem(LANGUAGE_STORAGE_KEY, 'en');
    render(
      <LanguageProvider>
        <Probe keyName="nav.home" />
      </LanguageProvider>,
    );
    expect(screen.getByTestId('lang').textContent).toBe('en');
    act(() => {
      screen.getByText('toggle').click();
    });
    expect(screen.getByTestId('lang').textContent).toBe('fr');
    expect(localStorage.getItem(LANGUAGE_STORAGE_KEY)).toBe('fr');
  });
});
