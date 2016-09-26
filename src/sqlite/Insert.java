package sqlite;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Insert {
	public static void main(String[] args) {
		try {
			Connection conn = DB.openConnection();
			Statement stmt = conn.createStatement();
			String sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) " +
	                   "VALUES (2, 'Paul', 32, 'California', 20000.00 );"; 
			stmt.executeUpdate(sql);
			stmt.close();
			DB.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
