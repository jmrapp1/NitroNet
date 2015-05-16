package com.jmr.wrapper.common.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import com.jmr.wrapper.common.exceptions.NNDatabaseQueryError;

/**
 * Networking Library
 * IDatabase.java
 * Purpose: Framework for a database driver to implement. Used in the already implemented
 * JDBCDatabase class which uses the JDBC driver.
 *
 * @author Jon R (Baseball435)
 * @version 1.0 7/19/2014
 */

public interface IDatabase {

	/** Executes a query.
	 * @param query The query to execute.
	 * @return The ResultSet that holds the returned information.
	 * @throws NNDatabaseQueryError Thrown when the query can't be ran.
	 */
	public ResultSet executeQuery(String query) throws NNDatabaseQueryError;
	
	/** Executes a query.
	 * @param statement An already instantiated statement to perform the query on.
	 * @param query The query to execute.
	 * @return The ResultSet that holds the returned information.
	 * @throws NNDatabaseQueryError Thrown when the query can't be ran.
	 */
	public ResultSet executeQuery(Statement statement, String query) throws NNDatabaseQueryError;
	
	/** Executes a query which can only be an INSERT, UPDATE, or DELETE query.
	 * @param query The query to execute.
	 * @return The count of the affected rows. 0 is nothing was effected.
	 * @throws NNDatabaseQueryError Thrown when the query can't be ran.
	 */
	public int executeUpdate(String query) throws NNDatabaseQueryError;
	
	/** Executes a query which can only be an INSERT, UPDATE, or DELETE query.
	 * @param statement An already instantiated statement to perform the query on.
	 * @param query The query to execute.
	 * @return The count of the affected rows. 0 is nothing was effected.
	 * @throws NNDatabaseQueryError Thrown when the query can't be ran.
	 */
	public int executeUpdate(Statement st, String query) throws NNDatabaseQueryError;
	
	/** Executes a query which can only be an INSERT, UPDATE, or DELETE query.
	 * @param statement An already instantiated prepared statement to execute.
	 * @return The count of the affected rows. 0 is nothing was effected.
	 * @throws NNDatabaseQueryError Thrown when the query can't be ran.
	 */
	public int executeUpdate(PreparedStatement st) throws NNDatabaseQueryError;
	
	/** Executes a query from a prepared statement.
	 * @param statement An already instantiated prepared statement to execute.
	 * @return The ResultSet that holds the returned database information
	 * @throws NNDatabaseQueryError Thrown when the query can't be ran.
	 */
	public ResultSet executeQuery(PreparedStatement st) throws NNDatabaseQueryError;
	
	/** Creates a prepared statement from a query. Used to stop SQL injection.
	 * @param query The query to perform.
	 * @return The prepared statement.
	 * @throws NNDatabaseQueryError
	 */
	public PreparedStatement getPreparedStatement(String query) throws NNDatabaseQueryError;
	
	/** Returns the amount of rows of a ResultSet.
	 * @param rs The ResultSet object to check.
	 * @return The amount of rows.
	 * @throws NNDatabaseQueryError Thrown when query can't be run.
	 */
	public int getRowCount(ResultSet rs) throws NNDatabaseQueryError;
		
	/** @return A new Statement object. 
	 * @throws NNDatabaseQueryError Thrown when the query can't be ran.
	 */
	public Statement getNewStatement() throws NNDatabaseQueryError;
	/** Closes the connection to the database. */
	public void closeConnection();
	
	/** @return The connection to the database. */
	public Connection getDatabaseConnection();
	
}
