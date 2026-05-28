import React, { useEffect, useState } from 'react';
import axios from 'axios';
import styles from './Reports.module.css';
import { useLanguage } from '../../i18n/useLanguage';

interface ReportEvent {
  index: number;
  timestamp: string;
  type: 'tool' | 'screenshot' | 'log';
  toolName?: string;
  args?: string;
  result?: string;
  resultPrefix?: string;
  screenshotPath?: string;
  message?: string;
}

interface ReportManifest {
  startedAt: string;
  endedAt?: string;
  scenario: string;
  packageName: string;
  logsEnabled: boolean;
  screenshotsEnabled: boolean;
  recordingEnabled: boolean;
  status: string;
  videoChunks: string[];
  events: ReportEvent[];
}

const fileUrl = (relative: string) =>
  `/api/report/file?path=${encodeURIComponent(relative)}`;

const formatTime = (iso: string) => {
  try {
    return new Date(iso).toLocaleTimeString();
  } catch {
    return iso;
  }
};

const tryFormatJson = (raw: string): string => {
  let body = raw.trim();
  if (!body) return raw;

  // perceiveScreen results come wrapped as: `{} "OK: ...payload..."`
  // Strip the leading `{} `, the opening `"<PREFIX>: ` (e.g. "OK: ", "VISIBLE: "), and the trailing `"`.
  let prefixOut = '';
  if (body.startsWith('{} ')) {
    prefixOut = '{} ';
    body = body.slice(3).trim();
  }
  const wrapMatch = body.match(/^"([A-Z_]+):\s*([\s\S]*)"$/);
  if (wrapMatch) {
    prefixOut += `${wrapMatch[1]}: `;
    body = wrapMatch[2].trim();
  }

  // Try whole string first, then after first space (legacy "PREFIX {...}" form).
  const candidates = [body];
  const spaceIdx = body.indexOf(' ');
  if (spaceIdx > 0) candidates.push(body.slice(spaceIdx + 1).trim());
  for (const candidate of candidates) {
    if (!(candidate.startsWith('{') || candidate.startsWith('['))) continue;
    try {
      const extraPrefix = candidate === body ? '' : body.slice(0, spaceIdx) + ' ';
      return prefixOut + extraPrefix + JSON.stringify(JSON.parse(candidate), null, 2);
    } catch {
      // not valid JSON — fall through
    }
  }
  return raw;
};

const formatDuration = (startIso: string, endIso: string) => {
  const ms = new Date(endIso).getTime() - new Date(startIso).getTime();
  if (!Number.isFinite(ms) || ms < 0) return '—';
  const totalSeconds = Math.floor(ms / 1000);
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return `${minutes}m ${seconds}s`;
};

