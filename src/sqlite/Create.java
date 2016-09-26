package sqlite;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Create {

	public static void main(String[] args) {
		try {
			Connection conn = DB.openConnection();
			Statement stmt = conn.createStatement();
			String sql = "CREATE TABLE COMPANY " +
			        "(ID INT PRIMARY KEY     NOT NULL," +
			        " NAME           TEXT    NOT NULL, " + 
			        " AGE            INT     NOT NULL, " + 
			        " ADDRESS        CHAR(50), " + 
			        " SALARY         REAL)"; 
		   stmt.executeUpdate(sql);
		   stmt.close();
		   DB.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
