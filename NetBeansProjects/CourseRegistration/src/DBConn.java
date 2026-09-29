
import fancyui.FancyTable;
import java.sql.*;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class DBConn {

    String DB_URL = "jdbc:sqlite:course.db";

    public Connection connect() {
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(DB_URL);
            System.out.println("Connected to SQLite Database!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public String login(String username, String password) {
        String query = "SELECT * FROM user WHERE username = ? AND password = ?";

        try (Connection conn = connect(); 
            PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
           
            if (rs.next()){
                String role = rs.getString("role");
                return role;
            }
            return "";
//            String role = rs.getString("role");
//            return rs.next() ? role : "";

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return "";
        }
    }

    public boolean register(String name, String email, String ID, String username, String password) {
        String query = "INSERT INTO user (name,email,id,username,password ) VALUES (?,?,?,?,?) ";

        try (Connection conn = connect(); 
        PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, ID);
            pstmt.setString(4, username);
            pstmt.setString(5, password);

            pstmt.executeUpdate();

            return true;

        } catch (SQLException e) {
            System.out.println("Register Error: " + e.getMessage());
            return false;
        }
    }
    public String getStudentID(String username) {

    String query = "SELECT id FROM user WHERE username = ?";

    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(query)) {

        pstmt.setString(1, username);

        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            return rs.getString("id");
        }

        return "";

    } catch (SQLException e) {
        System.out.println(e.getMessage());
        return "";
    }
}
    public int getEnrolledStudents() {
    String query = "SELECT COUNT(DISTINCT StudentID) AS total "
                 + "FROM info "
                 + "WHERE StudentID IS NOT NULL "
                 + "AND StudentID != ''";

    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(query)) {

        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            System.out.println("Enrolled students count: " + rs.getInt("total"));
            return rs.getInt("total");
        }

    } catch (SQLException e) {
        System.out.println("Error counting students: " + e.getMessage());
    }

    return 0;
}
    public int getCourseOffered() {
    String query = "SELECT COUNT(*) FROM courses";

    try (Connection conn = connect();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {

        if (rs.next()) {
            return rs.getInt(1);
        }

    } catch (SQLException e) {
        System.out.println("Error counting courses: " + e.getMessage());
    }

    return 0;
}

    public DefaultTableModel getAllInfo() {
        String[] headers = {"Student ID", "Code", "Subject", "Schedule", "Teacher", "Status"};

        DefaultTableModel model = new DefaultTableModel(headers, 0);

        String query = "SELECT * FROM info";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String studentID = rs.getString("StudentID");
                String code = rs.getString("Code");
                String subject = rs.getString("Subject");
                String schedule = rs.getString("Schedule");
                String teacher = rs.getString("Teacher");
                String status = rs.getString("Status");

                String[] row = {studentID, code, subject, schedule, teacher, status};

                model.addRow(row);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return model;
    }
    public DefaultTableModel getMyCourses(String studentID) {

    String[] headers = {
        "Student ID", "Code", "Subject",
        "Schedule", "Teacher", "Status"
    };

    DefaultTableModel model = new DefaultTableModel(headers, 0);

    String query = "SELECT StudentID, Code, Subject, Schedule, Teacher, Status "
                 + "FROM info WHERE StudentID = ?";

    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(query)) {

        pstmt.setString(1, studentID);

        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {

            model.addRow(new Object[]{
                rs.getString("StudentID"),
                rs.getString("Code"),
                rs.getString("Subject"),
                rs.getString("Schedule"),
                rs.getString("Teacher"),
                rs.getString("Status")
            });
        }

    } catch (SQLException e) {
        System.out.println("Error loading my courses: " + e.getMessage());
    }

    return model;
}

    public boolean saveCourse(String studentID, String code, String subject,
            String schedule, String teacher, String status) {

        String query = "INSERT INTO info "
                + "(StudentID, Code, Subject, Schedule, Teacher, Status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, studentID);
            pstmt.setString(2, code);
            pstmt.setString(3, subject);
            pstmt.setString(4, schedule);
            pstmt.setString(5, teacher);
            pstmt.setString(6, status);

            pstmt.executeUpdate();

            return true;

        } catch (SQLException e) {
            System.out.println("Error saving course: " + e.getMessage());
            return false;
        }
    }
    public void createCoursesTable() {

    String query = "CREATE TABLE IF NOT EXISTS courses ("
            + "code TEXT PRIMARY KEY, "
            + "courseName TEXT, "
            + "units INTEGER, "
            + "teacher TEXT, "
            + "schedule TEXT"
            + ")";

    try (Connection conn = connect();
         Statement stmt = conn.createStatement()) {

        stmt.executeUpdate(query);
        System.out.println("Courses table ready!");

    } catch (SQLException e) {
        System.out.println("Error creating courses table: " + e.getMessage());
    }
}
    public void insertDefaultCourses() {
        // System.out.println("INSERT DEFAULT COURSES CALLED!");

    String query = "INSERT OR IGNORE INTO courses "
            + "(code, courseName, units, teacher, schedule) "
            + "VALUES (?, ?, ?, ?, ?)";

    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(query)) {

        // NET101
        pstmt.setString(1, "NET101");
        pstmt.setString(2, "Networking 1");
        pstmt.setInt(3, 3);
        pstmt.setString(4, "Mr. Aldrine De Guzman");
        pstmt.setString(5, "Mon-Face to face - MV 304 6:00-8:00 AM | Tue-Virtual Classroom 7:00-8:00 AM");
        pstmt.executeUpdate();

        // MS102
        pstmt.setString(1, "MS102");
        pstmt.setString(2, "Quantitative Methods with Modelling Simulation");
        pstmt.setInt(3, 3);
        pstmt.setString(4, "Mr. Cesar Canlas");
        pstmt.setString(5, "Mon-Face to face - MV 304 8:30-10:30 AM | Tue-Virtual Classroom 8:00-9:00 AM");
        pstmt.executeUpdate();

        // HCI101
        pstmt.setString(1, "HCI101");
        pstmt.setString(2, "Introduction to Human Computer");
        pstmt.setInt(3, 3);
        pstmt.setString(4, "Ms. Regane Macahibag");
        pstmt.setString(5, "Mon-Face to face - MV 304 10:30-12:30 AM | Tue-Virtual Classroom 9:00-10:00 AM");
        pstmt.executeUpdate();

        // SOSLIT
        pstmt.setString(1, "SOSLIT");
        pstmt.setString(2, "Sosyedad at Literatura");
        pstmt.setInt(3, 3);
        pstmt.setString(4, "Ms. Azucena Lim");
        pstmt.setString(5, "Wed-Face to face - MV 304 8:30-10:30 AM | Thu-Virtual Classroom 8:00-9:00 AM");
        pstmt.executeUpdate();

        // PE3
        pstmt.setString(1, "PE3");
        pstmt.setString(2, "Individual and Dual Sports");
        pstmt.setInt(3, 2);
        pstmt.setString(4, "Ms. Rose Ann");
        pstmt.setString(5, "Wed-Face to face - MV 304 10:30-12:30 AM");
        pstmt.executeUpdate();

        // CC104
        pstmt.setString(1, "CC104");
        pstmt.setString(2, "Data Structures and Algorithms");
        pstmt.setInt(3, 3);
        pstmt.setString(4, "Mr. Jayboy Colo");
        pstmt.setString(5, "Fri-Face to face - MV 304 6:00-8:00 AM | Sat-Virtual Classroom 6:00-8:00 AM");
        pstmt.executeUpdate();

        // IPT101
        pstmt.setString(1, "IPT101");
        pstmt.setString(2, "Integrative Programming and Technologies");
        pstmt.setInt(3, 3);
        pstmt.setString(4, "Mr. Romel Cabiling");
        pstmt.setString(5, "Fri-Face to face - MV 304 8:30-10:30 AM | Sat-Virtual Classroom 8:00-10:00 AM");
        pstmt.executeUpdate();

        // ITE1
        pstmt.setString(1, "ITE1");
        pstmt.setString(2, "IT Elective 1 (Web Fundamental)");
        pstmt.setInt(3, 3);
        pstmt.setString(4, "Unknown");
        pstmt.setString(5, "Fri-Face to face - MV 304 10:30-12:30 AM | Sat-Virtual Classroom 10:00-12:00 AM");
        pstmt.executeUpdate();

        System.out.println("Default courses inserted!");

    } catch (SQLException e) {
        System.out.println("Error inserting courses: " + e.getMessage());
    }
}
    public ResultSet getAllCourses() {

    String query = "SELECT * FROM courses";

    try {
        Connection conn = connect();
        PreparedStatement pstmt = conn.prepareStatement(query);

        return pstmt.executeQuery();

    } catch (SQLException e) {
        System.out.println("Error loading courses: " + e.getMessage());
        return null;
    }
}
    public int getTotalUnits(String studentID) {

    String query = "SELECT SUM(c.units) AS totalUnits "
                 + "FROM info i "
                 + "JOIN courses c ON i.code = c.code "
                 + "WHERE i.StudentID = ?";

    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(query)) {

        pstmt.setString(1, studentID);

        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            return rs.getInt("totalUnits");
        }

    } catch (SQLException e) {
        System.out.println("Error getting total units: " + e.getMessage());
    }

    return 0;
}
    public String getStudentName(String studentID) {
    String query = "SELECT name FROM user WHERE id = ?";

    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(query)) {

        pstmt.setString(1, studentID);

        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            return rs.getString("name");
        }

    } catch (SQLException e) {
        System.out.println("Error getting student name: " + e.getMessage());
    }

    return "";
}
    public String getStudentEmail(String studentID) {
    String query = "SELECT email FROM user WHERE id = ?";

    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(query)) {

        pstmt.setString(1, studentID);

        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            return rs.getString("email");
        }

    } catch (SQLException e) {
        System.out.println("Error getting student email: " + e.getMessage());
    }

    return "";
}
  public boolean removeCourseOffered(String code) {
    String query = "DELETE FROM courses WHERE code = ?";

    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(query)) {

        pstmt.setString(1, code);

        int rows = pstmt.executeUpdate();
        return rows > 0;

    } catch (SQLException e) {
        System.out.println("Error removing course: " + e.getMessage());
        return false;
    }
  }
    public boolean isCourseAvailable(String code) {
    String query = "SELECT * FROM courses WHERE code = ?";

    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(query)) {

        pstmt.setString(1, code);
        ResultSet rs = pstmt.executeQuery();

        return rs.next();

    } catch (SQLException e) {
        System.out.println("Error checking course: " + e.getMessage());
        return false;
    }
}
  public boolean isCourseRegistered(String studentID, String code) {
    String query = "SELECT * FROM info WHERE StudentID = ? AND Code = ?";

    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(query)) {

        pstmt.setString(1, studentID);
        pstmt.setString(2, code);

        ResultSet rs = pstmt.executeQuery();

        return rs.next();

    } catch (SQLException e) {
        System.out.println("Error checking course: " + e.getMessage());
        return false;
    }
}
  public boolean dropCourse(String studentID, String code) {
    String query = "DELETE FROM info WHERE StudentID = ? AND Code = ?";

    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(query)) {

        pstmt.setString(1, studentID);
        pstmt.setString(2, code);

        return pstmt.executeUpdate() > 0;

    } catch (SQLException e) {
        System.out.println("Error dropping course: " + e.getMessage());
        return false;
    }
}
}
