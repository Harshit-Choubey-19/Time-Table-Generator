package loginandsignup;
import java.util.*;
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
import java.util.List;

public class ViewCourses extends javax.swing.JFrame {

     public ViewCourses() {
        setTitle("View Courses");
        setSize(1500, 700);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] columnNames = {
            "Course Code", "Course Name", "Assigned Instructors", "Department",
            "Student Count", "Consist Lab", "Rooms", "Days", "Time Slots"
        };

        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            if (conn != null) {
                String courseQuery = "SELECT * FROM Course";
                Statement courseStmt = conn.createStatement();
                ResultSet courseRs = courseStmt.executeQuery(courseQuery);

                while (courseRs.next()) {
                    String courseCode = courseRs.getString("courseCode");
                    String courseName = courseRs.getString("name");
                    String assignedInstructorsStr = courseRs.getString("assignedInstructId");
                    int studentCount = courseRs.getInt("studentCount");
                    boolean isLab = courseRs.getBoolean("consistLab");

                    // Instructors and Departments
                    List<String> instructorNames = new ArrayList<>();
                    Set<String> departments = new HashSet<>();

                    if (assignedInstructorsStr != null && !assignedInstructorsStr.trim().isEmpty()) {
                        String[] instructorIds = assignedInstructorsStr.split(",");
                        for (String instructorId : instructorIds) {
                            instructorId = instructorId.trim();
                            PreparedStatement instructorStmt = conn.prepareStatement(
                                "SELECT name, department FROM Instructor WHERE instructId = ?"
                            );
                            instructorStmt.setString(1, instructorId);
                            ResultSet instructorRs = instructorStmt.executeQuery();

                            if (instructorRs.next()) {
                                instructorNames.add(instructorRs.getString("name"));
                                departments.add(instructorRs.getString("department"));
                            }
                        }
                    }

                    String instructorList = instructorNames.isEmpty() ? "NIL" : String.join(", ", instructorNames);
                    String departmentList = departments.isEmpty() ? "NIL" : String.join(", ", departments);

                    // Rooms
                    Set<String> roomIds = new LinkedHashSet<>();
                    String roomQuery = "SELECT roomId FROM CourseClass WHERE courseCode = ?";
                    PreparedStatement roomStmt = conn.prepareStatement(roomQuery);
                    roomStmt.setString(1, courseCode);
                    ResultSet roomRs = roomStmt.executeQuery();
                    while (roomRs.next()) {
                        roomIds.add(roomRs.getString("roomId"));
                    }
                    String roomList = roomIds.isEmpty() ? "NIL" : String.join(", ", roomIds);

                    // SlotIds (can be multiple)
                    Set<Integer> slotIds = new LinkedHashSet<>();
                    String slotIdQuery = "SELECT slotId FROM TimeSlot WHERE courseCode = ?";
                    PreparedStatement slotStmt = conn.prepareStatement(slotIdQuery);
                    slotStmt.setString(1, courseCode);
                    ResultSet slotRs = slotStmt.executeQuery();
                    while (slotRs.next()) {
                        slotIds.add(slotRs.getInt("slotId"));
                    }

                    // Days and TimeSlots for each slotId
                    Set<String> allDays = new LinkedHashSet<>();
                    Set<String> allTimeSlots = new LinkedHashSet<>();

                    for (int slotId : slotIds) {
                        // Days
                        String dayQuery = "SELECT day FROM TimeSlotDay WHERE slotId = ?";
                        PreparedStatement dayStmt = conn.prepareStatement(dayQuery);
                        dayStmt.setInt(1, slotId);
                        ResultSet dayRs = dayStmt.executeQuery();
                        while (dayRs.next()) {
                            allDays.add(dayRs.getString("day"));
                        }

                        // Time Slot
                        String timeQuery = "SELECT startTime, endTime FROM TimeSlot WHERE slotId = ?";
                        PreparedStatement timeStmt = conn.prepareStatement(timeQuery);
                        timeStmt.setInt(1, slotId);
                        ResultSet timeRs = timeStmt.executeQuery();
                        if (timeRs.next()) {
                            Time start = timeRs.getTime("startTime");
                            Time end = timeRs.getTime("endTime");
                            allTimeSlots.add(String.format("%tR - %tR", start, end));
                        }
                    }

                    String daysStr = allDays.isEmpty() ? "NIL" : String.join(", ", allDays);
                    String timeSlotsStr = allTimeSlots.isEmpty() ? "NIL" : String.join(" | ", allTimeSlots);

                    Object[] row = {
                        courseCode, courseName, instructorList, departmentList,
                        studentCount, isLab, roomList, daysStr, timeSlotsStr
                    };

                    model.addRow(row);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Database connection not established.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error fetching courses: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
                new ViewCourses().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
