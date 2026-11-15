import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { 
  Users, 
  Building2, 
  Award, 
  Briefcase, 
  TrendingUp, 
  CheckCircle2, 
  Clock, 
  ArrowRight,
  Sparkles
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

export default function DashboardPage({ setActiveTab, onSelectExample }) {
  const [stats, setStats] = useState(null);
  const [deptStats, setDeptStats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      setError(null);
      const [dashData, deptData] = await Promise.all([
        api.getDashboardStats(),
        api.getDepartmentAnalytics()
      ]);
      setStats(dashData);
      setDeptStats(deptData);
    } catch (err) {
      setError(err.message || 'Failed to load dashboard statistics.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="page-wrapper" style={{ alignItems: 'center', justifyContent: 'center', minHeight: '60vh' }}>
        <div style={{ textAlign: 'center' }}>
          <div className="animate-spin" style={{ width: '40px', height: '40px', border: '3px solid var(--accent-indigo)', borderTopColor: 'transparent', borderRadius: '50%', margin: '0 auto 1rem' }} />
          <p style={{ color: 'var(--text-secondary)' }}>Loading campus metrics directly from MySQL...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="page-wrapper">
        <div className="glass-card" style={{ padding: '2rem', textAlign: 'center', borderColor: 'var(--accent-rose)' }}>
          <p style={{ color: '#fb7185', fontWeight: 600, marginBottom: '1rem' }}>{error}</p>
          <button className="btn btn-secondary" onClick={loadDashboardData}>Retry</button>
        </div>
      </div>
    );
  }

  const kpis = [
    { label: 'Total Students', value: stats?.totalStudents, icon: Users, color: '#38bdf8', sub: 'Across 6 faculties' },
    { label: 'Departments', value: stats?.totalDepartments, icon: Building2, color: '#6366f1', sub: 'Engineering & Technology' },
    { label: 'Average CGPA', value: stats?.averageCgpa ? `${stats.averageCgpa} / 10` : 'N/A', icon: Award, color: '#10b981', sub: 'Cumulative collegiate average' },
    { label: 'Placement Rate', value: `${stats?.placementRate || 0}%`, icon: Briefcase, color: '#f59e0b', sub: `${stats?.totalPlaced || 0} eligible candidates placed` },
    { label: 'Highest Package', value: `${stats?.highestPackageLpa || 0} LPA`, icon: TrendingUp, color: '#8b5cf6', sub: 'Top campus recruitment offer' },
    { label: 'Average Package', value: `${stats?.averagePackageLpa || 0} LPA`, icon: CheckCircle2, color: '#14b8a6', sub: 'Median recruitment tier' },
  ];

  const quickPrompts = [
    "Show all students belonging to the CSE department.",
    "Find students whose attendance is below 75%.",
    "What is the average CGPA of CSE students?",
    "Show the top 10 students based on CGPA."
  ];

  return (
    <div className="page-wrapper animate-fade-in">
      {/* Welcome Banner */}
      <div className="glass-card" style={{
        padding: '1.75rem 2rem',
        background: 'linear-gradient(135deg, rgba(30, 41, 59, 0.8), rgba(15, 23, 42, 0.9))',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        flexWrap: 'wrap',
        gap: '1.5rem'
      }}>
        <div>
          <div className="badge badge-info" style={{ marginBottom: '0.5rem' }}>AI-Powered Analytics</div>
          <h2 style={{ fontSize: '1.5rem', fontWeight: 800, letterSpacing: '-0.02em', marginBottom: '0.4rem' }}>
            College Academic & Placement Intelligence
          </h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', maxWidth: '650px' }}>
            Interact with your campus MySQL database using natural language. No manual SQL syntax needed.
          </p>
        </div>
        <button 
          className="btn btn-primary"
          onClick={() => setActiveTab('query')}
          style={{ padding: '0.75rem 1.5rem', fontSize: '0.95rem' }}
        >
          <Sparkles size={18} />
          <span>Ask AI Assistant</span>
          <ArrowRight size={16} />
        </button>
      </div>

      {/* KPI Cards Grid */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(210px, 1fr))',
        gap: '1.25rem'
      }}>
        {kpis.map((kpi, idx) => {
          const Icon = kpi.icon;
          return (
            <div key={idx} className="glass-card" style={{ padding: '1.35rem 1.5rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.75rem' }}>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', fontWeight: 600 }}>{kpi.label}</span>
                <div style={{ 
                  padding: '0.45rem', 
                  borderRadius: 'var(--radius-sm)', 
                  backgroundColor: `${kpi.color}15`,
                  color: kpi.color 
                }}>
                  <Icon size={18} />
                </div>
              </div>
              <div style={{ fontSize: '1.65rem', fontWeight: 800, color: 'var(--text-primary)', marginBottom: '0.25rem' }}>
                {kpi.value}
              </div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                {kpi.sub}
              </div>
            </div>
          );
        })}
      </div>

      {/* Chart & Quick Prompts Row */}
      <div style={{ display: 'grid', gridTemplateColumns: '1.8fr 1.2fr', gap: '1.5rem' }}>
        {/* Department Distribution Chart */}
        <div className="glass-card" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
            <div>
              <h3 style={{ fontSize: '1rem', fontWeight: 700, margin: 0 }}>Students per Department</h3>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Enrolled student counts from database</span>
            </div>
            <span className="badge badge-success">Live MySQL</span>
          </div>

          <div style={{ height: '260px', width: '100%' }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={deptStats} margin={{ top: 10, right: 10, left: -20, bottom: 20 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(148, 163, 184, 0.1)" />
                <XAxis dataKey="departmentCode" stroke="var(--text-muted)" fontSize={12} tickLine={false} />
                <YAxis stroke="var(--text-muted)" fontSize={12} tickLine={false} allowDecimals={false} />
                <Tooltip 
                  contentStyle={{ backgroundColor: 'var(--bg-secondary)', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-sm)' }}
                  labelStyle={{ color: 'var(--text-primary)', fontWeight: 600 }}
                  formatter={(val) => [`${val} Students`, 'Total']}
                />
                <Bar dataKey="studentCount" fill="#6366f1" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Quick Example Questions */}
        <div className="glass-card" style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
          <div>
            <h3 style={{ fontSize: '1rem', fontWeight: 700, marginBottom: '0.25rem' }}>Try Natural Language Queries</h3>
            <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
              Click any question to instantly execute and analyze via AI:
            </p>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.65rem' }}>
              {quickPrompts.map((prompt, i) => (
                <button
                  key={i}
                  onClick={() => {
                    if (onSelectExample) onSelectExample(prompt);
                    setActiveTab('query');
                  }}
                  style={{
                    background: 'rgba(15, 23, 42, 0.6)',
                    border: '1px solid var(--border-color)',
                    borderRadius: 'var(--radius-md)',
                    padding: '0.75rem 1rem',
                    color: 'var(--text-primary)',
                    fontSize: '0.825rem',
                    textAlign: 'left',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    gap: '0.5rem',
                    transition: 'all 0.15s ease'
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.borderColor = 'var(--accent-indigo)';
                    e.currentTarget.style.backgroundColor = 'rgba(99, 102, 241, 0.08)';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.borderColor = 'var(--border-color)';
                    e.currentTarget.style.backgroundColor = 'rgba(15, 23, 42, 0.6)';
                  }}
                >
                  <span>"{prompt}"</span>
                  <ArrowRight size={14} color="var(--accent-blue)" />
                </button>
              ))}
            </div>
          </div>

          <div style={{ marginTop: '1rem', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
            💡 Contextual follow-up questions like <em>"Now show only students with CGPA above 8.5"</em> are supported.
          </div>
        </div>
      </div>
    </div>
  );
}
