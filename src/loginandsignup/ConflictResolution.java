package loginandsignup;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.sql.*;
import java.util.ArrayList;
import java.util.*;

public class ConflictResolution extends javax.swing.JFrame {
    private JTable conflictTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> courseDropdown;
    private JComboBox<String> actionDropdown;
    private JComboBox<String> newCourseDropdown;
    private JButton resolveBtn;
    private List<Map<String, Object>> conflicts = new ArrayList<>();
    private Map<Integer, Integer> rowToConflictIdMap = new HashMap<>();
    // Set to track courses already in the timetable
    private Set<String> coursesInTimetable = new HashSet<>();
    private TimeTableGenerator parentFrame;
    
    // Adding an overloaded constructor that doesn't require a parameter
    public ConflictResolution() {
    this(null); // Call the other constructor with null parent
}

    public ConflictResolution(TimeTableGenerator parent) {
        this.parentFrame = parent;
        setTitle("Conflict Resolution - Time Table Builder");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // NAVBAR
        JPanel navbar = createNavBar();
        add(navbar, BorderLayout.NORTH);
        
        // MAIN PANEL
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Conflict Resolution", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 102, 102));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Conflict Table
        createConflictTable();
        JScrollPane tableScrollPane = new JScrollPane(conflictTable);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Resolution Panel
        JPanel resolutionPanel = createResolutionPanel();
        mainPanel.add(resolutionPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Load courses already in timetable
        loadCoursesInTimetable();
        
        // Load conflicts from database
        loadConflictsFromDatabase();
        
        setVisible(true);
    }
    
    // Method to load courses already in the timetable
    private void loadCoursesInTimetable() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT DISTINCT courseCode FROM TimeTable WHERE userId = ?"
            );
            ps.setString(1, UserSession.getInstance().getUserId());
            ResultSet rs = ps.executeQuery();
            
