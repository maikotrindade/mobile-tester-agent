import React from 'react';
import mermaid from 'mermaid';
import styles from './About.module.css';
import homeStyles from '../home/Home.module.css';
import { useTheme } from '../../useTheme';
import { useLanguage } from '../../i18n/useLanguage';

const About: React.FC = () => {
  const { theme } = useTheme();
  const { t } = useLanguage();

  React.useEffect(() => {
    const isDark = theme === 'dark';
    mermaid.initialize({
      startOnLoad: false,
      theme: 'base',
      themeVariables: isDark
        ? {
            background: '#1a2332',
            primaryColor: '#233246',
            primaryBorderColor: '#6a9bd1',
            primaryTextColor: '#e4e9f0',
            secondaryColor: '#212c3d',
            tertiaryColor: '#2a3648',
            lineColor: '#7e889a',
            textColor: '#e4e9f0',
            fontFamily: 'Inter, Segoe UI, sans-serif',
          }
        : {
            background: '#f5f7fa',
            primaryColor: '#e6eef5',
            primaryBorderColor: '#2c5282',
            primaryTextColor: '#1a2332',
            secondaryColor: '#eaeef3',
            tertiaryColor: '#ffffff',
            lineColor: '#6b7785',
            textColor: '#1a2332',
            fontFamily: 'Inter, Segoe UI, sans-serif',
          },
    });
    const nodes = document.querySelectorAll('.mermaid');
    nodes.forEach((node) => {
      const el = node as HTMLElement;
      if (el.dataset.source) {
        el.innerHTML = el.dataset.source;
      } else {
        el.dataset.source = el.innerHTML;
      }
      el.removeAttribute('data-processed');
    });
    mermaid.run();
  }, [theme]);

  return (
    <div className={styles.container}>
      <h1>{t('about.title')}</h1>
      <p className={styles.lead}>
        {t('about.lead1')}<strong>{t('about.leadProduct')}</strong>{t('about.lead2')}
        <strong>{t('about.leadAi')}</strong>{t('about.lead3')}
      </p>

      <div className={homeStyles.mainLayout}>
        {/* Left Column: Project Cards */}
        <div className={homeStyles.leftPanel}>
          <div className={homeStyles.panelTitle}>
            <span className="icon">📦</span> {t('about.core')}
          </div>
          <div>
            <article className={styles.card}>
              <h3>{t('about.backendTitle')}</h3>
              <p>
                {t('about.backendP1Pre')}
                <code className={styles.inline}>Koog</code>{t('about.backendP1Mid')}
                <code className={styles.inline}>Ktor</code>{t('about.backendP1Post')}
              </p>
              <p>{t('about.backendP2')}</p>
              <p>
                {t('about.backendP3')}
              </p>
              <div className={styles.btns}>
                <a
                  className={`${homeStyles.btnAddStep} ${homeStyles.button}`}
                  href="https://github.com/maikotrindade/mobile-tester-agent"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  {t('about.viewRepo')}
                </a>
              </div>
            </article>

            <article className={styles.card}>
              <h3>{t('about.platformsTitle')}</h3>
              <p>{t('about.platformsIntro')}</p>
              <ul>
                <li><strong>{t('about.platformAndroid')}</strong> — {t('about.platformAndroidDesc')}</li>
                <li><strong>{t('about.platformIos')}</strong> — {t('about.platformIosDesc')}</li>
                <li><strong>{t('about.platformRn')}</strong> — {t('about.platformRnDesc')}</li>
              </ul>
            </article>
          </div>
        </div>

        {/* Right Column: System Overview Diagram */}
        <div className={homeStyles.rightPanel}>
          <div className={homeStyles.panelTitle}>
            <span className="icon">🗺️</span> {t('about.architecture')}
          </div>
          <section className={styles.diagram} aria-label={t('about.diagramLabel')}>
            <div className="mermaid">
              {`
graph TD
    A[Frontend] --> B[Backend API]
    B --> C[Koog Agent]
    C --> G[LLM]
    C --> T[Platform Tools]
    T --> AND[Android]
    T --> IOS[iOS]
    T --> RN[React Native]
    C --> H[Reports]
    H --> A
`}
            </div>
          </section>
        </div>
      </div>
    </div>
  );
}
export default About;
