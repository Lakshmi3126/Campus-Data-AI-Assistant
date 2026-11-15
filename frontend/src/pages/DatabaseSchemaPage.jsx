
import React, { useEffect, useMemo, useState } from 'react';
import { api } from '../services/api';

function DatabaseSchemaPage() {
  const [tables, setTables] = useState([]);
  const [search, setSearch] = useState('');
  const [expandedTable, setExpandedTable] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchSchema = async () => {
    try {
      setLoading(true);
      setError('');

      const response = await api.getSchema();
      setTables(response.tables || []);
    } catch (err) {
      setError(err.message || 'Failed to load database schema.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSchema();
  }, []);

  const filteredTables = useMemo(() => {
    const term = search.toLowerCase().trim();

    if (!term) return tables;

    return tables.filter((table) =>
      table.tableName.toLowerCase().includes(term) ||
      table.description?.toLowerCase().includes(term) ||
      table.columns?.some((column) =>
        column.columnName.toLowerCase().includes(term)
      )
    );
  }, [tables, search]);

  if (loading) {
    return (
      <div className="page-wrapper">
        <div className="glass-card" style={{ padding: '2rem' }}>
          Loading database schema...
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="page-wrapper">
        <div className="glass-card" style={{ padding: '2rem' }}>
          <p style={{ color: '#ff7777' }}>{error}</p>
          <button onClick={fetchSchema}>Try Again</button>
        </div>
      </div>
    );
  }

  return (
    <div className="page-wrapper">
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        gap: '1rem',
        flexWrap: 'wrap',
        marginBottom: '1.5rem'
      }}>
        <div>
          <h1>Database Schema</h1>
          <p style={{ color: 'var(--text-secondary)' }}>
            Explore tables, columns, data types and relationships.
          </p>
        </div>

        <button onClick={fetchSchema}>↻ Refresh</button>
      </div>

      <div className="glass-card" style={{ padding: '1.25rem', marginBottom: '1.5rem' }}>
        <div style={{
          display: 'flex',
          gap: '1rem',
          flexWrap: 'wrap'
        }}>
          <div>
            <h2>{tables.length}</h2>
            <p style={{ color: 'var(--text-secondary)' }}>Tables</p>
          </div>

          <div>
            <h2>
              {tables.reduce((total, table) => total + (table.columns?.length || 0), 0)}
            </h2>
            <p style={{ color: 'var(--text-secondary)' }}>Columns</p>
          </div>

          <div>
            <h2>
              {tables.reduce((total, table) => total + (table.foreignKeys?.length || 0), 0)}
            </h2>
            <p style={{ color: 'var(--text-secondary)' }}>Relationships</p>
          </div>
        </div>
      </div>

      <input
        type="text"
        placeholder="Search tables or columns..."
        value={search}
        onChange={(e) => setSearch(e.target.value)}
        style={{
          width: '100%',
          padding: '0.9rem 1rem',
          marginBottom: '1.5rem',
          borderRadius: '10px',
          border: '1px solid var(--border-color, #334155)',
          background: 'var(--card-bg, #1e293b)',
          color: 'var(--text-primary, white)'
        }}
      />

      <div style={{ display: 'grid', gap: '1rem' }}>
        {filteredTables.map((table) => {
          const isExpanded = expandedTable === table.tableName;

          return (
            <div className="glass-card" key={table.tableName}>
              <div
                onClick={() =>
                  setExpandedTable(isExpanded ? null : table.tableName)
                }
                style={{
                  padding: '1.25rem',
                  cursor: 'pointer',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  gap: '1rem'
                }}
              >
                <div>
                  <h3>{table.tableName}</h3>
                  <p style={{
                    color: 'var(--text-secondary)',
                    marginTop: '0.35rem'
                  }}>
                    {table.description}
                  </p>
                  <small style={{ color: 'var(--text-secondary)' }}>
                    {table.columns?.length || 0} columns ·
                    {' '}{table.foreignKeys?.length || 0} foreign keys
                  </small>
                </div>

                <span>{isExpanded ? '▲' : '▼'}</span>
              </div>

              {isExpanded && (
                <div style={{ padding: '0 1.25rem 1.25rem' }}>
                  <h4 style={{ marginBottom: '0.75rem' }}>Columns</h4>

                  <div style={{ overflowX: 'auto' }}>
                    <table style={{
                      width: '100%',
                      borderCollapse: 'collapse',
                      textAlign: 'left'
                    }}>
                      <thead>
                        <tr>
                          {['Column', 'Data Type', 'Properties', 'Description'].map((heading) => (
                            <th key={heading} style={{
                              padding: '0.8rem',
                              borderBottom: '1px solid var(--border-color, #334155)',
                              color: 'var(--text-secondary)'
                            }}>
                              {heading}
                            </th>
                          ))}
                        </tr>
                      </thead>

                      <tbody>
                        {table.columns?.map((column) => (
                          <tr key={column.columnName}>
                            <td style={{ padding: '0.8rem' }}>
                              <strong>{column.columnName}</strong>
                            </td>

                            <td style={{ padding: '0.8rem' }}>
                              <code>{column.dataType}</code>
                            </td>

                            <td style={{ padding: '0.8rem' }}>
                              {column.primaryKey && (
                                <span style={{ color: '#fbbf24' }}>PRIMARY KEY </span>
                              )}
                              {column.nullable ? 'NULL' : 'NOT NULL'}
                            </td>

                            <td style={{
                              padding: '0.8rem',
                              color: 'var(--text-secondary)'
                            }}>
                              {column.description}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  {table.foreignKeys?.length > 0 && (
                    <div style={{ marginTop: '1.5rem' }}>
                      <h4>Relationships</h4>

                      {table.foreignKeys.map((fk, index) => (
                        <p key={index} style={{
                          padding: '0.65rem 0',
                          color: 'var(--text-secondary)'
                        }}>
                          {table.tableName}.{fk.columnName}
                          {' → '}
                          {fk.referencedTable}.{fk.referencedColumn}
                        </p>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </div>
          );
        })}

        {filteredTables.length === 0 && (
          <div className="glass-card" style={{ padding: '2rem' }}>
            No matching tables or columns found.
          </div>
        )}
      </div>
    </div>
  );
}

export default DatabaseSchemaPage;