            coursesInTimetable.clear();
            while (rs.next()) {
                coursesInTimetable.add(rs.getString("courseCode"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading timetable courses: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private JPanel createNavBar() {
        JPanel navbar = new JPanel(new BorderLayout());
        navbar.setBackground(new Color(0, 102, 102));
        navbar.setPreferredSize(new Dimension(1000, 50));
        
        JButton homeBtn = new JButton("Home");
        homeBtn.setFocusPainted(false);
        homeBtn.setBackground(Color.WHITE);
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
        logoutBtn.setBackground(Color.WHITE);
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
        
        return navbar;
    }
    
    private void createConflictTable() {
        String[] columns = {"Conflict ID", "Course", "Instructor", "Room", "Day", "Time Slot", "Conflict Type", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        
        conflictTable = new JTable(tableModel);
        conflictTable.setFillsViewportHeight(true);
        conflictTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Hide conflict ID column
        conflictTable.getColumnModel().getColumn(0).setMinWidth(0);
        conflictTable.getColumnModel().getColumn(0).setMaxWidth(0);
        conflictTable.getColumnModel().getColumn(0).setWidth(0);
        
        // Add selection listener
        conflictTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && conflictTable.getSelectedRow() != -1) {
                updateCourseDropdown();
            }
        });
    }
    
    private JPanel createResolutionPanel() {
        JPanel resolutionPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        resolutionPanel.setBorder(BorderFactory.createTitledBorder("Resolve Conflict"));
        
        JLabel courseLabel = new JLabel("Select Conflicting Course:");
        courseDropdown = new JComboBox<>();
        
        JLabel actionLabel = new JLabel("Action:");
        actionDropdown = new JComboBox<>(new String[]{"Remove Course", "Replace with Another Course"});
        
        JLabel newCourseLabel = new JLabel("New Course (if replacing):");
        newCourseDropdown = new JComboBox<>();
        
        resolveBtn = new JButton("Resolve Conflict");
        resolveBtn.setBackground(new Color(0, 153, 76));
        resolveBtn.setForeground(Color.WHITE);
        
        // Initially disable new course dropdown
        newCourseDropdown.setEnabled(false);
        
        // Add action listeners
        actionDropdown.addActionListener(e -> {
            String action = (String) actionDropdown.getSelectedItem();
            if (action != null) {
                newCourseDropdown.setEnabled(action.equals("Replace with Another Course"));
                if (action.equals("Replace with Another Course")) {
                    loadAvailableCourses();
                }
            }
        });
        
        resolveBtn.addActionListener(e -> resolveConflict());
        
        resolutionPanel.add(courseLabel);
        resolutionPanel.add(courseDropdown);
        resolutionPanel.add(actionLabel);
        resolutionPanel.add(actionDropdown);
        resolutionPanel.add(newCourseLabel);
        resolutionPanel.add(newCourseDropdown);
        resolutionPanel.add(new JLabel()); // Empty space
        resolutionPanel.add(resolveBtn);
        
        return resolutionPanel;
    }
    
    private void loadConflictsFromDatabase() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            
            // Get all conflicts for the current user
            PreparedStatement ps = conn.prepareStatement(
                "SELECT C.conflictId, C.courseCode, C.instructId, C.classId, C.slotId, " +
                "C.conflictType, I.name AS instructorName " +
                "FROM Conflict C " +
                "JOIN Instructor I ON C.instructId = I.instructId " +
                "WHERE C.userId = ? " +
                "ORDER BY C.courseCode, C.slotId"  // Order to help with grouping
            );
            ps.setString(1, UserSession.getInstance().getUserId());
            ResultSet rs = ps.executeQuery();
            
            tableModel.setRowCount(0); // Clear table
            conflicts.clear();
            rowToConflictIdMap.clear();
            
            // Maps to track unique course conflicts and their days/time slots
            Map<String, Map<String, Object>> uniqueConflicts = new HashMap<>();
            
            int row = 0;
            while (rs.next()) {
                int conflictId = rs.getInt("conflictId");
                String courseCode = rs.getString("courseCode");
                String instructorName = rs.getString("instructorName");
                String classId = rs.getString("classId");
                int slotId = rs.getInt("slotId");
                String conflictType = rs.getString("conflictType");
                
                // Get day and time from slotId
                String day = getDayForSlot(slotId);
                String timeSlot = getTimeSlotForSlot(slotId);
                
                // Create key for unique conflict
                String conflictKey = courseCode + "|" + conflictType;
                
                // Save or update conflict information
                if (uniqueConflicts.containsKey(conflictKey)) {
                    // This course already has a conflict entry, add the day and time
                    Map<String, Object> existingConflict = uniqueConflicts.get(conflictKey);
                    Set<String> days = (Set<String>) existingConflict.get("days");
                    Set<String> times = (Set<String>) existingConflict.get("times");
                    Set<Integer> slotIds = (Set<Integer>) existingConflict.get("slotIds");
                    
                    days.add(day);
                    times.add(timeSlot);
                    slotIds.add(slotId);
                    
                    // Add this conflictId to track all conflicts for this combination
                    Set<Integer> conflictIds = (Set<Integer>) existingConflict.get("conflictIds");
                    conflictIds.add(conflictId);
                } else {
                    // New conflict entry
                    Map<String, Object> conflictInfo = new HashMap<>();
                    conflictInfo.put("conflictId", conflictId);
                    conflictInfo.put("courseCode", courseCode);
                    conflictInfo.put("instructId", rs.getString("instructId"));
                    conflictInfo.put("instructorName", instructorName);
                    conflictInfo.put("classId", classId);
                    
                    // Use sets to track multiple days and times
                    Set<String> days = new HashSet<>();
                    days.add(day);
                    conflictInfo.put("days", days);
                    
                    Set<String> times = new HashSet<>();
                    times.add(timeSlot);
                    conflictInfo.put("times", times);
                    
                    Set<Integer> slotIds = new HashSet<>();
                    slotIds.add(slotId);
                    conflictInfo.put("slotIds", slotIds);
                    
                    conflictInfo.put("conflictType", conflictType);
                    
                    // Track all conflict IDs for this combination
                    Set<Integer> conflictIds = new HashSet<>();
                    conflictIds.add(conflictId);
                    conflictInfo.put("conflictIds", conflictIds);
                    
                    uniqueConflicts.put(conflictKey, conflictInfo);
                }
            }
            
            // Now process all unique conflicts
            for (Map<String, Object> conflict : uniqueConflicts.values()) {
                String courseCode = (String) conflict.get("courseCode");
                String instructorName = (String) conflict.get("instructorName");
                String classId = (String) conflict.get("classId");
                String conflictType = (String) conflict.get("conflictType");
                
                // Join all days with commas
                Set<String> days = (Set<String>) conflict.get("days");
                String joinedDays = String.join(", ", days);
                
                // Join all time slots with commas
                Set<String> times = (Set<String>) conflict.get("times");
                String joinedTimes = String.join(", ", times);
                
                // Check if the course is in the timetable
                String status = coursesInTimetable.contains(courseCode) ? "In Timetable" : "Not in Timetable";
                
                // Get the primary conflict ID (first one)
                Set<Integer> conflictIds = (Set<Integer>) conflict.get("conflictIds");
                int primaryConflictId = conflictIds.iterator().next();
                
                // Add to table
                Object[] rowData = {primaryConflictId, courseCode, instructorName, classId, 
                                   joinedDays, joinedTimes, conflictType, status};
                tableModel.addRow(rowData);
                
                // Map this row to the conflict ID
                rowToConflictIdMap.put(row, primaryConflictId);
                
                // Store conflict in our list for later reference
                Map<String, Object> conflictToStore = new HashMap<>(conflict);
                conflictToStore.put("day", joinedDays);
                conflictToStore.put("timeSlot", joinedTimes);
                conflictToStore.put("inTimetable", coursesInTimetable.contains(courseCode));
                conflicts.add(conflictToStore);
                
                row++;
                
                // Now, also find and add the clashing course entries
                if (conflictType.equals("Room Conflict") || conflictType.equals("Time Slot Conflict")) {
                    Set<Integer> slotIds = (Set<Integer>) conflict.get("slotIds");
                    for (Integer slotId : slotIds) {
                        findAndAddClashingCourses(slotId, courseCode, classId, conflictType, row);
                    }
                }
            }
            
            if (conflicts.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No conflicts found!");
                
                // Save final timetable
                saveFinalTimetable();
                
                // Return to TimeTableGenerator
                this.dispose();
            } else {
                // Select first row by default
                if (conflictTable.getRowCount() > 0) {
                    conflictTable.setRowSelectionInterval(0, 0);
                    updateCourseDropdown();
                }
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading conflicts: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void findAndAddClashingCourses(int slotId, String currentCourse, String classId, String conflictType, int row) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps;
            
            if (conflictType.equals("Room Conflict")) {
                // Find courses using the same room at the same time
               ps = conn.prepareStatement(
    "SELECT DISTINCT TT.courseCode, TT.instructId, I.name AS instructorName, TT.classId, " +
    "TSD.day, TS.startTime, TS.endTime " +
    "FROM TimeTable TT " +
    "JOIN Instructor I ON TT.instructId = I.instructId " +
    "JOIN TimeSlot TS ON TT.slotId = TS.slotId " +
    "JOIN TimeSlotDay TSD ON TT.slotId = TSD.slotId " +
    "WHERE TT.userId = ? AND TT.slotId = ? AND TT.classId = ? AND TT.courseCode != ?"
);

                ps.setString(1, UserSession.getInstance().getUserId());
                ps.setInt(2, slotId);
                ps.setString(3, classId); 
                ps.setString(4, currentCourse);
            } else {
                // Find courses at the same time slot (time conflict)
                ps = conn.prepareStatement(
    "SELECT DISTINCT TT.courseCode, TT.instructId, I.name AS instructorName, TT.classId, " +
    "TSD.day, TS.startTime, TS.endTime " +
    "FROM TimeTable TT " +
    "JOIN Instructor I ON TT.instructId = I.instructId " +
    "JOIN TimeSlot TS ON TT.slotId = TS.slotId " +
    "JOIN TimeSlotDay TSD ON TT.slotId = TSD.slotId " +
    "WHERE TT.userId = ? AND TT.slotId = ? AND TT.courseCode != ?"
);

                ps.setString(1, UserSession.getInstance().getUserId());
                ps.setInt(2, slotId);
                ps.setString(3, currentCourse);
            }
            
            ResultSet rs = ps.executeQuery();
            
            // Process each clashing course
            while (rs.next()) {
                String clashingCourse = rs.getString("courseCode");
                
                // Skip if we already have this course in our conflicts list
                boolean alreadyAdded = false;
                for (Map<String, Object> existingConflict : conflicts) {
                    if (clashingCourse.equals(existingConflict.get("courseCode"))) {
                        alreadyAdded = true;
                        break;
                    }
                }
                
                if (!alreadyAdded) {
                    String instructorName = rs.getString("instructorName");
                    String clashingClassId = rs.getString("classId");
                    String day = rs.getString("day");
                    
                    // Format time
                    Time startTime = rs.getTime("startTime");
                    Time endTime = rs.getTime("endTime");
                    String timeSlot = formatTime(startTime) + " - " + formatTime(endTime);
                    
                    // Check if the course is in the timetable
                    String status = coursesInTimetable.contains(clashingCourse) ? "In Timetable" : "Not in Timetable";
                    
                    // Generate a special conflict ID for this clashing course
                    // We'll use a negative value to distinguish from primary conflicts
                    int clashConflictId = -1 * (row * 1000 + slotId);
                    
                    // Add to table
                    Object[] rowData = {clashConflictId, clashingCourse, instructorName, clashingClassId, 
                                       day, timeSlot, conflictType + " (Clash)", status};
                    tableModel.addRow(rowData);
                    
                    // Add to our conflicts list
                    Map<String, Object> clashConflict = new HashMap<>();
                    clashConflict.put("conflictId", clashConflictId);
                    clashConflict.put("courseCode", clashingCourse);
                    clashConflict.put("instructId", rs.getString("instructId"));
                    clashConflict.put("instructorName", instructorName);
                    clashConflict.put("classId", clashingClassId);
                    clashConflict.put("slotId", slotId);
                    clashConflict.put("day", day);
                    clashConflict.put("timeSlot", timeSlot);
                    clashConflict.put("conflictType", conflictType + " (Clash)");
                    clashConflict.put("inTimetable", coursesInTimetable.contains(clashingCourse));
                    
                    // Add slot IDs and other needed fields
                    Set<Integer> slotIds = new HashSet<>();
                    slotIds.add(slotId);
                    clashConflict.put("slotIds", slotIds);
                    
                    Set<String> days = new HashSet<>();
                    days.add(day);
                    clashConflict.put("days", days);
                    
                    Set<String> times = new HashSet<>();
                    times.add(timeSlot);
                    clashConflict.put("times", times);
                    
                    conflicts.add(clashConflict);
                    
                    // Map this row to the conflict ID
                    rowToConflictIdMap.put(row, clashConflictId);
                    row++;
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error finding clashing courses: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void saveFinalTimetable() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);
            
            // Get all current timetable entries
            PreparedStatement psGet = conn.prepareStatement(
                "SELECT courseCode, instructId, classId, slotId FROM TimeTable WHERE userId = ?"
            );
            psGet.setString(1, UserSession.getInstance().getUserId());
            ResultSet rs = psGet.executeQuery();
            
            // Clear previous timetable first (to avoid duplicates)
            PreparedStatement psClear = conn.prepareStatement(
                "DELETE FROM TimeTable WHERE userId = ?"
            );
            psClear.setString(1, UserSession.getInstance().getUserId());
            psClear.executeUpdate();
            
            // Re-insert with proper timetableId
            PreparedStatement psInsert = conn.prepareStatement(
                "INSERT INTO TimeTable (timetableId, userId, courseCode, instructId, classId, slotId) " +
                "VALUES (?, ?, ?, ?, ?, ?)"
            );
            
            int nextId = 1;
            
            // Get the next available ID for timetable
            PreparedStatement psMaxId = conn.prepareStatement(
                "SELECT MAX(timetableId) FROM TimeTable"
            );
            ResultSet rsMaxId = psMaxId.executeQuery();
            if (rsMaxId.next() && rsMaxId.getObject(1) != null) {
                nextId = rsMaxId.getInt(1) + 1;
            }
            
            // Insert all timetable entries
            while (rs.next()) {
                psInsert.setInt(1, nextId);
                psInsert.setString(2, UserSession.getInstance().getUserId());
                psInsert.setString(3, rs.getString("courseCode"));
                psInsert.setString(4, rs.getString("instructId"));
                psInsert.setString(5, rs.getString("classId"));
                psInsert.setInt(6, rs.getInt("slotId"));
                psInsert.executeUpdate();
                nextId++;
            }
            
            conn.commit();
            JOptionPane.showMessageDialog(this, "Timetable saved successfully!");
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving timetable: " + e.getMessage());
            e.printStackTrace();
            try {
                Connection conn = DatabaseConnection.getInstance().getConnection();
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                Connection conn = DatabaseConnection.getInstance().getConnection();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private String getDayForSlot(int slotId) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT day FROM TimeSlotDay WHERE slotId = ?"
            );
            ps.setInt(1, slotId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getString("day");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return "Unknown";
    }
    
    private String getTimeSlotForSlot(int slotId) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT startTime, endTime FROM TimeSlot WHERE slotId = ?"
            );
            ps.setInt(1, slotId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                Time startTime = rs.getTime("startTime");
                Time endTime = rs.getTime("endTime");
                
                if (startTime != null && endTime != null) {
                    return formatTime(startTime) + " - " + formatTime(endTime);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return "Unknown";
    }
    
    private String formatTime(Time time) {
        String[] parts = time.toString().split(":");
        int hour = Integer.parseInt(parts[0]);
        String minute = parts[1];
        
        String period = hour >= 12 ? "PM" : "AM";
        hour = hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour);
        
        return hour + ":" + minute + " " + period;
    }
    
    private void updateCourseDropdown() {
        int selectedRow = conflictTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        courseDropdown.removeAllItems();
        
        // Find conflicting courses for this conflict
        int conflictId = (int) conflictTable.getValueAt(selectedRow, 0);
        
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            
            // Get the selected conflict details
            Map<String, Object> selectedConflict = null;
            for (Map<String, Object> conflict : conflicts) {
                if ((int)conflict.get("conflictId") == conflictId) {
                    selectedConflict = conflict;
                    break;
                }
            }
            
            if (selectedConflict == null) return;
            
            String conflictType = (String) selectedConflict.get("conflictType");
            Set<Integer> slotIds = (Set<Integer>) selectedConflict.get("slotIds");
            String currentCourseCode = (String) selectedConflict.get("courseCode");
            
            // Determine courses involved in this conflict
            Set<String> conflictingCourses = new HashSet<>();
            conflictingCourses.add(currentCourseCode);
            
            // Find all clashing courses for all slots involved in this conflict
            for (Integer slotId : slotIds) {
                String classId = (String) selectedConflict.get("classId");
                
                if (conflictType.contains("Room Conflict")) {
                    // Find other course using same room at same time
                    PreparedStatement ps = conn.prepareStatement(
                        "SELECT DISTINCT C.courseCode FROM TimeTable C " +
                        "WHERE C.userId = ? AND C.slotId = ? AND C.classId = ? AND C.courseCode != ?"
                    );
                    ps.setString(1, UserSession.getInstance().getUserId());
                    ps.setInt(2, slotId);
                    ps.setString(3, classId);
                    ps.setString(4, currentCourseCode);
                    
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        conflictingCourses.add(rs.getString("courseCode"));
                    }
                } else if (conflictType.contains("Time Slot Conflict")) {
                    // Find other course at same time
                    PreparedStatement ps = conn.prepareStatement(
                        "SELECT DISTINCT C.courseCode FROM TimeTable C " +
                        "WHERE C.userId = ? AND C.slotId = ? AND C.courseCode != ?"
                    );
                    ps.setString(1, UserSession.getInstance().getUserId());
                    ps.setInt(2, slotId);
                    ps.setString(3, currentCourseCode);
                    
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        conflictingCourses.add(rs.getString("courseCode"));
                    }
                }
            }
            
            // Add all courses to the dropdown
            for (String course : conflictingCourses) {
                courseDropdown.addItem(course);
            }
            
            // Customize renderer to show which courses are in timetable
            courseDropdown.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value,
                        int index, boolean isSelected, boolean cellHasFocus) {
                    Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    
                    if (value != null && coursesInTimetable.contains(value.toString())) {
                        c.setForeground(Color.GRAY);
                        setText(value.toString() + " (in timetable)");
                    } else {
                        c.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
                    }
                    
                    return c;
                }
            });
            
            // Add a listener to the dropdown to check if the course is in timetable
            courseDropdown.addActionListener(e -> {
                if (courseDropdown.getSelectedItem() != null) {
                    String selectedCourse = courseDropdown.getSelectedItem().toString();
                    // Strip "(in timetable)" suffix if present
                    if (selectedCourse.endsWith(" (in timetable)")) {
                        selectedCourse = selectedCourse.substring(0, selectedCourse.indexOf(" (in timetable)"));
                    }
                    
                    boolean isInTimetable = coursesInTimetable.contains(selectedCourse);
                    resolveBtn.setEnabled(!isInTimetable);
                    
                    if (isInTimetable) {
                        JOptionPane.showMessageDialog(this, 
                            "Course '" + selectedCourse + "' is already in the timetable and cannot be removed.");
                    }
                }
            });
            
            // If no courses were added, add a message
            if (courseDropdown.getItemCount() == 0) {
                courseDropdown.addItem("No available courses");
                resolveBtn.setEnabled(false);
            } else {
                // Select the current course by default if not in timetable or the first available course
                boolean currentCourseSelected = false;
                
                if (!coursesInTimetable.contains(currentCourseCode)) {
                    courseDropdown.setSelectedItem(currentCourseCode);
                    currentCourseSelected = true;
                    resolveBtn.setEnabled(true);
                }
                
                if (!currentCourseSelected) {
                    // Try to select the first non-timetable course
                    for (int i = 0; i < courseDropdown.getItemCount(); i++) {
                        String course = courseDropdown.getItemAt(i).toString();
                        // Strip "(in timetable)" suffix if present
                        if (course.endsWith(" (in timetable)")) {
                            course = course.substring(0, course.indexOf(" (in timetable)"));
                        }
                        
                        if (!coursesInTimetable.contains(course)) {
                            courseDropdown.setSelectedIndex(i);
                            resolveBtn.setEnabled(true);
                            break;
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating course dropdown: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadAvailableCourses() {
        newCourseDropdown.removeAllItems();
        
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            
            // Get all courses that are not in the timetable
            PreparedStatement ps = conn.prepareStatement(
                "SELECT C.courseCode FROM Course C " +
                "WHERE C.courseCode NOT IN (SELECT courseCode FROM TimeTable WHERE userId = ?)"
            );
            ps.setString(1, UserSession.getInstance().getUserId());
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                newCourseDropdown.addItem(rs.getString("courseCode"));
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading available courses: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void resolveConflict() {
        int selectedRow = conflictTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a conflict to resolve");
            return;
        }
        
        int conflictId = (int) conflictTable.getValueAt(selectedRow, 0);
        String selectedCourse = (String) courseDropdown.getSelectedItem();
        String action = (String) actionDropdown.getSelectedItem();
        
        // Clean up the selected course name if it has the "(in timetable)" suffix
        if (selectedCourse != null && selectedCourse.contains(" (in timetable)")) {
            selectedCourse = selectedCourse.substring(0, selectedCourse.indexOf(" (in timetable)"));
        }
        
        if (selectedCourse == null || action == null || selectedCourse.equals("No available courses")) {
            JOptionPane.showMessageDialog(this, "Please select a valid course and action");
            return;
        }
        
        // Check if selected course is in timetable
        if (coursesInTimetable.contains(selectedCourse)) {
            JOptionPane.showMessageDialog(this, 
                "Cannot remove or replace course '" + selectedCourse + "' as it's already saved in the timetable. " +
                "Please select a course that is not in the timetable.");
            return;
        }
        
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);
            
            // Get the conflict details
            Map<String, Object> selectedConflict = null;
            for (Map<String, Object> conflict : conflicts) {
                if ((int)conflict.get("conflictId") == conflictId) {
                    selectedConflict = conflict;
                    break;
                }
            }
            
            if (selectedConflict == null) return;
            
            // Remove the selected course from timetable
            PreparedStatement psRemove = conn.prepareStatement(
                "DELETE FROM TimeTable WHERE userId = ? AND courseCode = ?"
            );
            psRemove.setString(1, UserSession.getInstance().getUserId());
            psRemove.setString(2, selectedCourse);
            psRemove.executeUpdate();
            
            // If replacing with another course
            if (action.equals("Replace with Another Course")) {
                String newCourse = (String) newCourseDropdown.getSelectedItem();
                if (newCourse == null) {
                    JOptionPane.showMessageDialog(this, "Please select a new course");
                    conn.rollback();
                    return;
                }
                
                // Handle the set of slot IDs
                Set<Integer> slotIds = (Set<Integer>) selectedConflict.get("slotIds");
                if (slotIds == null || slotIds.isEmpty()) {
                    // Fallback to single slotId if set is not available
                    int slotId = (int) selectedConflict.get("slotId");
                    slotIds = new HashSet<>();
                    slotIds.add(slotId);
                }
                
                // Check if the new course would cause conflicts with any of the slots
                boolean wouldCauseConflict = false;
                for (Integer slotId : slotIds) {
                    if (wouldCauseConflict(newCourse, slotId)) {
                        wouldCauseConflict = true;
                        break;
                    }
                }
                
                if (wouldCauseConflict) {
                    JOptionPane.showMessageDialog(this, 
                        "The selected course would still cause conflicts. Please choose another course.");
                    conn.rollback();
                    return;
                }
                
                // Get instructor and class for the new course
                PreparedStatement psNewCourse = conn.prepareStatement(
                    "SELECT C.assignedInstructId, CC.roomId FROM Course C " +
                    "JOIN CourseClass CC ON C.courseCode = CC.courseCode " +
                    "WHERE C.courseCode = ? LIMIT 1"
                );
                psNewCourse.setString(1, newCourse);
                ResultSet rsNewCourse = psNewCourse.executeQuery();
                
                if (rsNewCourse.next()) {
                    String instructId = rsNewCourse.getString("assignedInstructId");
                    String classId = rsNewCourse.getString("roomId");
                    
                    // Add the new course for each slot
                    for (Integer slotId : slotIds) {
                        // Get the next available ID for timetable
                        int nextTimetableId = getNextTimetableId(conn);
                        
                        // Add new course to timetable with the new timetable ID
                        PreparedStatement psAdd = conn.prepareStatement(
                            "INSERT INTO TimeTable (timetableId, userId, courseCode, instructId, classId, slotId) " +
                            "VALUES (?, ?, ?, ?, ?, ?)"
                        );
                        psAdd.setInt(1, nextTimetableId);
                        psAdd.setString(2, UserSession.getInstance().getUserId());
                        psAdd.setString(3, newCourse);
                        psAdd.setString(4, instructId);
                        psAdd.setString(5, classId);
                        psAdd.setInt(6, slotId);
                        psAdd.executeUpdate();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Could not find details for the new course");
                    conn.rollback();
                    return;
                }
            }
            
            // Delete all related conflicts for this course
            // For both original and clashing entries
            if (conflictId > 0) {
                // For original conflicts, delete by conflict ID
                PreparedStatement psDeleteConflict = conn.prepareStatement(
                    "DELETE FROM Conflict WHERE conflictId = ?"
                );
                psDeleteConflict.setInt(1, conflictId);
                psDeleteConflict.executeUpdate();
                
                // Also delete any other conflicts involving this course
                PreparedStatement psDeleteCourseConflicts = conn.prepareStatement(
                    "DELETE FROM Conflict WHERE userId = ? AND courseCode = ?"
                );
                psDeleteCourseConflicts.setString(1, UserSession.getInstance().getUserId());
                psDeleteCourseConflicts.setString(2, selectedCourse);
                psDeleteCourseConflicts.executeUpdate();
            } else {
                // For clashing entries (negative IDs), we need to delete by course and user
                PreparedStatement psDeleteCourseConflicts = conn.prepareStatement(
                    "DELETE FROM Conflict WHERE userId = ? AND courseCode = ?"
                );
                psDeleteCourseConflicts.setString(1, UserSession.getInstance().getUserId());
                psDeleteCourseConflicts.setString(2, selectedCourse);
                psDeleteCourseConflicts.executeUpdate();
            }
            
            conn.commit();
            
            // Check if there are any remaining conflicts
            PreparedStatement psCheckConflicts = conn.prepareStatement(
                "SELECT COUNT(*) FROM Conflict WHERE userId = ?"
            );
            psCheckConflicts.setString(1, UserSession.getInstance().getUserId());
            ResultSet rsCount = psCheckConflicts.executeQuery();
            
            
            if (rsCount.next() && rsCount.getInt(1) == 0) {
                // No more conflicts, save the final timetable
                saveFinalTimetable();
                
                // Get reference to the parent TimeTableGenerator frame
               TimeTableGenerator parentFrame = null;
               for (Window window : Window.getWindows()) {
                  if (window instanceof TimeTableGenerator && window.isVisible()) {
                     parentFrame = (TimeTableGenerator) window;
                     break;
                    }
                }
               
                // Refresh the parent frame if found
                if (parentFrame != null) {
                   parentFrame.refreshAfterConflictResolution();
                }
                
                // Inform user and close window
                JOptionPane.showMessageDialog(this, "All conflicts resolved successfully! Timetable has been saved.");
                this.dispose();
            } else {
                // Show message for successful resolution of this conflict
                JOptionPane.showMessageDialog(this, "Conflict resolved successfully!");
                
                // Reload courses in timetable and conflicts to update the UI
                loadCoursesInTimetable();
                loadConflictsFromDatabase();
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error resolving conflict: " + e.getMessage());
            e.printStackTrace();
            try {
                Connection conn = DatabaseConnection.getInstance().getConnection();
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                Connection conn = DatabaseConnection.getInstance().getConnection();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    // Helper method to get the next available timetable ID
    private int getNextTimetableId(Connection conn) throws SQLException {
        int nextId = 1;
        
        PreparedStatement ps = conn.prepareStatement(
            "SELECT MAX(timetableId) FROM TimeTable"
        );
        ResultSet rs = ps.executeQuery();
        
        if (rs.next() && rs.getObject(1) != null) {
            nextId = rs.getInt(1) + 1;
        }
        
        return nextId;
    }
    
    private boolean wouldCauseConflict(String courseCode, int slotId) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            
            // Check if this course's instructor has another class at the same time
            PreparedStatement psInstructor = conn.prepareStatement(
                "SELECT COUNT(*) FROM TimeTable T " +
                "JOIN Course C ON T.courseCode = C.courseCode " +
                "WHERE T.userId = ? AND T.slotId = ? AND C.assignedInstructId = " +
                "(SELECT assignedInstructId FROM Course WHERE courseCode = ?)"
            );
            psInstructor.setString(1, UserSession.getInstance().getUserId());
            psInstructor.setInt(2, slotId);
            psInstructor.setString(3, courseCode);
            ResultSet rsInstructor = psInstructor.executeQuery();
            
            if (rsInstructor.next() && rsInstructor.getInt(1) > 0) {
                return true; // Would cause instructor conflict
            }
            
            // Check if this course's room is being used at the same time
            PreparedStatement psRoom = conn.prepareStatement(
                "SELECT COUNT(*) FROM TimeTable T " +
                "WHERE T.userId = ? AND T.slotId = ? AND T.classId IN " +
                "(SELECT roomId FROM CourseClass WHERE courseCode = ?)"
            );
            psRoom.setString(1, UserSession.getInstance().getUserId());
            psRoom.setInt(2, slotId);
            psRoom.setString(3, courseCode);
            ResultSet rsRoom = psRoom.executeQuery();
            
            if (rsRoom.next() && rsRoom.getInt(1) > 0) {
                return true; // Would cause room conflict
            }
            
            return false; // No conflicts
            
        } catch (Exception e) {
            e.printStackTrace();
            return true; // Assume conflict if error
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
                new ConflictResolution().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
