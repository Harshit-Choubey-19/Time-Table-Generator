package loginandsignup;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.*;

public class InstructorManagement extends javax.swing.JFrame {
    
  private JTextField idField, nameField;
  private JComboBox<String> deptCombo;
  public InstructorManagement() {
        setTitle("Instructor Management - Time Table Builder");
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
            dispose();
        });
        
        JButton viewBtn = new JButton("View Instructors");
        viewBtn.setBackground(Color.WHITE);
        viewBtn.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
              ViewInstructors viewFrame = new ViewInstructors();
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
        
        // Instructor Management Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        
        contentPanel.add(leftPanel, BorderLayout.WEST);
        
        JLabel titleLabel = new JLabel("Instructor Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 102, 102));
        
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        JLabel idLabel = new JLabel("Instructor ID:");
        idField = new JTextField();
        JLabel nameLabel = new JLabel("Instructor Name:");
        nameField = new JTextField();
        JLabel deptLabel = new JLabel("Department:");
        String[] departments = {"CS", "ECE", "EEE", "Mechanical", "Civil", "Chemical"};
        deptCombo = new JComboBox<>(departments);
        
        formPanel.add(idLabel);
        formPanel.add(idField);
        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(deptLabel);
        formPanel.add(deptCombo);
        
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Instructor");
        addButton.addActionListener(this::addInstructor);
        JButton deleteButton = new JButton("Delete Instructor");
        deleteButton.addActionListener(this::deleteInstructor);
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
    
    private void addInstructor(ActionEvent e) {
    String id = idField.getText().trim();
    String name = nameField.getText().trim();
    String department = (String) deptCombo.getSelectedItem();

    if (id.isEmpty() || name.isEmpty() || department.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter all fields", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection is not established.", "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if instructor already exists
        String checkQuery = "SELECT * FROM Instructor WHERE instructId = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
        checkStmt.setString(1, id);
        ResultSet rs = checkStmt.executeQuery();

        if (rs.next()) {
            JOptionPane.showMessageDialog(this, "Instructor already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String query = "INSERT INTO Instructor (instructId, name, department) VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, id);
        stmt.setString(2, name);
        stmt.setString(3, department);
        int rowsInserted = stmt.executeUpdate();

        if (rowsInserted > 0) {
            JOptionPane.showMessageDialog(this, "Instructor added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            idField.setText("");
            nameField.setText("");
            deptCombo.setSelectedIndex(0);
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Error adding instructor: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
    
    private void deleteInstructor(ActionEvent e) {
    String id = idField.getText().trim();

    if (id.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter Instructor ID to delete", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection is not established.", "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if instructor exists
        String checkQuery = "SELECT * FROM Instructor WHERE instructId = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
        checkStmt.setString(1, id);
        ResultSet rs = checkStmt.executeQuery();

        if (!rs.next()) {
            JOptionPane.showMessageDialog(this, "Instructor not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Perform delete
        String deleteQuery = "DELETE FROM Instructor WHERE instructId = ?";
        PreparedStatement stmt = conn.prepareStatement(deleteQuery);
        stmt.setString(1, id);
        int rowsDeleted = stmt.executeUpdate();

        if (rowsDeleted > 0) {
            JOptionPane.showMessageDialog(this, "Instructor deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            idField.setText("");
            nameField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Instructor deletion failed!", "Error", JOptionPane.ERROR_MESSAGE);
        }

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Error deleting instructor: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

    

   
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new InstructorManagement().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
