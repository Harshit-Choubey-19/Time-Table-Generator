
package loginandsignup;
import java.awt.Dimension;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class OopsProj {


    public static void main(String[] args) {

        // Initialize Database Connection
        Connection conn = DatabaseConnection.getInstance().getConnection();
        
        Login LoginFrame = new Login();
        LoginFrame.setVisible(true);
        LoginFrame.pack();
        LoginFrame.setLocationRelativeTo(null);       


    }
    
}
