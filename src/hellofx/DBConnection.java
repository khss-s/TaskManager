package hellofx;

import java.sql.*;

import javax.swing.JOptionPane;

public class DBConnection {
    public Connection connectToDB() {
        Connection con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/taskmaster_db", "root", "");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
        return con;
    }
}
