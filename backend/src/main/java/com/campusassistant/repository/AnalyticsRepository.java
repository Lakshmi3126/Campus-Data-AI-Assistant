package com.campusassistant.repository;

import com.campusassistant.dto.AttendanceAnalyticsDTO;
import com.campusassistant.dto.DashboardDTO;
import com.campusassistant.dto.DepartmentAnalyticsDTO;
import com.campusassistant.dto.PlacementAnalyticsDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Repository
public class AnalyticsRepository {

    private final JdbcTemplate jdbcTemplate;

    public AnalyticsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DashboardDTO getDashboardStats() {
        DashboardDTO dto = new DashboardDTO();

        Long totalStudents = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM students", Long.class);
        dto.setTotalStudents(totalStudents != null ? totalStudents : 0L);

        Long totalDepts = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM departments", Long.class);
        dto.setTotalDepartments(totalDepts != null ? totalDepts : 0L);

        BigDecimal avgCgpa = jdbcTemplate.queryForObject("SELECT ROUND(AVG(cgpa), 2) FROM students", BigDecimal.class);
        dto.setAverageCgpa(avgCgpa != null ? avgCgpa : BigDecimal.ZERO);

        Long totalPlaced = jdbcTemplate.queryForObject("SELECT COUNT(DISTINCT student_id) FROM placements WHERE placement_status = 'Placed'", Long.class);
        dto.setTotalPlaced(totalPlaced != null ? totalPlaced : 0L);

        Long eligibleStudents = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM students WHERE semester IN (7, 8)", Long.class);
        if (eligibleStudents != null && eligibleStudents > 0 && totalPlaced != null) {
            double rate = ((double) totalPlaced / eligibleStudents) * 100.0;
            dto.setPlacementRate(BigDecimal.valueOf(rate).setScale(1, RoundingMode.HALF_UP).doubleValue());
        } else {
            dto.setPlacementRate(0.0);
        }

        BigDecimal maxPkg = jdbcTemplate.queryForObject("SELECT MAX(package_lpa) FROM placements", BigDecimal.class);
        dto.setHighestPackageLpa(maxPkg != null ? maxPkg : BigDecimal.ZERO);

        BigDecimal avgPkg = jdbcTemplate.queryForObject("SELECT ROUND(AVG(package_lpa), 2) FROM placements", BigDecimal.class);
        dto.setAveragePackageLpa(avgPkg != null ? avgPkg : BigDecimal.ZERO);

        Long totalAttendance = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM attendance", Long.class);
        dto.setTotalAttendanceRecords(totalAttendance != null ? totalAttendance : 0L);

        Double avgAtt = jdbcTemplate.queryForObject("SELECT ROUND(AVG(attendance_percentage), 2) FROM attendance", Double.class);
        dto.setOverallAttendanceRate(avgAtt != null ? avgAtt : 0.0);

        return dto;
    }

    public List<DepartmentAnalyticsDTO> getDepartmentAnalytics() {
        String sql = """
            SELECT d.department_id, d.department_name, d.department_code,
                   COUNT(s.student_id) AS student_count,
                   COALESCE(ROUND(AVG(s.cgpa), 2), 0.00) AS avg_cgpa,
                   COUNT(DISTINCT p.student_id) AS placed_count
            FROM departments d
            LEFT JOIN students s ON d.department_id = s.department_id
            LEFT JOIN placements p ON s.student_id = p.student_id AND p.placement_status = 'Placed'
            GROUP BY d.department_id, d.department_name, d.department_code
            ORDER BY student_count DESC, d.department_id ASC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new DepartmentAnalyticsDTO(
                rs.getInt("department_id"),
                rs.getString("department_name"),
                rs.getString("department_code"),
                rs.getLong("student_count"),
                rs.getBigDecimal("avg_cgpa"),
                rs.getLong("placed_count")
        ));
    }

    public List<AttendanceAnalyticsDTO> getAttendanceAnalytics() {
        String sql = """
            SELECT 
                CASE 
                    WHEN attendance_percentage < 75.0 THEN 'Below 75% (Critical)'
                    WHEN attendance_percentage BETWEEN 75.0 AND 85.0 THEN '75% - 85% (Average)'
                    ELSE 'Above 85% (Good)'
                END AS bracket,
                COUNT(*) AS count
            FROM attendance
            GROUP BY bracket
            ORDER BY MIN(attendance_percentage) ASC
        """;

        Long totalRecords = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM attendance", Long.class);
        long total = (totalRecords != null && totalRecords > 0) ? totalRecords : 1L;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            long count = rs.getLong("count");
            double pct = BigDecimal.valueOf(((double) count / total) * 100.0)
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
            return new AttendanceAnalyticsDTO(rs.getString("bracket"), count, pct);
        });
    }

    public List<PlacementAnalyticsDTO> getPlacementAnalytics() {
        String sql = """
            SELECT company_name, 
                   COUNT(*) AS offers_count,
                   ROUND(AVG(package_lpa), 2) AS avg_package,
                   MAX(package_lpa) AS max_package
            FROM placements
            GROUP BY company_name
            ORDER BY offers_count DESC, max_package DESC
            LIMIT 15
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new PlacementAnalyticsDTO(
                rs.getString("company_name"),
                rs.getLong("offers_count"),
                rs.getBigDecimal("avg_package"),
                rs.getBigDecimal("max_package")
        ));
    }
}
