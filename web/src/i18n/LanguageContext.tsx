import { useCallback, useEffect, useState, type ReactNode } from 'react';
import { en, fr, type Language } from './translations';
import {
  LanguageCtx,
  LANGUAGE_STORAGE_KEY,
  getInitialLanguage,
  type LanguageContextValue,
} from './useLanguage';

type Dict = typeof en;

export function LanguageProvider({ children }: { children: ReactNode }) {
  const [language, setLanguage] = useState<Language>(getInitialLanguage);

  useEffect(() => {
    document.documentElement.setAttribute('lang', language);
    localStorage.setItem(LANGUAGE_STORAGE_KEY, language);
  }, [language]);

  const t = useCallback<LanguageContextValue['t']>((key, vars) => {
    const dict: Dict = language === 'fr' ? fr : en;
    const raw = key.split('.').reduce<unknown>((acc, k) => {
      if (acc && typeof acc === 'object' && k in (acc as Record<string, unknown>)) {
        return (acc as Record<string, unknown>)[k];
      }
      return undefined;
    }, dict);
    const str = typeof raw === 'string' ? raw : key;
    return vars ? str.replace(/\{(\w+)\}/g, (_, k: string) => String(vars[k] ?? '')) : str;
  }, [language]);

  const toggleLanguage = useCallback(() => {
    setLanguage(prev => (prev === 'en' ? 'fr' : 'en'));
  }, []);

  return (
    <LanguageCtx.Provider value={{ language, toggleLanguage, t }}>
      {children}
    </LanguageCtx.Provider>
  );
}
