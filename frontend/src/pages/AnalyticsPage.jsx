
import React, { useCallback, useEffect, useState } from 'react';
import { api } from '../services/api';

const formatNumber = (value, digits = 2) =>
  Number(value ?? 0).toFixed(digits);

function AnalyticsPage() {
  const [departments, setDepartments] = useState([]);
  const [attendance, setAttendance] = useState([]);
  const [placements, setPlacements] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadAnalytics = useCallback(async () => {
    setLoading(true);
    setError('');

    try {
      const [departmentData, attendanceData, placementData] =
        await Promise.all([
          api.getDepartmentAnalytics(),
          api.getAttendanceAnalytics(),
          api.getPlacementAnalytics(),
        ]);

      setDepartments(departmentData);
      setAttendance(attendanceData);
      setPlacements(placementData);
    } catch (err) {
      setError(err.message || 'Failed to load analytics.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadAnalytics();
  }, [loadAnalytics]);

  const totalStudents = departments.reduce(
    (sum, department) => sum + department.studentCount, 0
  );

  const totalPlaced = departments.reduce(
    (sum, department) => sum + department.placedCount, 0
  );

  const highestCgpa = Math.max(
    0,
    ...departments.map((department) =>
      Number(department.averageCgpa ?? 0)
    )
  );

  const totalAttendanceRecords = attendance.reduce(
    (sum, item) => sum + item.count, 0
  );

  const maxOffers = Math.max(
    1,
    ...placements.map((item) => item.offersCount)
  );

  const attendanceColors = ['#f87171', '#fbbf24', '#34d399'];

  if (loading) {
    return (
      <div className="page-wrapper">
        <div className="glass-card" style={{ padding: '2rem' }}>
          Loading academic analytics...
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="page-wrapper">
        <div className="glass-card" style={{ padding: '2rem' }}>
          <h2>Unable to load analytics</h2>
          <p style={{ color: 'var(--text-secondary)' }}>{error}</p>
          <button onClick={loadAnalytics}>Try again</button>
        </div>
      </div>
    );
  }

  return (
    <div className="page-wrapper">
      <div className="analytics-heading">
        <div>
          <h2>Academic Analytics</h2>
          <p>Explore department performance, attendance and placements.</p>
        </div>

        <button onClick={loadAnalytics} className="analytics-refresh">
          ↻ Refresh
        </button>
      </div>

      {/* Summary cards */}
      <div className="analytics-summary-grid">
        <div className="glass-card analytics-stat">
          <span>Total Students</span>
          <strong>{totalStudents}</strong>
        </div>

        <div className="glass-card analytics-stat">
          <span>Departments</span>
          <strong>{departments.length}</strong>
        </div>

        <div className="glass-card analytics-stat">
          <span>Placed Students</span>
          <strong>{totalPlaced}</strong>
        </div>

        <div className="glass-card analytics-stat">
          <span>Highest Department CGPA</span>
          <strong>{formatNumber(highestCgpa)}</strong>
        </div>
      </div>

      {/* Department analytics */}
      <section className="glass-card analytics-section">
        <h3>Department Performance</h3>
        <p className="analytics-subtitle">
          Student distribution and average CGPA by department
        </p>

        <div className="analytics-table-wrapper">
          <table className="analytics-table">
            <thead>
              <tr>
                <th>Department</th>
                <th>Students</th>
                <th>Average CGPA</th>
                <th>Placed Students</th>
              </tr>
            </thead>

            <tbody>
              {departments.map((department) => (
                <tr key={department.departmentId}>
                  <td>
                    <strong>{department.departmentName}</strong>
                    <small>{department.departmentCode}</small>
                  </td>
                  <td>{department.studentCount}</td>
                  <td>{formatNumber(department.averageCgpa)}</td>
                  <td>{department.placedCount}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {/* Attendance analytics */}
      <section className="glass-card analytics-section">
        <h3>Attendance Distribution</h3>
        <p className="analytics-subtitle">
          Distribution across {totalAttendanceRecords} attendance records
        </p>

        <div className="attendance-list">
          {attendance.map((item, index) => (
            <div className="attendance-item" key={item.bracket}>
              <div className="attendance-label">
                <span>
                  <i
                    style={{
                      backgroundColor: attendanceColors[index % attendanceColors.length],
                    }}
                  />
                  {item.bracket}
                </span>
                <strong>{formatNumber(item.percentage)}%</strong>
              </div>

              <div className="analytics-progress-track">
                <div
                  className="analytics-progress-fill"
                  style={{
                    width: `${Math.min(100, Math.max(0, item.percentage))}%`,
                    backgroundColor: attendanceColors[index % attendanceColors.length],
                  }}
                />
              </div>

              <small>{item.count} records</small>
            </div>
          ))}
        </div>
      </section>

      {/* Placement analytics */}
      <section className="glass-card analytics-section">
        <h3>Company-wise Placement Analytics</h3>
        <p className="analytics-subtitle">
          Hiring offers and package statistics by company
        </p>

        {placements.length === 0 ? (
          <p>No placement analytics available.</p>
        ) : (
          <div className="company-list">
            {placements.map((company) => (
              <div className="company-item" key={company.companyName}>
                <div className="company-details">
                  <strong>{company.companyName}</strong>
                  <span>
                    {company.offersCount} offers · Avg. ₹
                    {formatNumber(company.averagePackageLpa)} LPA · Max. ₹
                    {formatNumber(company.maxPackageLpa)} LPA
                  </span>
                </div>

                <div className="company-bar-track">
                  <div
                    className="company-bar-fill"
                    style={{
                      width: `${(company.offersCount / maxOffers) * 100}%`,
                    }}
                  />
                </div>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}

export default AnalyticsPage;
