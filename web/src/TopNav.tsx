import React from 'react';
import { NavLink } from 'react-router-dom';
import styles from './TopNav.module.css';
import { useTheme } from './useTheme';
import { useLanguage } from './i18n/useLanguage';

const TopNav: React.FC = () => {
  const { theme, toggleTheme } = useTheme();
  const { language, toggleLanguage, t } = useLanguage();
  const isDark = theme === 'dark';
  const themeLabel = isDark ? t('topnav.toLight') : t('topnav.toDark');
  const langLabel = language === 'en' ? t('topnav.toFrench') : t('topnav.toEnglish');

  return (
    <nav className={styles.nav}>
      <div className={styles.navContent}>
        <div className={styles.links}>
          <NavLink to="/" className={({ isActive }) => isActive ? `${styles.link} ${styles.active}` : styles.link}>
            {t('nav.home')}
          </NavLink>
          <NavLink to="/settings" className={({ isActive }) => isActive ? `${styles.link} ${styles.active}` : styles.link}>
            {t('nav.settings')}
          </NavLink>
          <NavLink to="/about" className={({ isActive }) => isActive ? `${styles.link} ${styles.active}` : styles.link}>
            {t('nav.about')}
          </NavLink>
        </div>
        <div className={styles.actions}>
          <a
            href="https://github.com/maikotrindade/mobile-tester-agent"
            target="_blank"
            rel="noopener noreferrer"
            className={styles.iconButton}
            aria-label={t('topnav.github')}
            title={t('topnav.github')}
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
              <path d="M12 .5C5.65.5.5 5.65.5 12c0 5.08 3.29 9.39 7.86 10.91.58.1.79-.25.79-.56 0-.28-.01-1.02-.02-2-3.2.69-3.87-1.54-3.87-1.54-.52-1.33-1.28-1.69-1.28-1.69-1.05-.72.08-.71.08-.71 1.16.08 1.77 1.19 1.77 1.19 1.03 1.77 2.7 1.26 3.36.96.1-.75.4-1.26.73-1.55-2.55-.29-5.24-1.28-5.24-5.69 0-1.26.45-2.29 1.18-3.09-.12-.29-.51-1.46.11-3.04 0 0 .97-.31 3.18 1.18a11 11 0 0 1 5.78 0c2.2-1.49 3.17-1.18 3.17-1.18.63 1.58.23 2.75.12 3.04.74.8 1.18 1.83 1.18 3.09 0 4.42-2.69 5.4-5.26 5.68.41.36.78 1.06.78 2.14 0 1.55-.01 2.8-.01 3.18 0 .31.21.67.8.55C20.21 21.39 23.5 17.08 23.5 12 23.5 5.65 18.35.5 12 .5z" />
            </svg>
          </a>
          <button
            type="button"
            className={styles.themeToggle}
            onClick={toggleLanguage}
            aria-label={langLabel}
            title={langLabel}
          >
            <span className={styles.themeToggleLabel}>{language === 'en' ? 'FR' : 'EN'}</span>
          </button>
          <button
            type="button"
            className={styles.themeToggle}
            onClick={toggleTheme}
            aria-label={themeLabel}
            title={themeLabel}
          >
            {isDark ? (
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                <circle cx="12" cy="12" r="4" />
                <path d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M4.93 19.07l1.41-1.41M17.66 6.34l1.41-1.41" />
              </svg>
            ) : (
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
              </svg>
            )}
            <span className={styles.themeToggleLabel}>{isDark ? t('topnav.light') : t('topnav.dark')}</span>
          </button>
        </div>
      </div>
    </nav>
  );
};

export default TopNav;
