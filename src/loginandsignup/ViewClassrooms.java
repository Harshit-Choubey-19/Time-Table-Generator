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

public class ViewClassrooms extends javax.swing.JFrame {


    public ViewClassrooms() {
      setTitle("View Classrooms");
        setSize(600, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        String[] columnNames = {"Classroom ID", "Capacity", "Has AV", "Is Lab"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            if (conn != null) {
                String query = "SELECT * FROM Classroom";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                while (rs.next()) {
                    String id = rs.getString("classId");
                    int capacity = rs.getInt("capacity");
                    boolean hasAV = rs.getBoolean("hasAV");
                    boolean isLab = rs.getBoolean("isLab");

                    Object[] row = {id, capacity, hasAV, isLab};
                    model.addRow(row);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Database connection not established.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error fetching classrooms: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
                new ViewClassrooms().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
