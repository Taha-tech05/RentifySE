package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHandler {

	private static DatabaseHandler instance;
	private Connection connection;

	// Simple SQL Authentication — NO IntegratedSecurity, NO DLL, NO problems
	private static final String DB_URL = "jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=Rentify;encrypt=false;trustServerCertificate=true;";

	private static final String USER = "sa";
	private static final String PASS = "123456"; // same password you used above

	private DatabaseHandler() {
		connect();
	}

	public static DatabaseHandler getInstance() {
		if (instance == null) {
			instance = new DatabaseHandler();
		}
		return instance;
	}

	public Connection connect() {
		try {
			if (connection == null || connection.isClosed()) {
				connection = DriverManager.getConnection(DB_URL, USER, PASS);
				System.out.println("Connected to Rentify successfully with javauser!");
			}
		} catch (SQLException e) {
			System.err.println("Connection failed!");
			e.printStackTrace();
		}
		return connection;
	}

	public void disconnect() {
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
				System.out.println("Disconnected!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Connection getConnection() {
		return connection;
	}
}