package loginandsignup;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class CourseManagement extends javax.swing.JFrame {
    private JTextField codeField,nameField,instructorField;
    private JCheckBox labCheckbox;
    
    public CourseManagement() {
        setTitle("Course Management - Time Table Builder");
        setSize(800, 500);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Navbar Panel
        JPanel navbar = new JPanel(new BorderLayout());
        navbar.setBackground(new Color(0, 102, 102));
        navbar.setPreferredSize(new Dimension(800, 50));
        
        JButton homeBtn = new JButton("Home");
        homeBtn.setBackground(Color.WHITE);
        
        homeBtn.addActionListener(e -> {
            Dashboard dashFrame = new Dashboard();
            dashFrame.setPreferredSize(new Dimension(1000, 750));
            dashFrame.setVisible(true);
            dashFrame.pack();
            dashFrame.setLocationRelativeTo(null);
            dispose(); // Close Course Management window
        });
        
        JButton viewBtn = new JButton("View Courses");
        viewBtn.setBackground(Color.WHITE);
        viewBtn.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
              ViewCourses viewFrame = new ViewCourses();
              viewFrame.setVisible(true);
              viewFrame.setLocationRelativeTo(null);
            }
        });        
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);
        leftPanel.add(viewBtn);
        leftPanel.add(homeBtn);        
        
        JButton settingsBtn = new JButton("Logout");
        settingsBtn.setBackground(Color.WHITE);
        
        settingsBtn.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
               UserSession.clearSession();
               JOptionPane.showMessageDialog(new JFrame(), "Logout Successfull!");
                Login loginFrame = new Login();
                loginFrame.setVisible(true);
                loginFrame.pack();
                loginFrame.setLocationRelativeTo(null); 
                dispose();
           } 
        });
        
        rightPanel.add(settingsBtn);
        
        navbar.add(homeBtn, BorderLayout.WEST);
        navbar.add(rightPanel, BorderLayout.EAST);
        
        // Course Management Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        
        contentPanel.add(leftPanel, BorderLayout.WEST);
        
        JLabel titleLabel = new JLabel("Course Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 102, 102));
        
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        JLabel codeLabel = new JLabel("Course Code:");
         codeField = new JTextField();
        JLabel nameLabel = new JLabel("Course Name:");
         nameField = new JTextField();
        JLabel instructorLabel = new JLabel("Assigned Instructor ID:");
         instructorField = new JTextField();
        JLabel labLabel = new JLabel("Consist Lab:");
         labCheckbox = new JCheckBox();         
        
        formPanel.add(codeLabel);
        formPanel.add(codeField);
        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(instructorLabel);
        formPanel.add(instructorField);
        formPanel.add(labLabel);
        formPanel.add(labCheckbox);
        
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Course");
        addButton.addActionListener(this::addCourse);
        JButton deleteButton = new JButton("Delete Course");
        deleteButton.addActionListener(this::deleteCourse);
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(navbar, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        add(mainPanel);
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

private void addCourse(ActionEvent e) {
    String courseCode = codeField.getText().trim();
    String courseName = nameField.getText().trim();
    String instructorId = instructorField.getText().trim();
    boolean isLab = labCheckbox.isSelected();

    if (courseCode.isEmpty() || courseName.isEmpty() || instructorId.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter all fields", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection is not established.", "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if course already exists
        String checkQuery = "SELECT * FROM Course WHERE courseCode = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
        checkStmt.setString(1, courseCode);
        ResultSet rs = checkStmt.executeQuery();

        if (rs.next()) {
            JOptionPane.showMessageDialog(this, "Course already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        //check isntructor is present or not with that instructorid
        String checkQuery2 = "SELECT * FROM Instructor WHERE instructId = ?";
        PreparedStatement checkStmt2 = conn.prepareStatement(checkQuery2);
        checkStmt2.setString(1, instructorId);
        ResultSet rs2 = checkStmt2.executeQuery();

        if (!rs2.next()) {
            JOptionPane.showMessageDialog(this, "Please enter valid instructor ID!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }        

        // Insert into Course table
        String insertQuery = "INSERT INTO Course (courseCode, name, assignedInstructId, consistLab) VALUES (?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(insertQuery);
        stmt.setString(1, courseCode);
        stmt.setString(2, courseName);
        stmt.setString(3, instructorId);
        stmt.setBoolean(4, isLab);
        int rowsInserted = stmt.executeUpdate();

        if (rowsInserted > 0) {
            JOptionPane.showMessageDialog(this, "Course added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            codeField.setText("");
            nameField.setText("");
            instructorField.setText("");
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Error adding course: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}


private void deleteCourse(ActionEvent e) {
    String courseCode = codeField.getText().trim();

    if (courseCode.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter Course Code to delete", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection is not established.", "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if course exists
        String checkQuery = "SELECT * FROM Course WHERE courseCode = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
        checkStmt.setString(1, courseCode);
        ResultSet rs = checkStmt.executeQuery();

        if (!rs.next()) {
            JOptionPane.showMessageDialog(this, "Course not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Delete from Course table
        String deleteQuery = "DELETE FROM Course WHERE courseCode = ?";
        PreparedStatement stmt = conn.prepareStatement(deleteQuery);
        stmt.setString(1, courseCode);
        int rowsDeleted = stmt.executeUpdate();

        if (rowsDeleted > 0) {
            JOptionPane.showMessageDialog(this, "Course deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            codeField.setText("");
            nameField.setText("");
            instructorField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Course deletion failed!", "Error", JOptionPane.ERROR_MESSAGE);
        }

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Error deleting course: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
    
    
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(CourseManagement.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CourseManagement.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CourseManagement.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CourseManagement.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CourseManagement().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
