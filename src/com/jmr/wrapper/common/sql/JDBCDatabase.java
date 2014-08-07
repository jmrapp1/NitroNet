package com.jmr.wrapper.common.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.jmr.wrapper.common.exceptions.NEDatabaseCantConnect;
import com.jmr.wrapper.common.exceptions.NEDatabaseQueryError;

/**
 * Networking Library
 * JDBCDatabase.java
 * Purpose: Connects to a database through the JBDC driver and allows your to perform 
 * queries.
 *
 * @author Jon R (Baseball435)
 * @version 1.0 7/19/2014
 */

public class JDBCDatabase implements IDatabase {

	/** The Connection to the database. */
	private Connection dbConnection;
	
	/** Connects to a database.
	 * @param url The link to the database. 
	 * @param databaseName The database name.
	 * @param username The username to connect to the database.
	 * @param password The password to connect to the database.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException Thrown when JBDC driver not found.
	 * @throws NEDatabaseCantConnect  Thrown when can't connect to the database.
	 * @throws SQLException
	 */
	public JDBCDatabase(String url, String databaseName, String username, String password) throws InstantiationException, IllegalAccessException, ClassNotFoundException, NEDatabaseCantConnect {
		Class.forName("com.mysql.jdbc.Driver").newInstance(); 
		try {
			dbConnection = DriverManager.getConnection(url + databaseName, username, password);
		} catch (SQLException e) {
			throw new NEDatabaseCantConnect();
		}
	}
	
	@Override
	public ResultSet executeQuery(String query) throws NEDatabaseQueryError {
		Statement st = getNewStatement();
		ResultSet ret = executeQuery(st, query);
		try {
			st.close();
		} catch (SQLException e) {
			throw new NEDatabaseQueryError(e.getMessage());
		}
		return ret;
	}
	
	@Override
	public ResultSet executeQuery(Statement statement, String query) throws NEDatabaseQueryError {
		try {
			return statement.executeQuery(query);
		} catch (SQLException e) {
			throw new NEDatabaseQueryError(e.getMessage());
		}
	}
	
	@Override
	public int executeUpdate(String query) throws NEDatabaseQueryError {
		Statement st = getNewStatement();
		int ret = executeUpdate(st, query);
		try {
			st.close();
		} catch (SQLException e) {
			throw new NEDatabaseQueryError(e.getMessage());
		}
		return ret;
	}
	
	@Override
	public int executeUpdate(Statement st, String query) throws NEDatabaseQueryError {
		try {
			return st.executeUpdate(query);
		} catch (SQLException e) {
			throw new NEDatabaseQueryError(e.getMessage());
		}
	}
	
	@Override
	public int executeUpdate(PreparedStatement st) throws NEDatabaseQueryError {
		try {
			return st.executeUpdate();
		} catch (SQLException e) {
			throw new NEDatabaseQueryError(e.getMessage());
		}
	}
	
	@Override
	public ResultSet executeQuery(PreparedStatement st) throws NEDatabaseQueryError {
		try {
			return st.executeQuery();
		} catch (SQLException e) {
			throw new NEDatabaseQueryError(e.getMessage());
		}
	}
	
	@Override
	public PreparedStatement getPreparedStatement(String query) throws NEDatabaseQueryError {
		try {
			return dbConnection.prepareStatement(query);
		} catch (SQLException e) {
			throw new NEDatabaseQueryError(e.getMessage());
		}
	}
	
	@Override
	public int getRowCount(ResultSet rs) throws NEDatabaseQueryError {
		int rows = 0;
		try {
			while (rs.next()) {
				rows++;
			}
		} catch (SQLException e) {
			throw new NEDatabaseQueryError(e.getMessage());
		}
		return rows;
	}
		
	@Override
	public Statement getNewStatement() throws NEDatabaseQueryError {
		try {
			return dbConnection.createStatement();
		} catch (SQLException e) {
			throw new NEDatabaseQueryError(e.getMessage());
		}
	}
	
	@Override
	public void closeConnection() {
		try {
			dbConnection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Connection getDatabaseConnection() {
		return dbConnection;
	}

}
