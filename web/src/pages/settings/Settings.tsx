import React, { useState, useEffect } from 'react';
import styles from './Settings.module.css';
import homeStyles from '../home/Home.module.css';
import { useLanguage } from '../../i18n/useLanguage';

const Settings: React.FC = () => {
  const { t } = useLanguage();
  const [executorInfoId, setExecutorInfoId] = useState<string>('');
  const [llmTemperature, setLlmTemperature] = useState<number>(0.2);
  const [maxAgentIterations, setMaxAgentIterations] = useState<number>(50);
  const [logTokensConsumption, setLogTokensConsumption] = useState<boolean>(true);
  const [apiBaseUrl, setApiBaseUrl] = useState<string>('http://localhost:8080');
  const [packageName, setPackageName] = useState<string>('');
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  useEffect(() => {
    const savedExecutorInfoId = localStorage.getItem('executorInfoId');
    if (savedExecutorInfoId) {
      setExecutorInfoId(savedExecutorInfoId);
    }
    const savedLlmTemperature = localStorage.getItem('llmTemperature');
    if (savedLlmTemperature) {
      setLlmTemperature(parseFloat(savedLlmTemperature));
    }
    const savedMaxAgentIterations = localStorage.getItem('maxAgentIterations');
    if (savedMaxAgentIterations) {
      setMaxAgentIterations(parseInt(savedMaxAgentIterations, 10));
    }
    const savedLogTokensConsumption = localStorage.getItem('logTokensConsumption');
    if (savedLogTokensConsumption) {
      setLogTokensConsumption(savedLogTokensConsumption === 'true');
    }
    const savedApiBaseUrl = localStorage.getItem('apiBaseUrl');
    if (savedApiBaseUrl) {
      setApiBaseUrl(savedApiBaseUrl);
    }
    const savedPackageName = localStorage.getItem('packageName');
    if (savedPackageName) {
      setPackageName(savedPackageName);
    }
  }, []);

  const handleSave = async () => {
    const settings = {
      executorInfoId,
      llmTemperature,
      maxAgentIterations,
      logTokensConsumption,
      apiBaseUrl,
      packageName,
    };

    // Save to localStorage
    localStorage.setItem('executorInfoId', executorInfoId);
    localStorage.setItem('llmTemperature', llmTemperature.toString());
    localStorage.setItem('maxAgentIterations', maxAgentIterations.toString());
    localStorage.setItem('logTokensConsumption', logTokensConsumption.toString());
    localStorage.setItem('apiBaseUrl', apiBaseUrl);
    localStorage.setItem('packageName', packageName);

    try {
      const response = await fetch('/api/config', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(settings),
      });

      if (response.ok) {
        setSuccessMessage(t('settings.successSaved'));
      } else {
        setSuccessMessage(t('settings.failedSaved'));
      }
    } catch (error) {
      console.error('Error saving settings:', error);
      setSuccessMessage(t('settings.errorSaving'));
    }

    setTimeout(() => setSuccessMessage(null), 3000);
  };

  return (
    <div className={styles.container}>
      <h1>{t('settings.title')}</h1>

      <div className={homeStyles.mainLayout}>
        {/* Left Panel - Model Configuration */}
        <div className={homeStyles.leftPanel}>
          <div className={homeStyles.panelTitle}>
            <span className="icon">🤖</span>{t('settings.modelConfig')}
          </div>

          <div className={styles.formSection}>
            <div className={styles.formGroup}>
              <label htmlFor="ai-model">{t('settings.selectModelLabel')}</label>
              <select
                id="ai-model"
                value={executorInfoId}
                onChange={(e) => setExecutorInfoId(e.target.value)}
              >
                <option value="">{t('settings.selectModelPlaceholder')}</option>
                <option value="Opus47">Claude Opus 4.7</option>
                <option value="DeepSeekV4Flash">DeepSeek V4 Flash</option>
                <option value="Gemini3Pro">Gemini 3 Pro</option>
                <option value="GPT52Pro">GPT-5.2 Pro</option>
                <option value="QWEN36B">QWEN 3 6B</option>
                <option value="Llama4">Llama 4</option>
                <option value="Grok8BExecutor">Grok 8B</option>
              </select>
            </div>

            <div className={styles.formGroup}>
              <label htmlFor="llm-temperature">{t('settings.llmTemperatureLabel', { value: llmTemperature })}</label>
              <input
                type="range"
                id="llm-temperature"
                min="0"
                max="10"
                step="0.2"
                value={llmTemperature}
                onChange={(e) => setLlmTemperature(parseFloat(e.target.value))}
              />
              <div className={styles.temperatureInfo}>
                <small>{t('settings.llmTemperatureHelp')}</small>
              </div>
            </div>
          </div>
        </div>

        {/* Right Panel - Agent Configuration */}
        <div className={homeStyles.rightPanel}>
          <div className={homeStyles.panelTitle}>
            <span className="icon">⚙️</span>{t('settings.agentConfig')}
          </div>

          <div className={styles.formSection}>

            <div className={styles.formGroup}>
              <label htmlFor="api-base-url">{t('settings.apiBaseUrlLabel')}</label>
              <input
                type="url"
                id="api-base-url"
                value={apiBaseUrl}
                onChange={(e) => setApiBaseUrl(e.target.value)}
                placeholder="http://localhost:8080"
                className={styles.inputField}
                pattern="https?://.*"
                required
              />
              <div className={styles.tokenInfo}>
                <small>{t('settings.apiBaseUrlHelp')}</small>
              </div>
            </div>

            <div className={styles.formGroup}>
              <label htmlFor="package-name">{t('settings.packageNameLabel')}</label>
              <input
                type="text"
                id="package-name"
                value={packageName}
                onChange={(e) => setPackageName(e.target.value)}
                placeholder="com.example.myapp"
                className={styles.inputField}
              />
              <div className={styles.tokenInfo}>
                <small>{t('settings.packageNameHelp')}</small>
              </div>
            </div>

            <div className={styles.formGroup}>
              <label htmlFor="max-agent-iterations">{t('settings.maxIterationsLabel')}</label>
              <input
                type="number"
                id="max-agent-iterations"
                value={maxAgentIterations}
                onChange={(e) => setMaxAgentIterations(parseInt(e.target.value, 10))}
                min="1"
                max="200"
              />
              <div className={styles.iterationInfo}>
                <small>{t('settings.maxIterationsHelp')}</small>
              </div>
            </div>

            <div className={styles.formGroup}>
              <label className={styles.checkboxLabel}>
                <input
                  type="checkbox"
                  checked={logTokensConsumption}
                  onChange={(e) => setLogTokensConsumption(e.target.checked)}
                />
                {t('settings.logTokensLabel')}
              </label>
              <div className={styles.tokenInfo}>
                <small>{t('settings.logTokensHelp')}</small>
              </div>
            </div>

            <div className={styles.buttonContainer}>
              <button
                onClick={handleSave}
                className={`${homeStyles.button} ${homeStyles.btnRunTest}`}
                disabled={!executorInfoId}
              >
                {t('settings.saveSettings')}
              </button>
            </div>

            {successMessage && (
              <div className={styles.successMessage}>
                {successMessage}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Settings;
