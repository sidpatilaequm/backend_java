import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDB {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/multimedia_governance", "root", "tiger");
            Statement stmt = conn.createStatement();
            
            System.out.println("--- SUPER ADMINS ---");
            ResultSet rs = stmt.executeQuery("SELECT super_admin_id, email FROM super_admin");
            while (rs.next()) {
                System.out.println(rs.getString(1) + " | " + rs.getString(2));
            }
            
            System.out.println("--- USER DETAILS ---");
            rs = stmt.executeQuery("SELECT user_id, email, super_admin_id FROM user_details");
            while (rs.next()) {
                System.out.println(rs.getString(1) + " | " + rs.getString(2) + " | " + rs.getString(3));
            }
            
            conn.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
