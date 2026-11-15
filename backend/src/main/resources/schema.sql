-- ============================================================================
-- CAMPUS DATA AI ASSISTANT - DATABASE SCHEMA SCRIPT
-- Database: campus_data_assistant
-- ============================================================================

CREATE DATABASE IF NOT EXISTS campus_data_assistant;
USE campus_data_assistant;

-- 1. Departments Table
CREATE TABLE IF NOT EXISTS departments (
    department_id INT AUTO_INCREMENT PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL UNIQUE,
    department_code VARCHAR(10) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Students Table
CREATE TABLE IF NOT EXISTS students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    usn VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    department_id INT NOT NULL,
    semester INT NOT NULL CHECK (semester BETWEEN 1 AND 8),
    cgpa DECIMAL(3, 2) NOT NULL CHECK (cgpa >= 0.00 AND cgpa <= 10.00),
    email VARCHAR(120) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_students_department FOREIGN KEY (department_id) 
        REFERENCES departments(department_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_students_dept (department_id),
    INDEX idx_students_semester (semester),
    INDEX idx_students_cgpa (cgpa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Attendance Table
CREATE TABLE IF NOT EXISTS attendance (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    subject VARCHAR(100) NOT NULL,
    attendance_percentage DECIMAL(5, 2) NOT NULL CHECK (attendance_percentage >= 0.00 AND attendance_percentage <= 100.00),
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) 
        REFERENCES students(student_id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_attendance_student (student_id),
    INDEX idx_attendance_subject (subject),
    INDEX idx_attendance_pct (attendance_percentage)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Marks Table
CREATE TABLE IF NOT EXISTS marks (
    marks_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    subject VARCHAR(100) NOT NULL,
    marks DECIMAL(5, 2) NOT NULL,
    maximum_marks DECIMAL(5, 2) NOT NULL DEFAULT 100.00,
    exam_type VARCHAR(50) DEFAULT 'Final Semester',
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_marks_student FOREIGN KEY (student_id) 
        REFERENCES students(student_id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_marks_student (student_id),
    INDEX idx_marks_subject (subject)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Placements Table
CREATE TABLE IF NOT EXISTS placements (
    placement_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    company_name VARCHAR(100) NOT NULL,
    package_lpa DECIMAL(4, 2) NOT NULL,
    placement_status VARCHAR(50) NOT NULL DEFAULT 'Placed',
    placement_date DATE,
    CONSTRAINT fk_placements_student FOREIGN KEY (student_id) 
        REFERENCES students(student_id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_placements_student (student_id),
    INDEX idx_placements_company (company_name),
    INDEX idx_placements_package (package_lpa),
    INDEX idx_placements_status (placement_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Query History Table
CREATE TABLE IF NOT EXISTS query_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question TEXT NOT NULL,
    generated_sql TEXT NOT NULL,
    explanation TEXT,
    execution_status VARCHAR(30) NOT NULL,
    execution_time_ms BIGINT DEFAULT 0,
    row_count INT DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_history_created (created_at),
    INDEX idx_history_status (execution_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
