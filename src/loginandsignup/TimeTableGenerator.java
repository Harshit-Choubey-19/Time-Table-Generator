package loginandsignup;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.util.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.border.EmptyBorder;

public class TimeTableGenerator extends javax.swing.JFrame {
    private JTextField[][] timeTableFields;
    private JComboBox<String> courseSelection;
    private JButton generateSuggestionBtn, newSuggestionBtn, saveButton, clearButton, addButton;
    private List<Map<String, String>> addedCourses = new ArrayList<>();
    
    // Static variables for tracking course details and conflicts
    private static String courseCode;
    private static String instructId;
    private static String classId;
    private static int slotId;
    private static boolean hasConfliction;
    private static String conflictType;
    
    // For storing conflicts to resolve before saving
    private List<Map<String, Object>> conflicts = new ArrayList<>();

    private final String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
    private final String[] timeSlots = {
        "8:00-8:50 AM", "9:00-9:50 AM", "10:00-10:50 AM", "11:00-11:50 AM",
        "12:00-12:50 PM", "1:00-1:50 PM", "2:00-2:50 PM", "3:00-3:50 PM", "4:00-4:50 PM", "5:00-5:50 PM"
    };
    
    // Maps to track which cells have which courses and delete buttons
    private Map<JTextField, String> cellToCourseMappings = new HashMap<>();
    private Map<JTextField, JButton> cellToDeleteButtonMappings = new HashMap<>();

    public TimeTableGenerator() {
        setTitle("Time Table Generator - Time Table Builder");
        setSize(1000, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(createTopBar(), BorderLayout.NORTH);
        add(createTimeTableGrid(), BorderLayout.CENTER);
        add(createBottomControls(), BorderLayout.SOUTH);

        fetchAndSetCourses();
        loadExistingTimetable(); // Load existing timetable entries
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(0, 102, 102));
        top.setPreferredSize(new Dimension(1000, 50));

        JButton homeBtn = new JButton("Home");
        JButton logoutBtn = new JButton("Logout");

        homeBtn.addActionListener(e -> {
            Dashboard dashFrame = new Dashboard();
            dashFrame.setPreferredSize(new Dimension(1000, 750));
            dashFrame.setVisible(true);
            dashFrame.pack();
            dashFrame.setLocationRelativeTo(null);
            dispose();
        });

        logoutBtn.addActionListener(e -> {
            UserSession.clearSession();
            JOptionPane.showMessageDialog(new JFrame(), "Logout Successful!");
            Login loginFrame = new Login();
            loginFrame.setVisible(true);
            loginFrame.pack();
            loginFrame.setLocationRelativeTo(null);
            dispose();
        });

        top.add(homeBtn, BorderLayout.WEST);
        top.add(logoutBtn, BorderLayout.EAST);
        return top;
    }

    private JScrollPane createTimeTableGrid() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel grid = new JPanel(new GridLayout(timeSlots.length + 1, days.length + 1));
        timeTableFields = new JTextField[timeSlots.length][days.length];

        grid.add(new JLabel("Time Slots", SwingConstants.CENTER));
        for (String day : days) {
            JLabel label = new JLabel(day, SwingConstants.CENTER);
            label.setOpaque(true);
            label.setBackground(new Color(0, 102, 102));
            label.setForeground(Color.WHITE);
            grid.add(label);
        }

        for (int i = 0; i < timeSlots.length; i++) {
            grid.add(new JLabel(timeSlots[i], SwingConstants.CENTER));
            for (int j = 0; j < days.length; j++) {
                JPanel cellPanel = new JPanel(new BorderLayout());
                cellPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                
                timeTableFields[i][j] = new JTextField();
                timeTableFields[i][j].setEditable(false);
                timeTableFields[i][j].setBorder(null);
                
                cellPanel.add(timeTableFields[i][j], BorderLayout.CENTER);
                grid.add(cellPanel);
            }
        }
        
