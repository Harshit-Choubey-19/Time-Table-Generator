
package loginandsignup;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.*;


public class Dashboard extends javax.swing.JFrame {

  public Dashboard() {
        setTitle("Dashboard - Time Table Builder");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Navbar Panel
        JPanel navbar = new JPanel(new BorderLayout());
        navbar.setBackground(new Color(0, 102, 102));
        navbar.setPreferredSize(new Dimension(800, 50));
        
        JButton homeBtn = new JButton("Home");
        homeBtn.setBackground(Color.WHITE);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        
         // Display logged-in user name
        UserSession user = UserSession.getInstance();
        JLabel userLabel;
        if(user != null){
            userLabel = new JLabel("Welcome, " + user.getFullName() + " ");
        }else{
           userLabel = new JLabel("Welcome User"); 
        }
        userLabel.setForeground(Color.WHITE);
        
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
       
        rightPanel.add(userLabel);
        rightPanel.add(settingsBtn);
        
        navbar.add(homeBtn, BorderLayout.WEST);
        navbar.add(rightPanel, BorderLayout.EAST);
        
        // Main Panel Content
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 3, 10, 10));
        panel.setBackground(Color.WHITE);
        
        JButton classroomBtn = new JButton("Classroom Management");
        JButton instructorBtn = new JButton("Instructor Management");
        JButton courseBtn = new JButton("Course Management");
        JButton timetableBtn = new JButton("Time Table Generator");
        JButton conflictBtn = new JButton("Conflict Resolution");
        JButton reportBtn = new JButton("Reports & Export");
    
        panel.add(classroomBtn);
        panel.add(instructorBtn);
        panel.add(courseBtn);                
        panel.add(timetableBtn);
        panel.add(conflictBtn);
        panel.add(reportBtn);

        instructorBtn.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
               UserSession user = UserSession.getInstance();
               String role = user.getRole(); 
               if("admin".equalsIgnoreCase(role)){
               InstructorManagement insFrame = new InstructorManagement();
               insFrame.setPreferredSize(new Dimension(900, 400));
               insFrame.setVisible(true);
               insFrame.pack();
               insFrame.setLocationRelativeTo(null);
               dispose();               
               }else{
               JOptionPane.showMessageDialog(null, "You are not authorize!", "Error", JOptionPane.ERROR_MESSAGE);
               }

           } 
        });
        
        classroomBtn.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
               UserSession user = UserSession.getInstance();
               String role = user.getRole(); 
               if("admin".equalsIgnoreCase(role)){
               ClassroomManagement classFrame = new ClassroomManagement();
               classFrame.setPreferredSize(new Dimension(900, 400));
               classFrame.setVisible(true);
               classFrame.pack();
               classFrame.setLocationRelativeTo(null);
               dispose();               
               }else{
               JOptionPane.showMessageDialog(null, "You are not authorize!", "Error", JOptionPane.ERROR_MESSAGE);
               }               
           } 
        });
        
         courseBtn.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
               UserSession user = UserSession.getInstance();
               String role = user.getRole(); 
               if("admin".equalsIgnoreCase(role)){
               CourseManagement courseFrame = new CourseManagement();
               courseFrame.setPreferredSize(new Dimension(900, 400));
               courseFrame.setVisible(true);
               courseFrame.pack();
               courseFrame.setLocationRelativeTo(null);
               dispose();               
               }else{
               JOptionPane.showMessageDialog(null, "You are not authorize!", "Error", JOptionPane.ERROR_MESSAGE);
               }

           } 
        });
         
        timetableBtn.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
               TimeTableGenerator timeTableFrame = new TimeTableGenerator();
               timeTableFrame.setPreferredSize(new Dimension(1000, 700));
               timeTableFrame.setVisible(true);
               timeTableFrame.pack();
               timeTableFrame.setLocationRelativeTo(null);
               dispose();
           } 
        });
          
        conflictBtn.addActionListener(new ActionListener() {
    public void actionPerformed(ActionEvent e) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM Conflict WHERE userId = ?"
            );
            ps.setString(1, UserSession.getInstance().getUserId());
            ResultSet rs = ps.executeQuery();
            
            if(rs.next()){
            ConflictResolution conFrame = new ConflictResolution();
            conFrame.setPreferredSize(new Dimension(1000, 700));
            conFrame.pack();
            conFrame.setLocationRelativeTo(null);
            conFrame.setVisible(true);
            dispose();
            }else{
            JOptionPane.showMessageDialog(null, "No Conflicts to resolve!", "Info", JOptionPane.INFORMATION_MESSAGE);
            } 
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error loading timetable courses: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
});

        
        reportBtn.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
               Reports reportFrame = new Reports();
               reportFrame.setPreferredSize(new Dimension(1000, 700));
               reportFrame.setVisible(true);
               reportFrame.pack();
               reportFrame.setLocationRelativeTo(null);
               dispose();
           } 
        });  
        
        mainPanel.add(navbar, BorderLayout.NORTH);
        mainPanel.add(panel, BorderLayout.CENTER);
        
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

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Dashboard().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
