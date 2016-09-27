package sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cn.cnic.peer.entity.Piece;

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
	
	public void insertPiece(Piece p) {
		try {
			Connection conn = DB.openConnection();
			String sql = "INSERT INTO ts (contentHash,offset,length) VALUES (?,?,?);"; 
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, p.getContentHash());
			stmt.setInt(2, p.getOffset());
			stmt.setInt(3, p.getLength());
			stmt.executeUpdate();
			stmt.close();
			DB.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public List<Piece> getPiecesByContentHash(String contentHash) {
		List<Piece> pieces = new ArrayList<Piece>();
		try {
			Connection conn = DB.openConnection();
			String sql = "SELECT * FROM ts where contentHash = ?;";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, contentHash);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Piece p = new Piece();
				p.setContentHash(rs.getString("contentHash"));
				p.setOffset(rs.getInt("offset"));
				p.setLength(rs.getInt("length"));
				pieces.add(p);
			}
			rs.close();
			stmt.close();
			DB.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return pieces;
	}
}