        mainPanel.add(grid, BorderLayout.CENTER);
        return new JScrollPane(mainPanel);
    }

    private JPanel createBottomControls() {
        JPanel bottom = new JPanel(new GridLayout(4, 2, 10, 10));
        bottom.setBorder(BorderFactory.createTitledBorder("Generate Time Table"));

        courseSelection = new JComboBox<>();
        generateSuggestionBtn = new JButton("Generate Suggestion");
        newSuggestionBtn = new JButton("New Suggestion");
        saveButton = new JButton("Save Timetable");
        clearButton = new JButton("Clear TimeTable");
        addButton = new JButton("Add to Timetable");
        
        // Initially hide the New Suggestion button
        newSuggestionBtn.setVisible(false);

        bottom.add(new JLabel("Select Course:"));
        bottom.add(courseSelection);
        bottom.add(addButton);
        bottom.add(clearButton);
        bottom.add(generateSuggestionBtn);
        bottom.add(newSuggestionBtn);
        bottom.add(saveButton);

        // Add action listeners for buttons
        generateSuggestionBtn.addActionListener(e -> generateSuggestion());
        newSuggestionBtn.addActionListener(e -> generateSuggestion());
        saveButton.addActionListener(e -> saveTimetable());
        clearButton.addActionListener(e -> clearTimeTable());
        addButton.addActionListener(e -> addCourseToTimetable());

        return bottom;
    }

    private void fetchAndSetCourses() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            
            // Get a list of course codes already added to this user's timetable
            Set<String> existingCourses = new HashSet<>();
            PreparedStatement psExisting = conn.prepareStatement(
                "SELECT DISTINCT courseCode FROM TimeTable WHERE userId = ?"
            );
            psExisting.setString(1, UserSession.getInstance().getUserId());
            ResultSet rsExisting = psExisting.executeQuery();
            
            while (rsExisting.next()) {
                existingCourses.add(rsExisting.getString("courseCode"));
            }
            
            // Now fetch all courses, excluding those already in the timetable
            PreparedStatement ps = conn.prepareStatement("SELECT courseCode FROM Course");
            ResultSet rs = ps.executeQuery();
            
            courseSelection.removeAllItems();
            while (rs.next()) {
                String courseCode = rs.getString("courseCode");
                if (!existingCourses.contains(courseCode)) {
                    courseSelection.addItem(courseCode);
                }
            }
            
            toggleAddButton();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error fetching courses: " + e.getMessage());
        }
    }

    private void generateSuggestion() {
        // Clear the current timetable
        clearTimeTable();
        
        // Hide generate suggestion button and show new suggestion button
        generateSuggestionBtn.setVisible(false);
        newSuggestionBtn.setVisible(true);
        
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            
            // Fetch all available courses
            PreparedStatement ps = conn.prepareStatement(
                "SELECT courseCode FROM Course ORDER BY RAND()"
            );
            ResultSet rs = ps.executeQuery();
            
            List<String> availableCourses = new ArrayList<>();
            while (rs.next()) {
                availableCourses.add(rs.getString("courseCode"));
            }
            
            if (availableCourses.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No courses available for suggestion");
                return;
            }
            
            // Try to add up to 4 non-conflicting courses
            int coursesAdded = 0;
            for (String course : availableCourses) {
                if (coursesAdded >= 4) break;
                
                if (addCourseToTimetableWithoutConflict(course)) {
                    coursesAdded++;
                // Remove the course from dropdown
                removeFromCourseSelection(course);
                }
            }
            
            if (coursesAdded == 0) {
                JOptionPane.showMessageDialog(this, "Could not find any non-conflicting courses. Please try again.");
            } else {
                JOptionPane.showMessageDialog(this, "Added " + coursesAdded + " courses to the timetable. You can now make changes.");
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error generating suggestion: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void removeFromCourseSelection(String courseCode) {
    for (int i = 0; i < courseSelection.getItemCount(); i++) {
        if (courseSelection.getItemAt(i).equals(courseCode)) {
            courseSelection.removeItemAt(i);
            break;
        }
    }
    toggleAddButton();
}
    
    private boolean addCourseToTimetableWithoutConflict(String courseCode) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            
            // Fetch course details
            PreparedStatement psCourse = conn.prepareStatement(
                "SELECT C.courseCode, C.assignedInstructId, I.name AS instructorName " +
                "FROM Course C " +
                "JOIN Instructor I ON C.assignedInstructId = I.instructId " +
                "WHERE C.courseCode = ?"
            );
            psCourse.setString(1, courseCode);
            ResultSet rsCourse = psCourse.executeQuery();
            
            if (rsCourse.next()) {
                instructId = rsCourse.getString("assignedInstructId");
                String instructorName = rsCourse.getString("instructorName");
                
                // Fetch all classrooms associated with this course
                PreparedStatement psClasses = conn.prepareStatement(
                    "SELECT roomId FROM CourseClass WHERE courseCode = ? ORDER BY RAND()"
                );
                psClasses.setString(1, courseCode);
                ResultSet rsClasses = psClasses.executeQuery();
                
                // Store all rooms for this course
                List<String> classRooms = new ArrayList<>();
                Map<String, Boolean> isLabMap = new HashMap<>();
                
                while (rsClasses.next()) {
                    String room = rsClasses.getString("roomId");
                    classRooms.add(room);
                    
                    // Fetch classroom info to check if it's a lab
                    PreparedStatement psClassroom = conn.prepareStatement(
                        "SELECT isLab FROM Classroom WHERE classId = ?"
                    );
                    psClassroom.setString(1, room);
                    ResultSet rsClassroom = psClassroom.executeQuery();
                    if (rsClassroom.next()) {
                        isLabMap.put(room, rsClassroom.getBoolean("isLab"));
                    } else {
                        isLabMap.put(room, false);
                    }
                }
                
                // No classrooms found for this course
                if (classRooms.isEmpty()) {
                    return false;
                }
                
                // Fetch all time slots for this course
                PreparedStatement psSlots = conn.prepareStatement(
                    "SELECT slotId, startTime, endTime FROM TimeSlot WHERE courseCode = ? ORDER BY RAND()"
                );
                psSlots.setString(1, courseCode);
                ResultSet rsSlots = psSlots.executeQuery();
                
                // Store all slots info
                List<Map<String, Object>> allSlots = new ArrayList<>();
                
                while (rsSlots.next()) {
                    int currentSlotId = rsSlots.getInt("slotId");
                    String startTime = rsSlots.getTime("startTime").toString();
                    String endTime = rsSlots.getTime("endTime").toString();
                    
                    // For each time slot, find all associated days
                    PreparedStatement psDays = conn.prepareStatement(
                        "SELECT day FROM TimeSlotDay WHERE slotId = ?"
                    );
                    psDays.setInt(1, currentSlotId);
                    ResultSet rsDays = psDays.executeQuery();
                    
                    List<String> slotDays = new ArrayList<>();
                    while (rsDays.next()) {
                        slotDays.add(rsDays.getString("day"));
                    }
                    
                    if (!slotDays.isEmpty()) {
                        Map<String, Object> slotInfo = new HashMap<>();
                        slotInfo.put("slotId", currentSlotId);
                        slotInfo.put("startTime", startTime);
                        slotInfo.put("endTime", endTime);
                        slotInfo.put("days", slotDays);
                        allSlots.add(slotInfo);
                    }
                }
                
                // No time slots found for this course
                if (allSlots.isEmpty()) {
                    return false;
                }
                
                // Try each time slot until we find a non-conflicting arrangement
                for (Map<String, Object> slotInfo : allSlots) {
                    int currentSlotId = (int) slotInfo.get("slotId");
                    String startTime = (String) slotInfo.get("startTime");
                    @SuppressWarnings("unchecked")
                    List<String> slotDays = (List<String>) slotInfo.get("days");
                    
                    // Find appropriate time index based on actual start time
                    int timeIndex = findTimeSlotIndex(startTime);
                    
                    // Determine which classroom to use based on the time slot
                    boolean isAfternoonSession = timeIndex >= 5; // After 1:00 PM
                    
                    // Check if this time slot would cause conflicts
                    boolean hasConflict = false;
                    List<JTextField> cellsToUpdate = new ArrayList<>();
                    String selectedRoom = null;
                    
                    // Choose a classroom
                    for (String room : classRooms) {
                        boolean isLab = isLabMap.get(room);
                        if ((isAfternoonSession && isLab) || (!isAfternoonSession && !isLab) || classRooms.size() == 1) {
                            selectedRoom = room;
                            break;
                        }
                    }
                    
                    if (selectedRoom == null && !classRooms.isEmpty()) {
                        selectedRoom = classRooms.get(0); // Fallback to first room
                    }
                    
                    // Now check if this arrangement would create conflicts
                    for (String day : slotDays) {
                        int dayIndex = Arrays.asList(days).indexOf(day);
                        
                        if (dayIndex >= 0 && timeIndex < timeSlots.length) {
                            JTextField cell = timeTableFields[timeIndex][dayIndex];
                            if (!cell.getText().isEmpty()) {
                                hasConflict = true;
                                break;
                            } else {
                                cellsToUpdate.add(cell);
                            }
                        }
                    }
                    
                    // If no conflicts, add the course to these cells
                    if (!hasConflict && selectedRoom != null) {
                        boolean isLab = isLabMap.get(selectedRoom);
                        String label = courseCode + " (" + instructorName + ") Room: " + selectedRoom;
                        if (isLab) {
                            label += " [LAB]";
                        }
                        
                        // Add the course to all relevant cells
                        for (JTextField cell : cellsToUpdate) {
                            cell.setText(label);
                            if (isLab) {
                                cell.setForeground(new Color(0, 100, 0)); // Dark green for labs
                            }
                            
                            // Add delete button to this cell
                            addDeleteButtonToCell(cell, findCellPosition(cell));
                            
                            // Remember which course is in this cell
                            cellToCourseMappings.put(cell, courseCode);
                        }
                        
                        // Add course to our tracked list
                        Map<String, String> entry = new HashMap<>();
                        entry.put("courseCode", courseCode);
                        entry.put("instructId", instructId);
                        entry.put("instructorName", instructorName);
                        entry.put("classId", selectedRoom);
                        entry.put("slotId", String.valueOf(currentSlotId));
                        entry.put("isLab", String.valueOf(isLab));
                        addedCourses.add(entry);
                        
                        // Course added successfully
                        return true;
                    }
                }
            }
            
            // Could not add the course
            return false;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void addDeleteButtonToCell(JTextField cell, int[] position) {
        if (position == null) return;
        
        // Create delete button
        JButton deleteBtn = new JButton("✖");
        deleteBtn.setForeground(Color.RED);
        deleteBtn.setFont(new Font("Arial", Font.BOLD, 10));
        deleteBtn.setMargin(new Insets(0, 0, 0, 0));
        deleteBtn.setPreferredSize(new Dimension(16, 16));
        deleteBtn.setToolTipText("Remove this course");
        
        // Get the parent of the cell (which is a JPanel)
        Container parent = cell.getParent();
        if (parent instanceof JPanel) {
            JPanel cellPanel = (JPanel) parent;
            deleteBtn.addActionListener(e -> removeCourseFromCell(position[0], position[1]));
            
            // Remove any existing delete button
            for (Component c : cellPanel.getComponents()) {
                if (c instanceof JButton) {
                    cellPanel.remove(c);
                    break;
                }
            }
            
            // Add the delete button to the cell panel
            cellPanel.add(deleteBtn, BorderLayout.EAST);
            cellPanel.revalidate();
            cellPanel.repaint();
            
            // Store the mapping
            cellToDeleteButtonMappings.put(cell, deleteBtn);
        }
    }
    
    private void removeCourseFromCell(int timeIndex, int dayIndex) {
        JTextField cell = timeTableFields[timeIndex][dayIndex];
        String courseCode = cellToCourseMappings.get(cell);
        
        if (courseCode != null) {
            // Remove all instances of this course from the timetable
            for (int i = 0; i < timeSlots.length; i++) {
                for (int j = 0; j < days.length; j++) {
                    JTextField currentCell = timeTableFields[i][j];
                    if (cellToCourseMappings.containsKey(currentCell) && 
                        cellToCourseMappings.get(currentCell).equals(courseCode)) {
                        
                        // Clear the cell
                        currentCell.setText("");
                        currentCell.setForeground(Color.BLACK);
                        currentCell.setBackground(Color.WHITE);
                        
                        // Remove delete button
                        JButton deleteBtn = cellToDeleteButtonMappings.get(currentCell);
                        if (deleteBtn != null) {
                            Container parent = currentCell.getParent();
                            if (parent instanceof JPanel) {
                                JPanel cellPanel = (JPanel) parent;
                                cellPanel.remove(deleteBtn);
                                cellPanel.revalidate();
                                cellPanel.repaint();
                            }
                            cellToDeleteButtonMappings.remove(currentCell);
                        }
                        
                        // Clear mapping
                        cellToCourseMappings.remove(currentCell);
                    }
                }
            }
            
            // Remove from addedCourses list
            Iterator<Map<String, String>> iterator = addedCourses.iterator();
            while (iterator.hasNext()) {
                Map<String, String> entry = iterator.next();
                if (entry.get("courseCode").equals(courseCode)) {
                    iterator.remove();
                }
            }
            
            // Add the course back to the dropdown
            boolean courseExists = false;
            for (int i = 0; i < courseSelection.getItemCount(); i++) {
                if (courseSelection.getItemAt(i).equals(courseCode)) {
                    courseExists = true;
                    break;
                }
            }
            
            if (!courseExists) {
                courseSelection.addItem(courseCode);
            }
            
            toggleAddButton();
        }
    }
    
    private int[] findCellPosition(JTextField cell) {
        for (int i = 0; i < timeSlots.length; i++) {
            for (int j = 0; j < days.length; j++) {
                if (timeTableFields[i][j] == cell) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    private void addCourseToTimetable() {
        courseCode = (String) courseSelection.getSelectedItem();
        if (courseCode == null) return;
        
        hasConfliction = false;
        
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            
            // Fetch course details
            PreparedStatement psCourse = conn.prepareStatement(
                "SELECT C.courseCode, C.assignedInstructId, I.name AS instructorName " +
                "FROM Course C " +
                "JOIN Instructor I ON C.assignedInstructId = I.instructId " +
                "WHERE C.courseCode = ?"
            );
            psCourse.setString(1, courseCode);
            ResultSet rsCourse = psCourse.executeQuery();
            
            if (rsCourse.next()) {
                instructId = rsCourse.getString("assignedInstructId");
                String instructorName = rsCourse.getString("instructorName");
                
                // Fetch all classrooms associated with this course
                PreparedStatement psClasses = conn.prepareStatement(
                    "SELECT roomId FROM CourseClass WHERE courseCode = ?"
                );
                psClasses.setString(1, courseCode);
                ResultSet rsClasses = psClasses.executeQuery();
                
                // Store all rooms for this course
                List<String> classRooms = new ArrayList<>();
                Map<String, Boolean> isLabMap = new HashMap<>();
                
                while (rsClasses.next()) {
                    String room = rsClasses.getString("roomId");
                    classRooms.add(room);
                    
                    // Fetch classroom info to check if it's a lab
                    PreparedStatement psClassroom = conn.prepareStatement(
                        "SELECT isLab FROM Classroom WHERE classId = ?"
                    );
                    psClassroom.setString(1, room);
                    ResultSet rsClassroom = psClassroom.executeQuery();
                    if (rsClassroom.next()) {
                        isLabMap.put(room, rsClassroom.getBoolean("isLab"));
                    } else {
                        isLabMap.put(room, false);
                    }
                }
                
                // No classrooms found for this course
                if (classRooms.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No classrooms found for course: " + courseCode);
                    return;
                }
                
                // Fetch all time slots for this course
                PreparedStatement psSlots = conn.prepareStatement(
                    "SELECT slotId, startTime, endTime FROM TimeSlot WHERE courseCode = ?"
                );
                psSlots.setString(1, courseCode);
                ResultSet rsSlots = psSlots.executeQuery();
                
                // Store all slots info
                List<Map<String, Object>> allSlots = new ArrayList<>();
                
                while (rsSlots.next()) {
                    int currentSlotId = rsSlots.getInt("slotId");
                    String startTime = rsSlots.getTime("startTime").toString();
                    String endTime = rsSlots.getTime("endTime").toString();
                    
                    // For each time slot, find all associated days
                    PreparedStatement psDays = conn.prepareStatement(
                        "SELECT day FROM TimeSlotDay WHERE slotId = ?"
                    );
                    psDays.setInt(1, currentSlotId);
                    ResultSet rsDays = psDays.executeQuery();
                    
                    List<String> slotDays = new ArrayList<>();
                    while (rsDays.next()) {
                        slotDays.add(rsDays.getString("day"));
                    }
                    
                    if (!slotDays.isEmpty()) {
                        Map<String, Object> slotInfo = new HashMap<>();
                        slotInfo.put("slotId", currentSlotId);
                        slotInfo.put("startTime", startTime);
                        slotInfo.put("endTime", endTime);
                        slotInfo.put("days", slotDays);
                        allSlots.add(slotInfo);
                    }
                }
                
                // No time slots found for this course
                if (allSlots.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No time slots found for course: " + courseCode);
                    return;
                }
                
                // Now we have all classrooms and time slots for this course
                // Process each time slot
                int courseEntriesAdded = 0;
                
                for (Map<String, Object> slotInfo : allSlots) {
                    int currentSlotId = (int) slotInfo.get("slotId");
                    String startTime = (String) slotInfo.get("startTime");
                    @SuppressWarnings("unchecked")
                    List<String> slotDays = (List<String>) slotInfo.get("days");
                    
                    // Find appropriate time index based on actual start time
                    int timeIndex = findTimeSlotIndex(startTime);
                    
                    // Determine which classroom to use based on the time slot
                    // If it's after 1PM, it might be a lab session
                    boolean isAfternoonSession = timeIndex >= 5; // After 1:00 PM
                    
                    // For each day in this time slot
                    for (String day : slotDays) {
                        int dayIndex = Arrays.asList(days).indexOf(day);
                        
                        if (dayIndex >= 0 && timeIndex < timeSlots.length) {
                            // Try to assign classroom based on timing
                            for (String room : classRooms) {
                                boolean isLab = isLabMap.get(room);
                                
                                // Prefer lab rooms for afternoon sessions if available
                                if ((isAfternoonSession && isLab) || (!isAfternoonSession && !isLab) || classRooms.size() == 1) {
                                    String label = courseCode + " (" + instructorName + ") Room: " + room;
                                    if (isLab) {
                                        label += " [LAB]";
                                    }
                                    
                                    // Check for conflicts
                                    String existing = timeTableFields[timeIndex][dayIndex].getText();
                                    if (existing.isEmpty()) {
                                        timeTableFields[timeIndex][dayIndex].setText(label);
                                        if (isLab) {
                                            timeTableFields[timeIndex][dayIndex].setForeground(new Color(0, 100, 0)); // Dark green for labs
                                        }
                                        
                                        // Add delete button to this cell
                                        addDeleteButtonToCell(timeTableFields[timeIndex][dayIndex], new int[]{timeIndex, dayIndex});
                                        
                                        // Remember which course is in this cell
                                        cellToCourseMappings.put(timeTableFields[timeIndex][dayIndex], courseCode);
                                        
                                        // Add course to our tracked list
                                        Map<String, String> entry = new HashMap<>();
                                        entry.put("courseCode", courseCode);
                                        entry.put("instructId", instructId);
                                        entry.put("instructorName", instructorName);
                                        entry.put("classId", room);
                                        entry.put("slotId", String.valueOf(currentSlotId));
                                        entry.put("isLab", String.valueOf(isLab));
                                        addedCourses.add(entry);
                                        
                                        courseEntriesAdded++;
                                        break; // Use this classroom for this time slot
                                    } else {
                                        // Visual indication of conflict
                                        timeTableFields[timeIndex][dayIndex].setBackground(Color.RED);
                                        
                                        // Determine conflict type
                                        conflictType = "Time Slot Conflict";
                                        if (existing.contains("Room: " + room)) {
                                            conflictType = "Room Conflict";
                                        }
                                        
                                        // Save conflict to database
                                        saveConflict(courseCode, instructId, room, currentSlotId, day, timeIndex, conflictType);
                                        hasConfliction = true;
                                    }
                                }
                            }
                        }
                    }
                }
                
                // If any entries were added, remove course from dropdown
                if (courseEntriesAdded > 0) {
                    courseSelection.removeItem(courseCode);
                    toggleAddButton();
                } else if (!hasConfliction) {
                    JOptionPane.showMessageDialog(this, "Could not add course " + courseCode + " to timetable. Check classroom and time slot assignments.");
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding course: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private int findTimeSlotIndex(String startTime) {
        // Convert database time to match our time slots format
        String hour = startTime.split(":")[0];
        int hourInt = Integer.parseInt(hour);
        
        if (hourInt >= 8 && hourInt <= 17) {
            return hourInt - 8; // Map 8AM to index 0, 9AM to index 1, etc.
        }
        
        return 0; // Default to first slot if time not found
    }
    
    private void saveConflict(String courseCode, String instructId, String classId, int slotId, 
                             String day, int timeIndex, String conflictType) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            
            // Save to Conflict table
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Conflict (userId, courseCode, instructId, classId, slotId, conflictType) " +
                "VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, UserSession.getInstance().getUserId());
            ps.setString(2, courseCode);
            ps.setString(3, instructId);
            ps.setString(4, classId);
            ps.setInt(5, slotId);
            ps.setString(6, conflictType);
            ps.executeUpdate();
            
            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                int conflictId = generatedKeys.getInt(1);
                
                // Store conflict details for later resolution
                Map<String, Object> conflict = new HashMap<>();
                conflict.put("conflictId", conflictId);
                conflict.put("courseCode", courseCode);
                conflict.put("day", day);
                conflict.put("timeIndex", timeIndex);
                conflict.put("conflictType", conflictType);
                conflicts.add(conflict);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving conflict: " + e.getMessage());
        }
    }

    private void toggleAddButton() {
        addButton.setEnabled(courseSelection.getItemCount() > 0);
    }

    private void saveTimetable() {
        if (!conflicts.isEmpty()) {
            int option = JOptionPane.showConfirmDialog(this, 
                "There are " + conflicts.size() + " conflicts that need to be resolved before saving. " +
                "Would you like to resolve them now?", 
                "Conflicts Detected", JOptionPane.YES_NO_OPTION);
                
            if (option == JOptionPane.YES_OPTION) {
                ConflictResolution cFrame = new ConflictResolution(this);
                cFrame.setPreferredSize(new Dimension(1000, 750));
                cFrame.setVisible(true);
                cFrame.pack();
                cFrame.setLocationRelativeTo(null);
                return;
            } else {
                return; // Don't save until conflicts are resolved
            }
        }
        
        // Show loading indicator
        JDialog loadingDialog = new JDialog(this, "Saving...", false);
        JPanel loadingPanel = new JPanel(new BorderLayout());
        JLabel loadingLabel = new JLabel("Saving timetable, please wait...", SwingConstants.CENTER);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        
        loadingPanel.add(loadingLabel, BorderLayout.CENTER);
        loadingPanel.add(progressBar, BorderLayout.SOUTH);
        loadingPanel.setBorder(new EmptyBorder(20, 50, 20, 50));
        
        loadingDialog.add(loadingPanel);
        loadingDialog.pack();
        loadingDialog.setLocationRelativeTo(this);
        
        // Use SwingWorker to handle database operations in background
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                Connection conn = DatabaseConnection.getInstance().getConnection();
                try {
                    // Begin transaction
                    conn.setAutoCommit(false);
                    
                    // First delete existing timetable entries for this user
                    PreparedStatement psDelete = conn.prepareStatement(
                        "DELETE FROM TimeTable WHERE userId = ?"
                    );
                    psDelete.setString(1, UserSession.getInstance().getUserId());
                    psDelete.executeUpdate();
                    
                    // Insert courses into TimeTable
                    PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO TimeTable (userId, courseCode, instructId, classId, slotId) " +
                        "VALUES (?, ?, ?, ?, ?)"
                    );
                    
                    for (Map<String, String> entry : addedCourses) {
                        ps.setString(1, UserSession.getInstance().getUserId());
                        ps.setString(2, entry.get("courseCode"));
                        ps.setString(3, entry.get("instructId"));
                        ps.setString(4, entry.get("classId"));
                        ps.setInt(5, Integer.parseInt(entry.get("slotId")));
                        ps.executeUpdate();
                    }
                    
                    // Clean up Conflict table for this user
                    PreparedStatement psCleanup = conn.prepareStatement(
                        "DELETE FROM Conflict WHERE userId = ?"
                    );
                    psCleanup.setString(1, UserSession.getInstance().getUserId());
                    psCleanup.executeUpdate();
                    
                    // Commit transaction
                    conn.commit();
                    conn.setAutoCommit(true);
                    
                    return true;
                } catch (Exception e) {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    JOptionPane.showMessageDialog(TimeTableGenerator.this, 
                        "Error saving timetable: " + e.getMessage());
                    e.printStackTrace();
                    return false;
                }
            }
            
            @Override
            protected void done() {
                loadingDialog.dispose();
                
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(TimeTableGenerator.this, 
                            "Time Table Saved Successfully!");
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(TimeTableGenerator.this, 
                        "Error in save operation: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
        loadingDialog.setVisible(true);
    }
    
    private void clearTimeTable() {
        for (int i = 0; i < timeSlots.length; i++) {
            for (int j = 0; j < days.length; j++) {
                timeTableFields[i][j].setText("");
                timeTableFields[i][j].setBackground(Color.WHITE);
                timeTableFields[i][j].setForeground(Color.BLACK);
                
                // Remove delete buttons
                JButton deleteBtn = cellToDeleteButtonMappings.get(timeTableFields[i][j]);
                if (deleteBtn != null) {
                    Container parent = timeTableFields[i][j].getParent();
                    if (parent instanceof JPanel) {
                        JPanel cellPanel = (JPanel) parent;
                        cellPanel.remove(deleteBtn);
                        cellPanel.revalidate();
                        cellPanel.repaint();
                    }
                }
            }
        }
        
        // Clear mappings
        cellToCourseMappings.clear();
        cellToDeleteButtonMappings.clear();
        
        // Reload the course selection dropdown
        courseSelection.removeAllItems();
        fetchAndSetCourses();
        
        // Clear our tracking lists
        addedCourses.clear();
        conflicts.clear();
        
        // Reset suggestion buttons
        generateSuggestionBtn.setVisible(true);
        newSuggestionBtn.setVisible(false);
        
        // Clear conflict table for this user
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM Conflict WHERE userId = ?"
            );
            ps.setString(1, UserSession.getInstance().getUserId());
            ps.executeUpdate();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error clearing conflicts: " + e.getMessage());
        }
    }
    
    // Add this method to load existing timetable entries when the form opens
    private void loadExistingTimetable() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String userId = UserSession.getInstance().getUserId();
            
            // Query to fetch all timetable entries for this user
            PreparedStatement ps = conn.prepareStatement(
                "SELECT t.courseCode, t.instructId, t.classId, t.slotId, " +
                "i.name AS instructorName, c.isLab " +
                "FROM TimeTable t " +
                "JOIN Instructor i ON t.instructId = i.instructId " +
                "JOIN Classroom c ON t.classId = c.classId " +
                "WHERE t.userId = ?"
            );
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            
            // Process each existing timetable entry
            while (rs.next()) {
                String courseCode = rs.getString("courseCode");
                String instructId = rs.getString("instructId");
                String instructorName = rs.getString("instructorName");
                String classId = rs.getString("classId");
                int slotId = rs.getInt("slotId");
                boolean isLab = rs.getBoolean("isLab");
                
                // Create an entry for our tracking list
                Map<String, String> entry = new HashMap<>();
                entry.put("courseCode", courseCode);
                entry.put("instructId", instructId);
                entry.put("instructorName", instructorName);
                entry.put("classId", classId);
                entry.put("slotId", String.valueOf(slotId));
                entry.put("isLab", String.valueOf(isLab));
                addedCourses.add(entry);
                
                // Now fetch the time slot details to display in the grid
                PreparedStatement psSlot = conn.prepareStatement(
                    "SELECT startTime FROM TimeSlot WHERE slotId = ?"
                );
                psSlot.setInt(1, slotId);
                ResultSet rsSlot = psSlot.executeQuery();
                
                if (rsSlot.next()) {
                    String startTime = rsSlot.getTime("startTime").toString();
                    int timeIndex = findTimeSlotIndex(startTime);
                    
                    // Fetch the days for this time slot
                    PreparedStatement psDays = conn.prepareStatement(
                        "SELECT day FROM TimeSlotDay WHERE slotId = ?"
                    );
                    psDays.setInt(1, slotId);
                    ResultSet rsDays = psDays.executeQuery();
                    
                    // For each day associated with this time slot
                    while (rsDays.next()) {
                        String day = rsDays.getString("day");
                        int dayIndex = Arrays.asList(days).indexOf(day);
                        
                        if (dayIndex >= 0 && timeIndex < timeSlots.length) {
                            JTextField cell = timeTableFields[timeIndex][dayIndex];
                            String label = courseCode + " (" + instructorName + ") Room: " + classId;
                            if (isLab) {
                                label += " [LAB]";
                                cell.setForeground(new Color(0, 100, 0)); // Dark green for labs
                            }
                            cell.setText(label);
                            
                            // Add delete button to this cell
                            addDeleteButtonToCell(cell, new int[]{timeIndex, dayIndex});
                            
                            // Remember which course is in this cell
                            cellToCourseMappings.put(cell, courseCode);
                        }
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading existing timetable: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void refreshAfterConflictResolution() {
    // Clear existing conflicts
    conflicts.clear();
    
    // Update the UI state to reflect resolved conflicts
    for (int i = 0; i < timeSlots.length; i++) {
        for (int j = 0; j < days.length; j++) {
            // Reset any red background (visual conflict indicator)
            timeTableFields[i][j].setBackground(Color.WHITE);
        }
    }
    
    // Re-load the timetable from the database
    // This ensures we only display the current saved state
    clearTimeTable();
    loadExistingTimetable();
    
    // Check if any conflicts still exist in the database
    try {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(
            "SELECT COUNT(*) FROM Conflict WHERE userId = ?"
        );
        ps.setString(1, UserSession.getInstance().getUserId());
        ResultSet rs = ps.executeQuery();
        
        if (rs.next() && rs.getInt(1) > 0) {
            // There are still conflicts in the database
            // Fetch them to update our conflicts list
            PreparedStatement psConflicts = conn.prepareStatement(
                "SELECT c.conflictId, c.courseCode, c.conflictType, ts.startTime, tsd.day " +
                "FROM Conflict c " +
                "JOIN TimeSlot ts ON c.slotId = ts.slotId " +
                "JOIN TimeSlotDay tsd ON ts.slotId = tsd.slotId " +
                "WHERE c.userId = ?"
            );
            psConflicts.setString(1, UserSession.getInstance().getUserId());
            ResultSet rsConflicts = psConflicts.executeQuery();
            
            while (rsConflicts.next()) {
                int conflictId = rsConflicts.getInt("conflictId");
                String courseCode = rsConflicts.getString("courseCode");
                String conflictType = rsConflicts.getString("conflictType");
                String startTime = rsConflicts.getTime("startTime").toString();
                String day = rsConflicts.getString("day");
                
                // Find the time and day indices
                int timeIndex = findTimeSlotIndex(startTime);
                int dayIndex = Arrays.asList(days).indexOf(day);
                
                if (dayIndex >= 0 && timeIndex < timeSlots.length) {
                    // Mark this cell as having a conflict
                    timeTableFields[timeIndex][dayIndex].setBackground(Color.RED);
                    
                    // Store conflict details for later resolution
                    Map<String, Object> conflict = new HashMap<>();
                    conflict.put("conflictId", conflictId);
                    conflict.put("courseCode", courseCode);
                    conflict.put("day", day);
                    conflict.put("timeIndex", timeIndex);
                    conflict.put("conflictType", conflictType);
                    conflicts.add(conflict);
                }
            }
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error checking for conflicts: " + e.getMessage());
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
                new TimeTableGenerator().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
