-- ============================================================================
-- CAMPUS DATA AI ASSISTANT - SYNTHETIC SAMPLE DATA SCRIPT
-- ============================================================================

USE campus_data_assistant;

-- Clear previous data in correct foreign key order
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE query_history;
TRUNCATE TABLE placements;
TRUNCATE TABLE marks;
TRUNCATE TABLE attendance;
TRUNCATE TABLE students;
TRUNCATE TABLE departments;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Departments
INSERT INTO departments (department_id, department_name, department_code) VALUES
(1, 'Computer Science & Engineering', 'CSE'),
(2, 'Information Science & Engineering', 'ISE'),
(3, 'Electronics & Communication Engineering', 'ECE'),
(4, 'Artificial Intelligence & Data Science', 'AI_DS'),
(5, 'Mechanical Engineering', 'MECH'),
(6, 'Civil Engineering', 'CIVIL');

-- 2. Students
INSERT INTO students (student_id, usn, name, department_id, semester, cgpa, email) VALUES
-- CSE Students (Dept 1)
(1, '1RV21CS001', 'Aarav Sharma', 1, 8, 9.45, 'aarav.sharma@campus.edu'),
(2, '1RV21CS015', 'Ananya Iyer', 1, 8, 9.62, 'ananya.iyer@campus.edu'),
(3, '1RV21CS028', 'Dev Patel', 1, 8, 8.85, 'dev.patel@campus.edu'),
(4, '1RV21CS042', 'Ishita Rao', 1, 8, 8.40, 'ishita.rao@campus.edu'),
(5, '1RV22CS010', 'Karthik Nair', 1, 6, 7.80, 'karthik.nair@campus.edu'),
(6, '1RV22CS034', 'Meera Kulkarni', 1, 6, 9.15, 'meera.k@campus.edu'),
(7, '1RV23CS005', 'Nikhil Joshi', 1, 4, 6.95, 'nikhil.joshi@campus.edu'),
(8, '1RV23CS022', 'Pooja Reddy', 1, 4, 8.70, 'pooja.reddy@campus.edu'),

-- ISE Students (Dept 2)
(9,  '1RV21IS004', 'Rohan Gupta', 2, 8, 9.10, 'rohan.gupta@campus.edu'),
(10, '1RV21IS018', 'Sneha Deshmukh', 2, 8, 8.75, 'sneha.d@campus.edu'),
(11, '1RV22IS009', 'Tanmay Bhat', 2, 6, 7.30, 'tanmay.bhat@campus.edu'),
(12, '1RV22IS025', 'Varun Menon', 2, 6, 8.92, 'varun.menon@campus.edu'),
(13, '1RV23IS012', 'Vidya Hegde', 2, 4, 8.25, 'vidya.hegde@campus.edu'),
(14, '1RV23IS030', 'Aditya Verma', 2, 4, 7.10, 'aditya.v@campus.edu'),

-- ECE Students (Dept 3)
(15, '1RV21EC007', 'Chirag Shetty', 3, 8, 8.95, 'chirag.shetty@campus.edu'),
(16, '1RV21EC021', 'Divya Sundaram', 3, 8, 9.30, 'divya.s@campus.edu'),
(17, '1RV22EC014', 'Gautam Singhania', 3, 6, 7.65, 'gautam.s@campus.edu'),
(18, '1RV22EC033', 'Harini Krishnan', 3, 6, 8.50, 'harini.k@campus.edu'),
(19, '1RV23EC008', 'Jayant Pai', 3, 4, 6.80, 'jayant.pai@campus.edu'),
(20, '1RV23EC029', 'Kavya Pillai', 3, 4, 8.40, 'kavya.pillai@campus.edu'),

