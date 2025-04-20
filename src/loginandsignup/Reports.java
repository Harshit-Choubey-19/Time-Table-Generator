package loginandsignup;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.List;
import java.sql.*;
import java.util.ArrayList;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;


public class Reports extends JFrame {
    private JTable reportTable;
    private DefaultTableModel tableModel;

    public Reports() {
        setTitle("Reports & Export - Time Table Builder");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // NAVBAR
        JPanel navbar = new JPanel(new BorderLayout());
        navbar.setBackground(new java.awt.Color(0, 102, 102));
        navbar.setPreferredSize(new Dimension(1000, 50));

        JButton homeBtn = new JButton("Home");
        homeBtn.setFocusPainted(false);
        homeBtn.setBackground(java.awt.Color.WHITE);
        navbar.add(homeBtn, BorderLayout.WEST);

        homeBtn.addActionListener(e -> {
            Dashboard dashFrame = new Dashboard();
            dashFrame.setPreferredSize(new Dimension(1000, 750));
            dashFrame.setVisible(true);
            dashFrame.pack();
            dashFrame.setLocationRelativeTo(null);
            dispose();
        });

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBackground(java.awt.Color.WHITE);
        navbar.add(logoutBtn, BorderLayout.EAST);

        logoutBtn.addActionListener(e -> {
            UserSession.clearSession();
            JOptionPane.showMessageDialog(new JFrame(), "Logout Successful!");
            Login loginFrame = new Login();
            loginFrame.setVisible(true);
            loginFrame.pack();
            loginFrame.setLocationRelativeTo(null);
            dispose();
        });

        add(navbar, BorderLayout.NORTH);

        // MAIN PANEL
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Reports", SwingConstants.CENTER);
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
        titleLabel.setForeground(new java.awt.Color(0, 102, 102));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Table Setup
        String[] columns = {"Course Code", "Course Name", "Instructor", "Rooms", "Days", "Time Slots"};
        tableModel = new DefaultTableModel(columns, 0);
        reportTable = new JTable(tableModel);
        reportTable.setFillsViewportHeight(true);
        JScrollPane tableScrollPane = new JScrollPane(reportTable);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Load data from DB
        loadTimetableFromDatabase();

        // Export Panel
        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        exportPanel.setBorder(BorderFactory.createTitledBorder("Export Reports"));

        JButton exportPDF = new JButton("Export as PDF");

        exportPDF.setBackground(new java.awt.Color(0, 102, 204));
        exportPDF.setForeground(java.awt.Color.WHITE);
        exportPDF.addActionListener(e -> exportToPDF());

        exportPanel.add(exportPDF);

        mainPanel.add(exportPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private void loadTimetableFromDatabase() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String userId = UserSession.getInstance().getUserId();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT t.courseCode, c.name AS courseName, i.name AS instructorName, " +
                "t.classId, tsd.day, CONCAT(TIME_FORMAT(ts.startTime, '%h:%i %p'), ' - ', TIME_FORMAT(ts.endTime, '%h:%i %p')) AS timeSlot " +
                "FROM TimeTable t " +
                "JOIN Course c ON t.courseCode = c.courseCode " +
                "JOIN Instructor i ON t.instructId = i.instructId " +
                "JOIN TimeSlot ts ON t.slotId = ts.slotId " +
                "JOIN TimeSlotDay tsd ON ts.slotId = tsd.slotId " +
                "WHERE t.userId = ?"
            );
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();

            // Temporary storage to group course data by courseCode
            List<CourseSchedule> courseSchedules = new ArrayList<>();

            while (rs.next()) {
                String courseCode = rs.getString("courseCode");
                String courseName = rs.getString("courseName");
                String instructorName = rs.getString("instructorName");
                String classId = rs.getString("classId");
                String day = rs.getString("day");
                String timeSlot = rs.getString("timeSlot");

                // Find if the course already exists in the list
                boolean found = false;
                for (CourseSchedule schedule : courseSchedules) {
                    if (schedule.getCourseCode().equals(courseCode)) {
                        // Add the new day, timeSlot, and classId to the existing entry
                        schedule.addDay(day);
                        schedule.addTimeSlot(timeSlot);
                        schedule.addRoom(classId);
                        found = true;
                        break;
                    }
                }

                // If course is not found, create a new entry
                if (!found) {
                    CourseSchedule newSchedule = new CourseSchedule(courseCode, courseName, instructorName);
                    newSchedule.addDay(day);
                    newSchedule.addTimeSlot(timeSlot);
                    newSchedule.addRoom(classId);
                    courseSchedules.add(newSchedule);
                }
            }

            // Populate table with grouped data
            for (CourseSchedule schedule : courseSchedules) {
                Vector<String> row = new Vector<>();
                row.add(schedule.getCourseCode());
                row.add(schedule.getCourseName());
                row.add(schedule.getInstructorName());
                row.add(String.join(", ", schedule.getRooms()));
                row.add(String.join(", ", schedule.getDays()));
                row.add(String.join(", ", schedule.getTimeSlots()));
                tableModel.addRow(row);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading timetable: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void exportToPDF() {
        try {
            Document document = new Document();
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save PDF File");
            if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

            PdfWriter.getInstance(document, new FileOutputStream(fileChooser.getSelectedFile() + ".pdf"));
            document.open();

            PdfPTable pdfTable = new PdfPTable(tableModel.getColumnCount());
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                pdfTable.addCell(new PdfPCell(new Phrase(tableModel.getColumnName(i))));
            }

            for (int row = 0; row < tableModel.getRowCount(); row++) {
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    pdfTable.addCell(tableModel.getValueAt(row, col).toString());
                }
            }

            document.add(new Paragraph("Timetable Report"));
            document.add(Chunk.NEWLINE);
            document.add(pdfTable);
            document.close();

            JOptionPane.showMessageDialog(this, "Exported successfully to PDF!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error exporting to PDF: " + e.getMessage());
        }
    }
    // Helper class to store course schedule data
    private static class CourseSchedule {
        private String courseCode;
        private String courseName;
        private String instructorName;
        private List<String> days = new ArrayList<>();
        private List<String> timeSlots = new ArrayList<>();
        private List<String> rooms = new ArrayList<>();

        public CourseSchedule(String courseCode, String courseName, String instructorName) {
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.instructorName = instructorName;
        }

        public String getCourseCode() {
            return courseCode;
        }

        public String getCourseName() {
            return courseName;
        }

        public String getInstructorName() {
            return instructorName;
        }

        public List<String> getDays() {
            return days;
        }

        public List<String> getTimeSlots() {
            return timeSlots;
        }

        public List<String> getRooms() {
            return rooms;
        }

        public void addDay(String day) {
            if (!days.contains(day)) {
                days.add(day);
            }
        }

        public void addTimeSlot(String timeSlot) {
            if (!timeSlots.contains(timeSlot)) {
                timeSlots.add(timeSlot);
            }
        }

        public void addRoom(String room) {
            if (!rooms.contains(room)) {
                rooms.add(room);
            }
        }
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
                new Reports().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
