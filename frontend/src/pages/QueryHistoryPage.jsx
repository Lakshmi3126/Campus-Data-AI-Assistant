
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { api } from '../services/api';

function QueryHistoryPage() {
  const [history, setHistory] = useState([]);
  const [selectedRecord, setSelectedRecord] = useState(null);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [loading, setLoading] = useState(true);
  const [detailsLoading, setDetailsLoading] = useState(false);
  const [error, setError] = useState('');

  const fetchHistory = useCallback(async () => {
    setLoading(true);
    setError('');

    try {
      const data = await api.getHistory(100);
      setHistory(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || 'Failed to load query history.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchHistory();
  }, [fetchHistory]);

  const filteredHistory = useMemo(() => {
    return history.filter((item) => {
      const matchesSearch =
        (item.question || '').toLowerCase().includes(search.toLowerCase()) ||
        (item.generatedSql || '').toLowerCase().includes(search.toLowerCase());

      const matchesStatus =
        statusFilter === 'ALL' ||
        (item.executionStatus || '').toUpperCase() === statusFilter;

      return matchesSearch && matchesStatus;
    });
  }, [history, search, statusFilter]);

  const openDetails = async (id) => {
    setDetailsLoading(true);
    setSelectedRecord(null);

    try {
      const record = await api.getHistoryById(id);
      setSelectedRecord(record);
    } catch (err) {
      setError(err.message || 'Failed to load query details.');
    } finally {
      setDetailsLoading(false);
    }
  };

  const formatDate = (date) => {
    if (!date) return '—';

    const parsed = new Date(date);
    return Number.isNaN(parsed.getTime())
      ? '—'
      : parsed.toLocaleString();
  };

  const getStatusStyle = (status) => {
    const normalized = (status || '').toUpperCase();

    if (normalized === 'SUCCESS') {
      return { color: '#34d399', background: 'rgba(16,185,129,0.12)' };
    }

    if (normalized === 'FAILED' || normalized === 'VALIDATION_FAILED') {
      return { color: '#f87171', background: 'rgba(239,68,68,0.12)' };
    }

    return { color: '#fbbf24', background: 'rgba(245,158,11,0.12)' };
  };

  const closeDetails = () => setSelectedRecord(null);

  const cardStyle = {
    background: 'var(--card-bg, #1b273a)',
    border: '1px solid var(--border-color, #334155)',
    borderRadius: '14px',
    padding: '20px',
  };

  const mutedText = {
    color: 'var(--text-secondary, #94a3b8)',
  };

  return (
    <div className="page-wrapper">
      <div style={{ ...cardStyle, marginBottom: '20px' }}>
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          gap: '16px',
          flexWrap: 'wrap',
        }}>
          <div>
            <h2 style={{ margin: 0, fontSize: '24px' }}>
              Query History
            </h2>
            <p style={{ ...mutedText, marginBottom: 0 }}>
              Review your previous questions and SQL executions.
            </p>
          </div>

          <button
            onClick={fetchHistory}
            disabled={loading}
            style={{
              padding: '10px 16px',
              borderRadius: '9px',
              border: 'none',
              background: '#625bff',
              color: '#fff',
              cursor: loading ? 'not-allowed' : 'pointer',
            }}
          >
            {loading ? 'Refreshing...' : '↻ Refresh'}
          </button>
        </div>

        <div style={{
          display: 'flex',
          gap: '12px',
          marginTop: '22px',
          flexWrap: 'wrap',
        }}>
          <input
            type="text"
            placeholder="Search questions or SQL..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            style={{
              flex: '1 1 240px',
              minWidth: 0,
              padding: '12px',
              borderRadius: '9px',
              border: '1px solid #334155',
              background: '#111827',
              color: '#fff',
            }}
          />

          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            style={{
              padding: '12px',
              borderRadius: '9px',
              border: '1px solid #334155',
              background: '#111827',
              color: '#fff',
            }}
          >
            <option value="ALL">All statuses</option>
            <option value="SUCCESS">Success</option>
            <option value="FAILED">Failed</option>
            <option value="REJECTED">Rejected</option>
            <option value="VALIDATION_FAILED">Validation failed</option>
          </select>
        </div>
      </div>

      {error && (
        <div style={{
          ...cardStyle,
          color: '#f87171',
          marginBottom: '16px',
        }}>
          {error}
        </div>
      )}

      <div style={cardStyle}>
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          marginBottom: '16px',
          gap: '12px',
        }}>
          <h3 style={{ margin: 0 }}>Previous Queries</h3>
          <span style={mutedText}>
            {filteredHistory.length} records
          </span>
        </div>

        {loading ? (
          <p style={mutedText}>Loading query history...</p>
        ) : filteredHistory.length === 0 ? (
          <p style={mutedText}>
            {history.length === 0
              ? 'No query history found yet. Run a query to see it here.'
              : 'No records match your search or status filter.'}
          </p>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{
              width: '100%',
              borderCollapse: 'collapse',
              minWidth: '760px',
              textAlign: 'left',
            }}>
              <thead>
                <tr style={{
                  color: '#94a3b8',
                  borderBottom: '1px solid #334155',
                }}>
                  {['Question', 'Status', 'Rows', 'Execution Time', 'Date', ''].map(
                    (heading, index) => (
                      <th key={index} style={{ padding: '14px 12px' }}>
                        {heading}
                      </th>
                    )
                  )}
                </tr>
              </thead>

              <tbody>
                {filteredHistory.map((item) => (
                  <tr
                    key={item.id}
                    style={{ borderBottom: '1px solid #293548' }}
                  >
                    <td style={{
                      padding: '14px 12px',
                      maxWidth: '340px',
                    }}>
                      <div style={{
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        whiteSpace: 'nowrap',
                      }}>
                        {item.question || 'Untitled query'}
                      </div>
                    </td>

                    <td style={{ padding: '14px 12px' }}>
                      <span style={{
                        ...getStatusStyle(item.executionStatus),
                        padding: '5px 9px',
                        borderRadius: '20px',
                        fontSize: '12px',
                        fontWeight: 600,
                        whiteSpace: 'nowrap',
                      }}>
                        {item.executionStatus || 'UNKNOWN'}
                      </span>
                    </td>

                    <td style={{ padding: '14px 12px' }}>
                      {item.rowCount ?? 0}
                    </td>

                    <td style={{ padding: '14px 12px' }}>
                      {item.executionTimeMs ?? 0} ms
                    </td>

                    <td style={{
                      padding: '14px 12px',
                      whiteSpace: 'nowrap',
                      ...mutedText,
                    }}>
                      {formatDate(item.createdAt)}
                    </td>

                    <td style={{ padding: '14px 12px' }}>
                      <button
                        onClick={() => openDetails(item.id)}
                        style={{
                          background: 'transparent',
                          border: '1px solid #625bff',
                          color: '#a5a0ff',
                          borderRadius: '7px',
                          padding: '7px 11px',
                          cursor: 'pointer',
                          whiteSpace: 'nowrap',
                        }}
                      >
                        View details
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {(detailsLoading || selectedRecord) && (
        <div
          onClick={closeDetails}
          style={{
            position: 'fixed',
            inset: 0,
            zIndex: 1000,
            background: 'rgba(0,0,0,0.65)',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            padding: '20px',
          }}
        >
          <div
            onClick={(e) => e.stopPropagation()}
            style={{
              ...cardStyle,
              width: 'min(800px, 100%)',
              maxHeight: '85vh',
              overflowY: 'auto',
            }}
          >
            <div style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
            }}>
              <h3>Query Details</h3>
              <button onClick={closeDetails}>✕</button>
            </div>

            {detailsLoading ? (
              <p style={mutedText}>Loading details...</p>
            ) : selectedRecord && (
              <>
                <p style={mutedText}>Original question</p>
                <p>{selectedRecord.question}</p>

                <p style={mutedText}>Generated SQL</p>
                <pre style={{
                  background: '#080e19',
                  padding: '16px',
                  borderRadius: '9px',
                  overflowX: 'auto',
                  whiteSpace: 'pre-wrap',
                  overflowWrap: 'anywhere',
                  color: '#c4b5fd',
                }}>
                  {selectedRecord.generatedSql || 'No SQL available.'}
                </pre>

                <p style={mutedText}>Explanation</p>
                <p>{selectedRecord.explanation || 'No explanation available.'}</p>

                {selectedRecord.errorMessage && (
                  <>
                    <p style={{ color: '#f87171' }}>Error message</p>
                    <p>{selectedRecord.errorMessage}</p>
                  </>
                )}

                <p style={mutedText}>
                  Status: {selectedRecord.executionStatus || 'UNKNOWN'}
                  {' · '}
                  Rows: {selectedRecord.rowCount ?? 0}
                  {' · '}
                  Time: {selectedRecord.executionTimeMs ?? 0} ms
                </p>

                <p style={mutedText}>
                  {formatDate(selectedRecord.createdAt)}
                </p>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default QueryHistoryPage;