-- AI_DS Students (Dept 4)
(21, '1RV21AI002', 'Manish Agrawal', 4, 8, 9.55, 'manish.a@campus.edu'),
(22, '1RV21AI016', 'Neha Bhattacharya', 4, 8, 9.20, 'neha.b@campus.edu'),
(23, '1RV22AI011', 'Omkar Patil', 4, 6, 8.15, 'omkar.patil@campus.edu'),
(24, '1RV22AI027', 'Pranav Sawant', 4, 6, 7.45, 'pranav.sawant@campus.edu'),
(25, '1RV23AI006', 'Rhea Chakraborty', 4, 4, 9.05, 'rhea.c@campus.edu'),

-- MECH Students (Dept 5)
(26, '1RV21ME005', 'Sachin Kulkarni', 5, 8, 8.10, 'sachin.k@campus.edu'),
(27, '1RV21ME019', 'Shashank Murthy', 5, 8, 7.85, 'shashank.m@campus.edu'),
(28, '1RV22ME012', 'Siddharth Roy', 5, 6, 7.20, 'siddharth.r@campus.edu'),
(29, '1RV22ME031', 'Tejas Gowda', 5, 6, 6.90, 'tejas.gowda@campus.edu'),
(30, '1RV23ME015', 'Utkarsh Trivedi', 5, 4, 7.60, 'utkarsh.t@campus.edu'),

-- CIVIL Students (Dept 6)
(31, '1RV21CV003', 'Vikramaditya Bose', 6, 8, 8.20, 'vikram.bose@campus.edu'),
(32, '1RV21CV017', 'Yashaswini Rao', 6, 8, 8.60, 'yashaswini.r@campus.edu'),
(33, '1RV22CV008', 'Zameer Khan', 6, 6, 6.75, 'zameer.khan@campus.edu'),
(34, '1RV22CV022', 'Abhinav Swaminathan', 6, 6, 7.40, 'abhinav.s@campus.edu'),
(35, '1RV23CV011', 'Bhavana Chawla', 6, 4, 8.10, 'bhavana.c@campus.edu');

-- 3. Attendance Records
-- Include realistic attendance, with multiple students below 75% for testing
INSERT INTO attendance (student_id, subject, attendance_percentage) VALUES
-- CSE
(1, 'Database Management Systems', 92.50),
(1, 'Cloud Computing', 95.00),
(2, 'Database Management Systems', 98.00),
(2, 'Cloud Computing', 96.50),
(3, 'Database Management Systems', 84.00),
(3, 'Cloud Computing', 81.50),
(4, 'Database Management Systems', 71.00), -- < 75%
(4, 'Cloud Computing', 73.50),             -- < 75%
(5, 'Operating Systems', 68.00),            -- < 75%
(5, 'Compiler Design', 74.00),              -- < 75%
(6, 'Operating Systems', 94.00),
(6, 'Compiler Design', 91.00),
(7, 'Data Structures', 64.50),              -- < 75%
(7, 'Design & Analysis of Algorithms', 70.00), -- < 75%
(8, 'Data Structures', 89.00),
(8, 'Design & Analysis of Algorithms', 87.50),

-- ISE
(9,  'Distributed Systems', 91.00),
(9,  'Big Data Analytics', 93.00),
(10, 'Distributed Systems', 86.00),
(10, 'Big Data Analytics', 84.50),
(11, 'Software Engineering', 69.50),        -- < 75%
(11, 'Computer Networks', 72.00),           -- < 75%
(12, 'Software Engineering', 90.00),
(12, 'Computer Networks', 88.50),
(13, 'Web Technologies', 85.00),
(14, 'Web Technologies', 71.50),            -- < 75%

-- ECE
(15, 'Digital Signal Processing', 88.00),
(15, 'VLSI Design', 85.50),
(16, 'Digital Signal Processing', 96.00),
(16, 'VLSI Design', 94.00),
(17, 'Embedded Systems', 72.00),            -- < 75%
(17, 'Control Systems', 70.50),             -- < 75%
(18, 'Embedded Systems', 89.00),
(18, 'Control Systems', 86.50),
(19, 'Signals & Systems', 63.00),           -- < 75%
(20, 'Signals & Systems', 88.00),

