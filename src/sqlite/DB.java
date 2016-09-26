package sqlite;

import java.sql.Connection;
import java.sql.DriverManager;

public class DB {
	private static Connection conn = null;

	public static Connection openConnection() {
		try {
			if (null == conn || conn.isClosed()) {
				Class.forName("org.sqlite.JDBC");
				conn = DriverManager.getConnection("jdbc:sqlite:peer.db");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}

	public static void closeConnection() {
		try {
			if (null != conn) {
				conn.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn = null;
			System.gc();
		}
	}
}