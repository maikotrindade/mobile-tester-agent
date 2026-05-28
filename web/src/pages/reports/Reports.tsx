import React, { useEffect, useState } from 'react';
import axios from 'axios';
import styles from './Reports.module.css';

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

const Reports: React.FC = () => {
  const [manifest, setManifest] = useState<ReportManifest | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

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
        setError('Failed to load report');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  if (loading) {
    return <div className={styles.container}><p>Loading report...</p></div>;
  }

  if (error) {
    return (
      <div className={styles.container}>
        <p>{error}</p>
        <button onClick={load}>Retry</button>
      </div>
    );
  }

  if (!manifest) {
    return (
      <div className={styles.container}>
        <h1>Reports</h1>
        <div className={styles.empty}>
          No report yet — run a test to generate one.
        </div>
      </div>
    );
  }

  const statusClass =
    manifest.status === 'completed' ? styles.statusCompleted
    : manifest.status === 'failed' ? styles.statusFailed
    : manifest.status === 'stopped' ? styles.statusStopped
    : styles.statusRunning;

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <div>
          <h1 style={{ margin: 0 }}>
            Latest Report
            <span className={`${styles.statusChip} ${statusClass}`}>{manifest.status}</span>
          </h1>
          <div className={styles.meta}>
            <div><strong>Scenario:</strong> {manifest.scenario}</div>
            <div><strong>Package:</strong> {manifest.packageName}</div>
            <div><strong>Started:</strong> {formatTime(manifest.startedAt)}{manifest.endedAt && ` • Ended: ${formatTime(manifest.endedAt)}`}</div>
          </div>
        </div>
        <button onClick={load}>Refresh</button>
      </div>

      {manifest.recordingEnabled && manifest.videoChunks.length > 0 && (
        <section className={styles.videoSection}>
          <h2 style={{ margin: 0 }}>Recording</h2>
          {manifest.videoChunks.map((chunk) => (
            <video key={chunk} controls src={fileUrl(chunk)} />
          ))}
        </section>
      )}

      <section className={styles.eventList}>
        <h2>Timeline ({manifest.events.length})</h2>
        {manifest.events.length === 0 && (
          <div className={styles.empty}>No events recorded.</div>
        )}
        {manifest.events.map((event) => (
          <div key={event.index} className={styles.eventRow}>
            <div className={styles.eventHeader}>
              <span>#{event.index}</span>
              <span>{formatTime(event.timestamp)}</span>
              {event.type === 'tool' && event.toolName && (
                <span className={styles.toolName}>{event.toolName}</span>
              )}
              {event.resultPrefix && (
                <span className={`${styles.prefixChip} ${styles[`prefix${event.resultPrefix}`] ?? ''}`}>
                  {event.resultPrefix}
                </span>
              )}
              {event.type === 'screenshot' && <span>screenshot</span>}
            </div>
            {event.args && <div className={styles.args}>{event.args}</div>}
            {event.result && <div className={styles.result}>{event.result}</div>}
            {event.message && <div className={styles.result}>{event.message}</div>}
            {event.screenshotPath && (
              <img
                className={styles.screenshot}
                src={fileUrl(event.screenshotPath)}
                alt={`screenshot ${event.index}`}
              />
            )}
          </div>
        ))}
      </section>
    </div>
  );
};

export default Reports;