-- AI_DS
(21, 'Deep Learning', 97.00),
(21, 'Natural Language Processing', 95.50),
(22, 'Deep Learning', 93.00),
(22, 'Natural Language Processing', 91.50),
(23, 'Machine Learning', 84.00),
(24, 'Machine Learning', 73.00),            -- < 75%
(25, 'Python for Data Science', 92.00),

-- MECH
(26, 'Thermodynamics', 82.00),
(27, 'Thermodynamics', 79.50),
(28, 'Fluid Mechanics', 70.00),             -- < 75%
(29, 'Fluid Mechanics', 66.50),             -- < 75%
(30, 'Kinematics of Machines', 80.00),

-- CIVIL
(31, 'Structural Analysis', 85.00),
(32, 'Structural Analysis', 89.00),
(33, 'Geotechnical Engineering', 65.00),    -- < 75%
(34, 'Geotechnical Engineering', 76.00),
(35, 'Environmental Engineering', 84.00);

-- 4. Marks Records
INSERT INTO marks (student_id, subject, marks, maximum_marks, exam_type) VALUES
-- CSE
(1, 'Database Management Systems', 95.0, 100.0, 'Final Semester'),
(1, 'Cloud Computing', 92.0, 100.0, 'Final Semester'),
(2, 'Database Management Systems', 98.0, 100.0, 'Final Semester'),
(2, 'Cloud Computing', 97.0, 100.0, 'Final Semester'),
(3, 'Database Management Systems', 85.0, 100.0, 'Final Semester'),
(3, 'Cloud Computing', 88.0, 100.0, 'Final Semester'),
(4, 'Database Management Systems', 79.0, 100.0, 'Final Semester'),
(4, 'Cloud Computing', 81.0, 100.0, 'Final Semester'),
(5, 'Operating Systems', 72.0, 100.0, 'Final Semester'),
(5, 'Compiler Design', 76.0, 100.0, 'Final Semester'),
(6, 'Operating Systems', 93.0, 100.0, 'Final Semester'),
(6, 'Compiler Design', 90.0, 100.0, 'Final Semester'),
(7, 'Data Structures', 65.0, 100.0, 'Final Semester'),
(7, 'Design & Analysis of Algorithms', 68.0, 100.0, 'Final Semester'),
(8, 'Data Structures', 88.0, 100.0, 'Final Semester'),
(8, 'Design & Analysis of Algorithms', 86.0, 100.0, 'Final Semester'),

-- ISE
(9,  'Distributed Systems', 92.0, 100.0, 'Final Semester'),
(9,  'Big Data Analytics', 90.0, 100.0, 'Final Semester'),
(10, 'Distributed Systems', 87.0, 100.0, 'Final Semester'),
(10, 'Big Data Analytics', 85.0, 100.0, 'Final Semester'),
(11, 'Software Engineering', 71.0, 100.0, 'Final Semester'),
(12, 'Software Engineering', 91.0, 100.0, 'Final Semester'),
(13, 'Web Technologies', 83.0, 100.0, 'Final Semester'),
(14, 'Web Technologies', 69.0, 100.0, 'Final Semester'),

-- ECE
(15, 'Digital Signal Processing', 89.0, 100.0, 'Final Semester'),
(15, 'VLSI Design', 87.0, 100.0, 'Final Semester'),
(16, 'Digital Signal Processing', 94.0, 100.0, 'Final Semester'),
(16, 'VLSI Design', 92.0, 100.0, 'Final Semester'),
(17, 'Embedded Systems', 74.0, 100.0, 'Final Semester'),
(18, 'Embedded Systems', 86.0, 100.0, 'Final Semester'),
(19, 'Signals & Systems', 67.0, 100.0, 'Final Semester'),
(20, 'Signals & Systems', 85.0, 100.0, 'Final Semester'),

