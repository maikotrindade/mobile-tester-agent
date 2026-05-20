import { createContext, useContext } from 'react';
import { en, type Language } from './translations';

type Dict = typeof en;

type Path<T> = T extends object
  ? { [K in keyof T]: K extends string ? (T[K] extends object ? `${K}` | `${K}.${Path<T[K]>}` : `${K}`) : never }[keyof T]
  : never;

export type TranslationKey = Path<Dict>;

export type LanguageContextValue = {
  language: Language;
  toggleLanguage: () => void;
  t: (key: TranslationKey, vars?: Record<string, string | number>) => string;
};

export const LANGUAGE_STORAGE_KEY = 'language';

export const LanguageCtx = createContext<LanguageContextValue | null>(null);

export function getInitialLanguage(): Language {
  if (typeof window === 'undefined') return 'en';
  const stored = localStorage.getItem(LANGUAGE_STORAGE_KEY) as Language | null;
  if (stored === 'en' || stored === 'fr') return stored;
  return navigator.language?.toLowerCase().startsWith('fr') ? 'fr' : 'en';
}

export function useLanguage(): LanguageContextValue {
  const ctx = useContext(LanguageCtx);
  if (!ctx) throw new Error('useLanguage must be used within a LanguageProvider');
  return ctx;
}