const Reports: React.FC = () => {
  const { t } = useLanguage();
  const [manifest, setManifest] = useState<ReportManifest | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [collapsed, setCollapsed] = useState<Set<number>>(new Set());

  useEffect(() => {
    if (!manifest) return;
    setCollapsed(new Set(
      manifest.events
        .filter((e) => e.toolName === 'perceiveScreen')
        .map((e) => e.index)
    ));
  }, [manifest]);

  const toggle = (index: number) => {
    setCollapsed((prev) => {
      const next = new Set(prev);
      if (next.has(index)) next.delete(index); else next.add(index);
      return next;
    });
  };

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.get<ReportManifest>('/api/report');
      setManifest(response.data);
    } catch (err: unknown) {
      if (axios.isAxiosError(err) && err.response?.status === 404) {
        setManifest(null);
      } else {
        setError(t('reports.loadFailed'));
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  if (loading) {
    return <div className={styles.container}><p>{t('reports.loading')}</p></div>;
  }

  if (error) {
    return (
      <div className={styles.container}>
        <p>{error}</p>
        <button onClick={load}>{t('reports.retry')}</button>
      </div>
    );
  }

  if (!manifest) {
    return (
      <div className={styles.container}>
        <h1>{t('reports.title')}</h1>
        <div className={styles.empty}>
          {t('reports.empty')}
        </div>
      </div>
    );
  }

  const passed = manifest.status === 'completed';
  const statusClass = passed ? styles.statusCompleted : styles.statusFailed;
  const statusLabel = passed ? t('reports.passed') : t('reports.failed');

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <div>
          <h1 style={{ margin: 0 }}>
            {t('reports.latest')}
            <span className={`${styles.statusChip} ${statusClass}`}>{statusLabel}</span>
          </h1>
          <div className={styles.meta}>
            <div><strong>{t('reports.scenario')}</strong> {manifest.scenario}</div>
            <div><strong>{t('reports.package')}</strong> {manifest.packageName}</div>
            <div><strong>{t('reports.started')}</strong> {formatTime(manifest.startedAt)}{manifest.endedAt && ` • ${t('reports.duration')} ${formatDuration(manifest.startedAt, manifest.endedAt)}`}</div>
          </div>
        </div>
        <button onClick={load}>{t('reports.refresh')}</button>
      </div>

      {manifest.recordingEnabled && manifest.videoChunks.length > 0 && (
        <section className={styles.videoSection}>
          <h2 style={{ margin: 0 }}>{t('reports.recording')}</h2>
          {manifest.videoChunks.map((chunk) => (
            <video key={chunk} controls src={fileUrl(chunk)} />
          ))}
        </section>
      )}

      <section className={styles.eventList}>
        <h2>{t('reports.timeline', { count: manifest.events.length })}</h2>
        {manifest.events.length === 0 && (
          <div className={styles.empty}>{t('reports.noEvents')}</div>
        )}
        {manifest.events.map((event) => {
          const hasBody = !!(event.args || event.result || event.message || event.screenshotPath);
          const isCollapsed = collapsed.has(event.index);
          const normalizedPrefix = event.resultPrefix?.replace(/^["{}\s]+|["\s:,]+$/g, '');
          const failed = !!normalizedPrefix && ['NOT_VISIBLE', 'NOT_FOUND', 'ERROR', 'TIMEOUT', 'AMBIGUOUS'].includes(normalizedPrefix);
          return (
            <div key={event.index} className={styles.eventRow}>
              <button
                type="button"
                className={`${styles.eventHeader} ${failed ? styles.eventHeaderFailed : ''}`}
                onClick={() => hasBody && toggle(event.index)}
                aria-expanded={hasBody ? !isCollapsed : undefined}
                disabled={!hasBody}
              >
                <span className={styles.eventIndex}>#{event.index}</span>
                <span>{formatTime(event.timestamp)}</span>
                {event.type === 'tool' && event.toolName && (
                  <span className={styles.toolName}>{event.toolName}</span>
                )}
                {normalizedPrefix && (
                  <span className={`${styles.prefixChip} ${styles[`prefix${normalizedPrefix}`] ?? ''}`}>
                    {normalizedPrefix}
                  </span>
                )}
                {event.type === 'screenshot' && <span>{t('reports.screenshot')}</span>}
                <span className={styles.chevron} aria-hidden="true">
                  {hasBody ? (isCollapsed ? '▸' : '▾') : ''}
                </span>
              </button>
              {hasBody && !isCollapsed && (
                <div className={styles.eventBody}>
                  {event.args && <div className={styles.args}>{event.args}</div>}
                  {event.result && <div className={styles.result}>{tryFormatJson(event.result)}</div>}
                  {event.message && <div className={styles.result}>{event.message}</div>}
                  {event.screenshotPath && (
                    <img
                      className={styles.screenshot}
                      src={fileUrl(event.screenshotPath)}
                      alt={`screenshot ${event.index}`}
                    />
                  )}
                </div>
              )}
            </div>
          );
        })}
      </section>
    </div>
  );
};

export default Reports;
