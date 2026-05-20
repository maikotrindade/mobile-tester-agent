import styles from './Footer.module.css';
import { useLanguage } from './i18n/useLanguage';

export function Footer() {
  const { t } = useLanguage();
  return (
    <footer className={styles.footer}>
      <p>
        {t('footer.madeWith')} <a href="https://github.com/maikotrindade" target="_blank" rel="noopener noreferrer">Maiko Trindade</a>
      </p>
    </footer>
  );
}
