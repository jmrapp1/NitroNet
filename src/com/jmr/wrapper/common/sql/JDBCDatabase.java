package com.jmr.wrapper.common.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.jmr.wrapper.common.exceptions.NNDatabaseCantConnect;
import com.jmr.wrapper.common.exceptions.NNDatabaseQueryError;

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
	 * @throws NNDatabaseCantConnect  Thrown when can't connect to the database.
	 * @throws SQLException
	 */
	public JDBCDatabase(String url, String databaseName, String username, String password) throws InstantiationException, IllegalAccessException, ClassNotFoundException, NNDatabaseCantConnect {
		Class.forName("com.mysql.jdbc.Driver").newInstance(); 
		try {
			dbConnection = DriverManager.getConnection(url + databaseName, username, password);
		} catch (SQLException e) {
			throw new NNDatabaseCantConnect();
		}
	}
	
	@Override
	public ResultSet executeQuery(String query) throws NNDatabaseQueryError {
		Statement st = getNewStatement();
		ResultSet ret = executeQuery(st, query);
		try {
			st.close();
		} catch (SQLException e) {
			throw new NNDatabaseQueryError(e.getMessage());
		}
		return ret;
	}
	
	@Override
	public ResultSet executeQuery(Statement statement, String query) throws NNDatabaseQueryError {
		try {
			return statement.executeQuery(query);
		} catch (SQLException e) {
			throw new NNDatabaseQueryError(e.getMessage());
		}
	}
	
	@Override
	public int executeUpdate(String query) throws NNDatabaseQueryError {
		Statement st = getNewStatement();
		int ret = executeUpdate(st, query);
		try {
			st.close();
		} catch (SQLException e) {
			throw new NNDatabaseQueryError(e.getMessage());
		}
		return ret;
	}
	
	@Override
	public int executeUpdate(Statement st, String query) throws NNDatabaseQueryError {
		try {
			return st.executeUpdate(query);
		} catch (SQLException e) {
			throw new NNDatabaseQueryError(e.getMessage());
		}
	}
	
	@Override
	public int executeUpdate(PreparedStatement st) throws NNDatabaseQueryError {
		try {
			return st.executeUpdate();
		} catch (SQLException e) {
			throw new NNDatabaseQueryError(e.getMessage());
		}
	}
	
	@Override
	public ResultSet executeQuery(PreparedStatement st) throws NNDatabaseQueryError {
		try {
			return st.executeQuery();
		} catch (SQLException e) {
			throw new NNDatabaseQueryError(e.getMessage());
		}
	}
	
	@Override
	public PreparedStatement getPreparedStatement(String query) throws NNDatabaseQueryError {
		try {
			return dbConnection.prepareStatement(query);
		} catch (SQLException e) {
			throw new NNDatabaseQueryError(e.getMessage());
		}
	}
	
	@Override
	public int getRowCount(ResultSet rs) throws NNDatabaseQueryError {
		int rows = 0;
		try {
			while (rs.next()) {
				rows++;
			}
		} catch (SQLException e) {
			throw new NNDatabaseQueryError(e.getMessage());
		}
		return rows;
	}
		
	@Override
	public Statement getNewStatement() throws NNDatabaseQueryError {
		try {
			return dbConnection.createStatement();
		} catch (SQLException e) {
			throw new NNDatabaseQueryError(e.getMessage());
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
