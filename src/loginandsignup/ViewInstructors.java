package loginandsignup;
import javax.swing.table.DefaultTableModel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.*;
import java.util.ArrayList;

public class ViewInstructors extends javax.swing.JFrame {


    public ViewInstructors() {
        setTitle("View Instructors");
        setSize(800, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] columnNames = {"Instructor ID", "Name", "Department", "Assigned Courses"};
        // Make table non-editable
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Disallow editing
            }
        };
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            if (conn != null) {
                String instructorQuery = "SELECT * FROM Instructor";
                Statement instructorStmt = conn.createStatement();
                ResultSet instructorRs = instructorStmt.executeQuery(instructorQuery);

                while (instructorRs.next()) {
                    String instructId = instructorRs.getString("instructId");
                    String name = instructorRs.getString("name");
                    String department = instructorRs.getString("department");

                    // Fetch assigned courses from InstructorCourses table
                    String coursesQuery = "SELECT courseCode FROM Course WHERE assignedInstructId = ?";
                    PreparedStatement courseStmt = conn.prepareStatement(coursesQuery);
                    courseStmt.setString(1, instructId);
                    ResultSet courseRs = courseStmt.executeQuery();

                    ArrayList<String> courses = new ArrayList<>();
                    while (courseRs.next()) {
                        courses.add(courseRs.getString("courseCode"));
                    }

                    String courseList = courses.isEmpty() ? "NIL" : String.join(", ", courses);
                    Object[] row = {instructId, name, department, courseList};
                    model.addRow(row);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Database connection not established.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error fetching instructors: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        add(scrollPane, BorderLayout.CENTER);
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ViewInstructors().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