-- AI_DS
(21, 'Deep Learning', 96.0, 100.0, 'Final Semester'),
(21, 'Natural Language Processing', 94.0, 100.0, 'Final Semester'),
(22, 'Deep Learning', 91.0, 100.0, 'Final Semester'),
(22, 'Natural Language Processing', 89.0, 100.0, 'Final Semester'),
(23, 'Machine Learning', 82.0, 100.0, 'Final Semester'),
(24, 'Machine Learning', 75.0, 100.0, 'Final Semester'),
(25, 'Python for Data Science', 91.0, 100.0, 'Final Semester'),

-- MECH
(26, 'Thermodynamics', 81.0, 100.0, 'Final Semester'),
(27, 'Thermodynamics', 78.0, 100.0, 'Final Semester'),
(28, 'Fluid Mechanics', 71.0, 100.0, 'Final Semester'),
(29, 'Fluid Mechanics', 68.0, 100.0, 'Final Semester'),
(30, 'Kinematics of Machines', 77.0, 100.0, 'Final Semester'),

-- CIVIL
(31, 'Structural Analysis', 83.0, 100.0, 'Final Semester'),
(32, 'Structural Analysis', 87.0, 100.0, 'Final Semester'),
(33, 'Geotechnical Engineering', 66.0, 100.0, 'Final Semester'),
(34, 'Geotechnical Engineering', 73.0, 100.0, 'Final Semester'),
(35, 'Environmental Engineering', 82.0, 100.0, 'Final Semester');

-- 5. Placements (Eligible 7th & 8th sem students)
INSERT INTO placements (student_id, company_name, package_lpa, placement_status, placement_date) VALUES
(1,  'Google', 42.50, 'Placed', '2025-09-15'),
(2,  'Microsoft', 44.00, 'Placed', '2025-09-18'),
(3,  'Amazon', 32.00, 'Placed', '2025-10-02'),
(4,  'Cisco Systems', 19.50, 'Placed', '2025-10-15'),
(9,  'Oracle', 24.00, 'Placed', '2025-10-20'),
(10, 'Deloitte', 12.50, 'Placed', '2025-11-05'),
(15, 'Qualcomm', 28.00, 'Placed', '2025-10-10'),
(16, 'Texas Instruments', 26.50, 'Placed', '2025-10-12'),
(21, 'NVIDIA', 38.00, 'Placed', '2025-09-25'),
(22, 'Adobe', 34.00, 'Placed', '2025-10-08'),
(26, 'Larsen & Toubro', 9.50, 'Placed', '2025-11-12'),
(27, 'Tata Motors', 8.80, 'Placed', '2025-11-20'),
(31, 'Sobha Developers', 7.50, 'Placed', '2025-11-25'),
(32, 'L&T Construction', 8.20, 'Placed', '2025-11-28');

-- 6. Initial Sample Query History
INSERT INTO query_history (question, generated_sql, explanation, execution_status, execution_time_ms, row_count) VALUES
('Show all students belonging to the CSE department.', 
 'SELECT s.student_id, s.usn, s.name, s.semester, s.cgpa, s.email FROM students s JOIN departments d ON s.department_id = d.department_id WHERE d.department_code = ''CSE'' LIMIT 100;', 
 'Selects all student columns for students whose department code is CSE by joining the students and departments tables.', 
 'SUCCESS', 14, 8),
('What is the average CGPA of CSE students?', 
 'SELECT AVG(s.cgpa) AS average_cgpa FROM students s JOIN departments d ON s.department_id = d.department_id WHERE d.department_code = ''CSE'';', 
 'Calculates the arithmetic average of the CGPA column for students in the CSE department.', 
 'SUCCESS', 11, 1),
('Find students whose attendance is below 75%.', 
 'SELECT s.student_id, s.usn, s.name, a.subject, a.attendance_percentage FROM students s JOIN attendance a ON s.student_id = a.student_id WHERE a.attendance_percentage < 75.00 ORDER BY a.attendance_percentage ASC LIMIT 100;', 
 'Retrieves students with attendance records under 75 percent, ordered ascending by attendance percentage.', 
 'SUCCESS', 16, 12);
