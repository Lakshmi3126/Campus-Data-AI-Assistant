
import React, { useCallback, useEffect, useState } from 'react';

import Header from './components/Header';
import Sidebar from './components/Sidebar';
import DashboardPage from './pages/DashboardPage';
import QueryAssistantPage from './pages/QueryAssistantPage';
import { api } from './services/api';
import QueryHistoryPage from './pages/QueryHistoryPage';

function App() {
  const [activeTab, setActiveTab] = useState('dashboard');
  const [initialQuery, setInitialQuery] = useState('');
  const [healthStatus, setHealthStatus] = useState(null);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [dashboardKey, setDashboardKey] = useState(0);

  const refreshData = useCallback(async () => {
    setIsRefreshing(true);

    try {
      const health = await api.getHealth();
      setHealthStatus(health);
      setDashboardKey((previous) => previous + 1);
    } catch (error) {
      console.error('Backend health check failed:', error);
      setHealthStatus({
        database: { status: 'DISCONNECTED' },
        aiService: { model: 'gpt-4o-mini' }
      });
    } finally {
      setIsRefreshing(false);
    }
  }, []);

  useEffect(() => {
    refreshData();
  }, [refreshData]);

  const handleSelectExample = (prompt) => {
    setInitialQuery(prompt);
  };

  const renderPage = () => {
    switch (activeTab) {
      case 'dashboard':
        return (
          <DashboardPage
            key={dashboardKey}
            setActiveTab={setActiveTab}
            onSelectExample={handleSelectExample}
          />
        );

      case 'query':
        return <QueryAssistantPage initialQuery={initialQuery} />;

      case 'history':
        return <QueryHistoryPage />;

        case 'analytics':
        case 'schema':
        return (
            <div className="page-wrapper">
            <div className="glass-card" style={{ padding: '2rem' }}>
                <h2 style={{ marginBottom: '0.75rem' }}>
                {activeTab === 'analytics'
                    ? 'Academic Analytics'
                    : 'Database Schema'}
                </h2>
                <p style={{ color: 'var(--text-secondary)' }}>
                This page has not yet been implemented.
                </p>
            </div>
            </div>
        );
      

      default:
        return <DashboardPage setActiveTab={setActiveTab} />;
    }
  };

  return (
    <div className="app-container">
      <Sidebar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
      />

      <main className="main-content">
        <Header
          activeTab={activeTab}
          healthStatus={healthStatus}
          onRefresh={refreshData}
          isRefreshing={isRefreshing}
        />

        {renderPage()}
      </main>
    </div>
  );
}

export default App;