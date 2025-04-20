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

public class ClassroomManagement extends javax.swing.JFrame {
    
    private JTextField roomField, capacityField;
    private JCheckBox avCheckbox, labCheckbox;

public ClassroomManagement() {
        setTitle("Classroom Management - Time Table Builder");
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
        homeBtn.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
               Dashboard dashFrame = new Dashboard();
               dashFrame.setPreferredSize(new Dimension(1000, 750));
               dashFrame.setVisible(true);
               dashFrame.pack();
               dashFrame.setLocationRelativeTo(null);
               dispose();
           } 
        });
        
        JButton viewBtn = new JButton("View Classrooms");
        viewBtn.setBackground(Color.WHITE);
        viewBtn.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
              ViewClassrooms viewFrame = new ViewClassrooms();
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
        
        // Classroom Management Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel("Classroom Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 102, 102));
        
        contentPanel.add(leftPanel, BorderLayout.WEST);
        
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        JLabel roomLabel = new JLabel("Room ID:");
         roomField = new JTextField();
        JLabel capacityLabel = new JLabel("Capacity:");
         capacityField = new JTextField();
        JLabel avLabel = new JLabel("Has AV:");
         avCheckbox = new JCheckBox();
        JLabel labLabel = new JLabel("Is Lab:");
         labCheckbox = new JCheckBox();
        
        formPanel.add(roomLabel);
        formPanel.add(roomField);
        formPanel.add(capacityLabel);
        formPanel.add(capacityField);
        formPanel.add(avLabel);
        formPanel.add(avCheckbox);
        formPanel.add(labLabel);
        formPanel.add(labCheckbox);
        
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Classroom");
        JButton deleteButton = new JButton("Delete Classroom");
        addButton.addActionListener(this::addClassroom);
        deleteButton.addActionListener(this::deleteClassroom);
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);

        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(navbar, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }

private void addClassroom(ActionEvent e) {
        String roomId = roomField.getText().trim();
        String capacityStr = capacityField.getText().trim();
        boolean hasAV = avCheckbox.isSelected();
        boolean isLab = labCheckbox.isSelected();

        if (roomId.isEmpty() || capacityStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
             Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection is not established.", "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Check if class already exists
        String checkQuery = "SELECT * FROM Classroom WHERE classId = '" + roomId + "'";
        Statement stmt1 = conn.createStatement();
        ResultSet rs = stmt1.executeQuery(checkQuery);
        
        if (rs.next()) {
            JOptionPane.showMessageDialog(this, "Class already exists.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

            
            int capacity = Integer.parseInt(capacityStr);
            String query = "INSERT INTO Classroom (classId, timetableId, isLab, capacity, hasAV) VALUES (?, NULL, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, roomId);
            stmt.setBoolean(2, isLab);
            stmt.setInt(3, capacity);
            stmt.setBoolean(4, hasAV);
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(this, "Classroom added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Capacity must be a number!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding classroom: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteClassroom(ActionEvent e) {
        String roomId = roomField.getText().trim();
        if (roomId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Room ID to delete", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
             Connection conn = DatabaseConnection.getInstance().getConnection();
             if (conn == null) {
             JOptionPane.showMessageDialog(this, "Database connection is not established.", "Database Error", JOptionPane.ERROR_MESSAGE);
             return;
             }
             
            // Check if class already exists
        String checkQuery = "SELECT * FROM Classroom WHERE classId = '" + roomId + "'";
        Statement stmt1 = conn.createStatement();
        ResultSet rs = stmt1.executeQuery(checkQuery);
        
        if (!rs.next()) {
            JOptionPane.showMessageDialog(this, "Classroom doesn't exists.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        } 
            
            String query = "DELETE FROM Classroom WHERE classId = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, roomId);
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(this, "Classroom deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Room ID not found!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting classroom: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
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
            java.util.logging.Logger.getLogger(ClassroomManagement.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ClassroomManagement.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ClassroomManagement.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ClassroomManagement.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ClassroomManagement().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
