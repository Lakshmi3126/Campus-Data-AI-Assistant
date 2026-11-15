import React, { useState, useEffect, useRef } from 'react';
import { api } from '../services/api';
import { 
  Sparkles, 
  Send, 
  Copy, 
  Check, 
  Terminal, 
  FileText, 
  Table, 
  BarChart2, 
  Download, 
  AlertCircle, 
  Clock, 
  Layers,
  ChevronRight,
  Info
} from 'lucide-react';
import { 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  Tooltip, 
  ResponsiveContainer, 
  CartesianGrid 
} from 'recharts';

export default function QueryAssistantPage({ initialQuery }) {
  const [question, setQuestion] = useState(initialQuery || '');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [history, setHistory] = useState([]);
  const [copiedIndex, setCopiedIndex] = useState(null);
  const [activeViewMode, setActiveViewMode] = useState('table'); // 'table' or 'chart'
  const [searchTerm, setSearchTerm] = useState('');
  const chatBottomRef = useRef(null);

  // Conversational state
  const [conversationId, setConversationId] = useState(null);
  const [lastSql, setLastSql] = useState(null);
  const [lastQuestion, setLastQuestion] = useState(null);

  useEffect(() => {
    if (initialQuery) {
      setQuestion(initialQuery);
    }
  }, [initialQuery]);

  useEffect(() => {
    chatBottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [history, loading]);

  const exampleQuestions = [
    "Show all students belonging to the CSE department.",
    "Find students whose attendance is below 75%.",
    "What is the average CGPA of CSE students?",
    "Show the top 10 students based on CGPA.",
    "Which department has the highest number of students?",
    "Find students eligible for placements.",
    "Show the average marks for each department."
  ];

  const handleSubmit = async (e, overridePrompt) => {
    if (e) e.preventDefault();
    const queryToSubmit = overridePrompt || question;
    if (!queryToSubmit.trim() || loading) return;

    setError(null);
    setLoading(true);

    const userMessage = {
      role: 'user',
      text: queryToSubmit,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };

    setHistory((prev) => [...prev, userMessage]);
    setQuestion('');

    try {
      const response = await api.submitQuery({
        question: queryToSubmit,
        conversationId,
        previousQuestion: lastQuestion,
        previousSql: lastSql,
      });

      const assistantMessage = {
        role: 'assistant',
        data: response,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };

      setHistory((prev) => [...prev, assistantMessage]);

      // Update conversational context
      if (response.conversationId) setConversationId(response.conversationId);
      if (response.generatedSql) setLastSql(response.generatedSql);
      setLastQuestion(queryToSubmit);

    } catch (err) {
      setError(err.message || 'Failed to process natural language query.');
      const errorMessage = {
        role: 'assistant',
        isError: true,
        errorText: err.message,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };
      setHistory((prev) => [...prev, errorMessage]);
    } finally {
      setLoading(false);
    }
  };

  const handleCopySql = (sql, index) => {
    navigator.clipboard.writeText(sql);
    setCopiedIndex(index);
    setTimeout(() => setCopiedIndex(null), 2000);
  };

  const exportToCsv = (columns, rows, filename = 'campus_query_results.csv') => {
    if (!rows || rows.length === 0) return;
    const header = columns.join(',');
    const csvRows = rows.map(r => columns.map(c => `"${r[c] !== null && r[c] !== undefined ? r[c] : ''}"`).join(','));
    const csvContent = "data:text/csv;charset=utf-8," + [header, ...csvRows].join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", filename);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  // Helper to determine if results can be charted (e.g. 1 string + 1 number column)
  const getChartableData = (columns, rows) => {
    if (!rows || rows.length === 0 || !columns || columns.length < 2) return null;
    let labelCol = columns.find(c => typeof rows[0][c] === 'string') || columns[0];
    let numCol = columns.find(c => typeof rows[0][c] === 'number') || columns[1];
    
    if (typeof rows[0][numCol] !== 'number') return null;

    return {
      labelKey: labelCol,
      valueKey: numCol,
      data: rows.slice(0, 15).map(r => ({
        label: String(r[labelCol] || 'N/A'),
        value: Number(r[numCol] || 0)
      }))
    };
  };

  return (
    <div className="page-wrapper animate-fade-in" style={{ paddingBottom: '3rem' }}>
      {/* Example Prompts Shelf */}
      <div className="glass-card" style={{ padding: '1.25rem 1.5rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.75rem' }}>
          <Sparkles size={16} color="var(--accent-indigo)" />
          <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-secondary)' }}>
            Suggested Questions:
          </span>
        </div>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
          {exampleQuestions.map((prompt, idx) => (
            <button
              key={idx}
              onClick={() => {
                setQuestion(prompt);
                handleSubmit(null, prompt);
              }}
              disabled={loading}
              style={{
                background: 'rgba(15, 23, 42, 0.7)',
                border: '1px solid var(--border-color)',
                borderRadius: '9999px',
                padding: '0.4rem 0.85rem',
                fontSize: '0.8rem',
                color: 'var(--text-primary)',
                cursor: 'pointer',
                transition: 'all 0.15s ease'
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.borderColor = 'var(--accent-blue)';
                e.currentTarget.style.backgroundColor = 'rgba(56, 189, 248, 0.08)';
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.borderColor = 'var(--border-color)';
                e.currentTarget.style.backgroundColor = 'rgba(15, 23, 42, 0.7)';
              }}
            >
              {prompt}
            </button>
          ))}
        </div>
      </div>

      {/* Conversation Stream */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
        {history.length === 0 && (
          <div className="glass-card" style={{ padding: '3.5rem 2rem', textAlign: 'center' }}>
            <div style={{
              width: '64px',
              height: '64px',
              borderRadius: '50%',
              background: 'rgba(99, 102, 241, 0.1)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              margin: '0 auto 1.25rem',
              color: 'var(--accent-indigo)'
            }}>
              <Terminal size={32} />
            </div>
            <h3 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '0.5rem' }}>
              How can I assist you with college database queries?
            </h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', maxWidth: '520px', margin: '0 auto 1.5rem' }}>
              Type any question about students, departments, CGPAs, attendance percentages, exam marks, or placements in plain English.
            </p>
          </div>
        )}

        {history.map((msg, index) => {
          if (msg.role === 'user') {
            return (
              <div key={index} style={{
                display: 'flex',
                justifyContent: 'flex-end',
              }}>
                <div style={{
                  maxWidth: '75%',
                  padding: '1rem 1.35rem',
                  borderRadius: 'var(--radius-lg) var(--radius-lg) 4px var(--radius-lg)',
                  background: 'linear-gradient(135deg, var(--accent-indigo), #4f46e5)',
                  color: '#fff',
                  boxShadow: '0 4px 14px rgba(99, 102, 241, 0.25)',
                }}>
                  <p style={{ fontSize: '0.925rem', fontWeight: 500, lineHeight: 1.5, margin: 0 }}>
                    {msg.text}
                  </p>
                  <span style={{ fontSize: '0.65rem', opacity: 0.75, display: 'block', textAlign: 'right', marginTop: '0.35rem' }}>
                    {msg.timestamp}
                  </span>
                </div>
              </div>
            );
          }

          if (msg.isError) {
            return (
              <div key={index} className="glass-card" style={{
                padding: '1.25rem 1.5rem',
                borderColor: 'var(--accent-rose)',
                background: 'rgba(244, 63, 94, 0.05)'
              }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#fb7185', fontWeight: 600, marginBottom: '0.4rem' }}>
                  <AlertCircle size={18} />
                  <span>Execution or Validation Error</span>
                </div>
                <p style={{ color: 'var(--text-primary)', fontSize: '0.85rem', margin: 0 }}>
                  {msg.errorText}
                </p>
              </div>
            );
          }

          const { data } = msg;
          const chartable = getChartableData(data.columns, data.rows);

          return (
            <div key={index} className="glass-card" style={{ padding: '1.75rem', display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
              {/* Header Status & Timing */}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.75rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
                  <span className="badge badge-success">
                    {data.status || 'SUCCESS'}
                  </span>
                  <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                    Type: <strong style={{ color: 'var(--text-primary)' }}>{data.queryType || 'SELECT'}</strong>
                  </span>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                  <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                    <Clock size={14} />
                    {data.executionTimeMs} ms
                  </span>
                  <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                    <Layers size={14} />
                    {data.rowCount} rows retrieved
                  </span>
                </div>
              </div>

              {/* SQL Explanation */}
              {data.explanation && (
                <div style={{
                  padding: '0.85rem 1rem',
                  borderRadius: 'var(--radius-md)',
                  background: 'rgba(56, 189, 248, 0.08)',
                  border: '1px solid rgba(56, 189, 248, 0.2)',
                  display: 'flex',
                  alignItems: 'flex-start',
                  gap: '0.65rem'
                }}>
                  <Info size={18} color="var(--accent-blue)" style={{ flexShrink: 0, marginTop: '2px' }} />
                  <div style={{ fontSize: '0.85rem', color: 'var(--text-primary)', lineHeight: 1.5 }}>
                    <strong>Query Analysis:</strong> {data.explanation}
                  </div>
                </div>
              )}

              {/* Generated SQL Code Box */}
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.4rem' }}>
                  <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', fontWeight: 600 }}>
                    VALIDATED MYSQL QUERY
                  </span>
                  <button
                    className="btn btn-ghost"
                    onClick={() => handleCopySql(data.generatedSql, index)}
                    style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem' }}
                  >
                    {copiedIndex === index ? (
                      <>
                        <Check size={13} color="var(--accent-emerald)" />
                        <span style={{ color: 'var(--accent-emerald)' }}>Copied</span>
                      </>
                    ) : (
                      <>
                        <Copy size={13} />
                        <span>Copy SQL</span>
                      </>
                    )}
                  </button>
                </div>

                <pre className="code-block">
                  <code>{data.generatedSql}</code>
                </pre>
              </div>

              {/* AI Natural Language Summary */}
              {data.summary && (
                <div style={{
                  padding: '1rem',
                  borderRadius: 'var(--radius-md)',
                  background: 'rgba(99, 102, 241, 0.08)',
                  border: '1px solid rgba(99, 102, 241, 0.25)',
                }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: 'var(--accent-indigo)', fontWeight: 700, fontSize: '0.825rem', marginBottom: '0.35rem' }}>
                    <Sparkles size={15} />
                    <span>AI Result Summary</span>
                  </div>
                  <p style={{ fontSize: '0.875rem', color: 'var(--text-primary)', margin: 0, lineHeight: 1.5 }}>
                    {data.summary}
                  </p>
                </div>
              )}

              {/* Data Table / Visualization Toggle & Actions */}
              {data.rows && data.rows.length > 0 && (
                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem', flexWrap: 'wrap', gap: '0.5rem' }}>
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                      <button
                        className={`btn ${activeViewMode === 'table' ? 'btn-primary' : 'btn-secondary'}`}
                        onClick={() => setActiveViewMode('table')}
                        style={{ padding: '0.4rem 0.85rem', fontSize: '0.8rem' }}
                      >
                        <Table size={14} />
                        <span>Table View</span>
                      </button>
                      {chartable && (
                        <button
                          className={`btn ${activeViewMode === 'chart' ? 'btn-primary' : 'btn-secondary'}`}
                          onClick={() => setActiveViewMode('chart')}
                          style={{ padding: '0.4rem 0.85rem', fontSize: '0.8rem' }}
                        >
                          <BarChart2 size={14} />
                          <span>Chart Preview</span>
                        </button>
                      )}
                    </div>

                    <button
                      className="btn btn-secondary"
                      onClick={() => exportToCsv(data.columns, data.rows)}
                      style={{ padding: '0.4rem 0.85rem', fontSize: '0.8rem' }}
                    >
                      <Download size={14} />
                      <span>Export CSV</span>
                    </button>
                  </div>

                  {activeViewMode === 'table' ? (
                    <div className="table-container">
                      <table className="data-table">
                        <thead>
                          <tr>
                            {data.columns.map((col, idx) => (
                              <th key={idx}>{col.replace(/_/g, ' ')}</th>
                            ))}
                          </tr>
                        </thead>
                        <tbody>
                          {data.rows.map((row, rowIdx) => (
                            <tr key={rowIdx}>
                              {data.columns.map((col, colIdx) => (
                                <td key={colIdx}>
                                  {row[col] !== null && row[col] !== undefined ? String(row[col]) : '—'}
                                </td>
                              ))}
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  ) : (
                    chartable && (
                      <div style={{ height: '280px', width: '100%', padding: '1rem', background: '#090d16', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-color)' }}>
                        <ResponsiveContainer width="100%" height="100%">
                          <BarChart data={chartable.data}>
                            <CartesianGrid strokeDasharray="3 3" stroke="rgba(148, 163, 184, 0.1)" />
                            <XAxis dataKey="label" stroke="var(--text-muted)" fontSize={11} />
                            <YAxis stroke="var(--text-muted)" fontSize={11} />
                            <Tooltip 
                              contentStyle={{ backgroundColor: 'var(--bg-secondary)', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-sm)' }}
                            />
                            <Bar dataKey="value" fill="#38bdf8" radius={[4, 4, 0, 0]} />
                          </BarChart>
                        </ResponsiveContainer>
                      </div>
                    )
                  )}
                </div>
              )}
            </div>
          );
        })}

        {loading && (
          <div className="glass-card" style={{ padding: '1.5rem', display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <div className="animate-spin" style={{ width: '24px', height: '24px', border: '3px solid var(--accent-indigo)', borderTopColor: 'transparent', borderRadius: '50%' }} />
            <div>
              <p style={{ fontWeight: 600, fontSize: '0.875rem', margin: 0 }}>
                Synthesizing SQL query & analyzing database records...
              </p>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                Verifying AST constraints and executing against MySQL
              </span>
            </div>
          </div>
        )}

        <div ref={chatBottomRef} />
      </div>

      {/* Persistent Bottom Input Bar */}
      <div style={{
        position: 'sticky',
        bottom: '1rem',
        marginTop: '1.5rem',
        zIndex: 20
      }}>
        <form onSubmit={handleSubmit} className="glass-card" style={{
          padding: '0.65rem 0.85rem',
          display: 'flex',
          alignItems: 'center',
          gap: '0.75rem',
          borderRadius: 'var(--radius-xl)',
          boxShadow: '0 8px 30px rgba(0, 0, 0, 0.45)',
          background: 'rgba(30, 41, 59, 0.95)',
          border: '1px solid rgba(99, 102, 241, 0.35)'
        }}>
          <input
            type="text"
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
            placeholder="Ask anything about students, CGPA, attendance, marks or placements... (e.g. 'Show CSE students')"
            disabled={loading}
            style={{
              flex: 1,
              background: 'transparent',
              border: 'none',
              outline: 'none',
              color: 'var(--text-primary)',
              fontFamily: 'var(--font-sans)',
              fontSize: '0.925rem',
              padding: '0.5rem 0.75rem'
            }}
          />
          <button
            type="submit"
            className="btn btn-primary"
            disabled={!question.trim() || loading}
            style={{ borderRadius: 'var(--radius-lg)', padding: '0.65rem 1.25rem' }}
          >
            <Send size={16} />
            <span>Ask</span>
          </button>
        </form>
      </div>
    </div>
  );
}
