import React from 'react';
import { Activity, Database, Sparkles, RefreshCw } from 'lucide-react';

export default function Header({ activeTab, onRefresh, isRefreshing, healthStatus }) {
  const titles = {
    dashboard: { title: 'Executive Campus Dashboard', subtitle: 'Real-time academic performance, demographics and placement metrics' },
    query: { title: 'AI Natural Language Assistant', subtitle: 'Query college database in plain English with automated SQL generation & execution' },
    analytics: { title: 'Academic Analytics & Trends', subtitle: 'In-depth visualizations for department distribution, attendance and hiring packages' },
    history: { title: 'Query Audit Log & History', subtitle: 'Complete log of natural language questions, generated queries and execution times' },
    schema: { title: 'Database Schema & Tables', subtitle: 'Interactive catalog of available MySQL tables, columns, constraints and relations' },
  };

  const current = titles[activeTab] || titles.dashboard;

  return (
    <header style={{
      height: '70px',
      borderBottom: '1px solid var(--border-color)',
      backgroundColor: 'rgba(15, 23, 42, 0.75)',
      backdropFilter: 'blur(12px)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: '0 2rem',
      position: 'sticky',
      top: 0,
      zIndex: 10
    }}>
      <div>
        <h2 style={{ fontSize: '1.15rem', fontWeight: 700, color: 'var(--text-primary)', margin: 0 }}>
          {current.title}
        </h2>
        <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', margin: 0 }}>
          {current.subtitle}
        </p>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        {/* Live Backend Connection Indicator */}
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '0.5rem',
          padding: '0.35rem 0.75rem',
          borderRadius: '9999px',
          background: 'rgba(30, 41, 59, 0.8)',
          border: '1px solid var(--border-color)',
          fontSize: '0.75rem'
        }}>
          <span style={{
            width: '8px',
            height: '8px',
            borderRadius: '50%',
            backgroundColor: healthStatus?.database?.status === 'CONNECTED' ? '#10b981' : '#f59e0b',
            boxShadow: healthStatus?.database?.status === 'CONNECTED' ? '0 0 8px #10b981' : 'none'
          }} />
          <span style={{ color: 'var(--text-secondary)' }}>MySQL:</span>
          <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>
            {healthStatus?.database?.status || 'Connecting...'}
          </span>
        </div>

        {/* AI Model Tag */}
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '0.4rem',
          padding: '0.35rem 0.75rem',
          borderRadius: '9999px',
          background: 'rgba(99, 102, 241, 0.12)',
          border: '1px solid rgba(99, 102, 241, 0.3)',
          fontSize: '0.75rem',
          color: 'var(--accent-blue)',
          fontWeight: 600
        }}>
          <Sparkles size={13} />
          <span>{healthStatus?.aiService?.model || 'gpt-4o-mini'}</span>
        </div>

        {/* Refresh button */}
        {onRefresh && (
          <button 
            className="btn btn-ghost" 
            onClick={onRefresh}
            title="Refresh Data"
            style={{ padding: '0.5rem', borderRadius: '50%' }}
          >
            <RefreshCw size={16} className={isRefreshing ? 'animate-spin' : ''} />
          </button>
        )}
      </div>
    </header>
  );
}
