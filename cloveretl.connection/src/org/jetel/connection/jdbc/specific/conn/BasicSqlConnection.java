/*
 * jETeL/CloverETL - Java based ETL application framework.
 * Copyright (c) Javlin, a.s. (info@cloveretl.com)
 *  
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jetel.connection.jdbc.specific.conn;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetel.database.sql.DBConnection;
import org.jetel.database.sql.JdbcSpecific;
import org.jetel.database.sql.JdbcSpecific.AutoGeneratedKeysType;
import org.jetel.database.sql.JdbcSpecific.OperationType;
import org.jetel.database.sql.SqlConnection;
import org.jetel.exception.JetelException;

/**
 * 
 * Default adapter for common java.sql.Connection class
 * It is directly used by the DefaultJdbcSpecific or as a ascendant of other Connection implementation.
 * 
 * @author Martin Zatopek (martin.zatopek@javlinconsulting.cz)
 *         (c) Javlin Consulting (www.javlinconsulting.cz)
 *
 * @created May 29, 2008
 */
public class BasicSqlConnection implements SqlConnection {

	protected final static Log logger = LogFactory.getLog(BasicSqlConnection.class);

	protected final static int DEFAULT_FETCH_SIZE = 50;

	protected Connection connection;
	
	protected OperationType operationType;
	
	private DBConnection dbConnection;
	
	public BasicSqlConnection(DBConnection dbConnection, Connection connection, OperationType operationType) throws JetelException {
		this.dbConnection = dbConnection;
		this.connection = connection;
		this.operationType = operationType;
		
		try {
			optimizeConnection(operationType);
		} catch (Exception e1) {
			logger.warn("Optimizing connection failed: " + e1.getMessage());
			logger.warn("Try to use another jdbc specific");
		}
		
		try {
			if (dbConnection.getHoldability() != null) {
				setHoldability(dbConnection.getHoldability());
			}
			if (dbConnection.getTransactionIsolation() != null) {
				setTransactionIsolation(dbConnection.getTransactionIsolation());
			}
		} catch (SQLException e) {
			throw new JetelException("Wrong connection configuration.", e);
		}
	}

	@Override
	public JdbcSpecific getJdbcSpecific() {
		return dbConnection.getJdbcSpecific();
	}
	
	@Override
	public List<String> getSchemas() throws SQLException {
		List<String> tmp;
		List<String> schemas = new ArrayList<String>();
		
		// add schemas
		tmp = getMetaSchemas();
		if (tmp != null) {
			schemas.addAll(tmp);
		}

		// add catalogs
		tmp = getMetaCatalogs();
		if (tmp != null) {
			schemas.addAll(tmp);
		}
		
		return schemas;
	}

	@Override
	public ResultSet getTables(String schema) throws SQLException {
		return getTablesAsCatalog(schema);
	}

	protected ResultSet getTablesAsCatalog(String catalog) throws SQLException {
		return connection.getMetaData().getTables(catalog, null, "%", new String[] {"TABLE", "VIEW" });
	}

	protected ResultSet getTablesAsSchema(String schema) throws SQLException {
		return connection.getMetaData().getTables(null, schema, "%", new String[] {"TABLE", "VIEW" });
	}

    @Override
	public ResultSetMetaData getColumns(String schema, String owner, String table) throws SQLException {
		String sqlQuery = getJdbcSpecific().compileSelectQuery4Table(schema, owner, table) + " where 0=1";
		ResultSet resultSet = connection.createStatement().executeQuery(sqlQuery);

		return resultSet.getMetaData();
    }
	
	@Override
	public Set<ResultSet> getColumns() throws SQLException {
		Set<ResultSet> resultSets = new HashSet<ResultSet>();
		try {
			resultSets.add(connection.getMetaData().getColumns(null, null, null, "%"));
		} catch (SQLException e) {
		}
		return resultSets;
	}

	/**
	 * A static method that retrieves schemas from dbMeta objects.
	 * Returns it as arraylist of strings in the format either <schema> or <catalog>.<schema>
	 * e.g.:
	 * mytable
	 * dbo.anothertable
	 * 
	 * @param dbMeta
	 * @return
	 * @throws SQLException
	 */
	protected List<String> getMetaSchemas() throws SQLException {
		DatabaseMetaData dbMeta = connection.getMetaData();
		List<String> ret = new ArrayList<String>();
		ResultSet result = dbMeta.getSchemas();
		String tmp;
		
		while (result.next()) {
			tmp = "";
			try {
				if (result.getString(2) != null) {
					tmp = result.getString(2) + dbMeta.getCatalogSeparator();
				}
			} catch (Exception e) {
				// -pnajvar
				// this is here deliberately
				// some dbms don't provide second column and that is not wrong, just have to ignore
			}
			tmp += result.getString(1);
			ret.add(tmp);
		}
		result.close();
		return ret;
	}
	
	protected List<String> getMetaCatalogs() throws SQLException {
		DatabaseMetaData dbMeta = connection.getMetaData();
		List<String> ret = new ArrayList<String>();
		ResultSet result = dbMeta.getCatalogs();
		String tmp;
		while (result.next()) {
			tmp = result.getString(1);
			if (tmp != null) {
				ret.add(tmp);
			}
		}
		result.close();
		return ret;
	}
	
	//*************** java.sql.Connection interface **************//
	/* (non-Javadoc)
	 * @see java.sql.Connection#clearWarnings()
	 */
	@Override
	public void clearWarnings() throws SQLException {
		connection.clearWarnings();
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#close()
	 */
	@Override
	public void close() throws SQLException {
		connection.close();
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#commit()
	 */
	@Override
	public void commit() throws SQLException {
		if (isTransactionsSupported()) {
			connection.commit();
		}
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#createStatement()
	 */
	@Override
	public Statement createStatement() throws SQLException {
		Statement statement;

		switch (operationType) {
		case READ:
			try {
				statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
			} catch (SQLException e) {
				logger.warn(e);
				statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			}catch (UnsupportedOperationException e) {
				logger.warn(e);
				statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			}
			break;
		default:
			statement = connection.createStatement();
		}
		
		return optimizeStatement(statement);
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#createStatement(int, int, int)
	 */
	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return optimizeStatement(connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability));
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#createStatement(int, int)
	 */
	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		return optimizeStatement(connection.createStatement(resultSetType, resultSetConcurrency));
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#getAutoCommit()
	 */
	@Override
	public boolean getAutoCommit() throws SQLException {
		return connection.getAutoCommit();
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#getCatalog()
	 */
	@Override
	public String getCatalog() throws SQLException {
		return connection.getCatalog();
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#getHoldability()
	 */
	@Override
	public int getHoldability() throws SQLException {
		return connection.getHoldability();
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#getMetaData()
	 */
	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return connection.getMetaData();
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#getTransactionIsolation()
	 */
	@Override
	public int getTransactionIsolation() throws SQLException {
		return connection.getTransactionIsolation();
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#getTypeMap()
	 */
	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return connection.getTypeMap();
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#getWarnings()
	 */
	@Override
	public SQLWarning getWarnings() throws SQLException {
		return connection.getWarnings();
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#isClosed()
	 */
	@Override
	public boolean isClosed() throws SQLException {
		return connection.isClosed();
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#isReadOnly()
	 */
	@Override
	public boolean isReadOnly() throws SQLException {
		return connection.isReadOnly();
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#nativeSQL(java.lang.String)
	 */
	@Override
	public String nativeSQL(String sql) throws SQLException {
		return connection.nativeSQL(sql);
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int, int)
	 */
	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return (CallableStatement) optimizeStatement(connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int)
	 */
	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return (CallableStatement) optimizeStatement(connection.prepareCall(sql, resultSetType, resultSetConcurrency));
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#prepareCall(java.lang.String)
	 */
	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		return (CallableStatement) optimizeStatement(connection.prepareCall(sql));
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int, int)
	 */
	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return (PreparedStatement) optimizeStatement(connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int)
	 */
	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return (PreparedStatement) optimizeStatement(connection.prepareStatement(sql, resultSetType, resultSetConcurrency));
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int)
	 */
	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		return (PreparedStatement) optimizeStatement(connection.prepareStatement(sql, autoGeneratedKeys));
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int[])
	 */
	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		PreparedStatement statement;
		if (getJdbcSpecific().getAutoKeyType() == AutoGeneratedKeysType.SINGLE) {
			if (columnIndexes != null) {
				statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			}else{
				logger.warn("Columns are null");
				logger.info("Getting generated keys switched off !");
				statement = connection.prepareStatement(sql);
			}
		}else{
			statement = connection.prepareStatement(sql, columnIndexes);
		}
		optimizeStatement(statement);
		return statement;
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#prepareStatement(java.lang.String, java.lang.String[])
	 */
	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		PreparedStatement statement;
		if (getJdbcSpecific().getAutoKeyType() == AutoGeneratedKeysType.SINGLE) {
			if (columnNames != null) {
				statement =  connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			}else{
				logger.warn("Columns are null");
				logger.info("Getting generated keys switched off !");
				statement =  connection.prepareStatement(sql);
			}
		}else{
			statement =  connection.prepareStatement(sql, columnNames);
		}
		optimizeStatement(statement);
		return statement;
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#prepareStatement(java.lang.String)
	 */
	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		PreparedStatement statement;
		switch (operationType) {
		case READ:
			try {
				statement = connection.prepareStatement(sql,ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
			} catch (SQLException e) {
				logger.warn(e);
				statement = connection.prepareStatement(sql,ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			}catch (UnsupportedOperationException e) {
				logger.warn(e);
				statement = connection.prepareStatement(sql,ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			}
			break;
		default:
			statement = connection.prepareStatement(sql);
		}
		optimizeStatement(statement);
		return statement;
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#releaseSavepoint(java.sql.Savepoint)
	 */
	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		connection.releaseSavepoint(savepoint);
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#rollback()
	 */
	@Override
	public void rollback() throws SQLException {
		if (isTransactionsSupported()) {
			connection.rollback();
		}
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#rollback(java.sql.Savepoint)
	 */
	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		if (isTransactionsSupported()) {
			connection.rollback(savepoint);
		}
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#setAutoCommit(boolean)
	 */
	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
// pnajvar-
// following check for transactions support causes problems and it seems to work fine without it
// even on non-transactions-enabled servers
//		if (isTransactionsSupported()) {
			connection.setAutoCommit(autoCommit);
//		}
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#setCatalog(java.lang.String)
	 */
	@Override
	public void setCatalog(String catalog) throws SQLException {
		connection.setCatalog(catalog);
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#setHoldability(int)
	 */
	@Override
	public void setHoldability(int holdability) throws SQLException {
		connection.setHoldability(holdability);
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#setReadOnly(boolean)
	 */
	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		connection.setReadOnly(readOnly);
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#setSavepoint()
	 */
	@Override
	public Savepoint setSavepoint() throws SQLException {
		return connection.setSavepoint();
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#setSavepoint(java.lang.String)
	 */
	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		return connection.setSavepoint(name);
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#setTransactionIsolation(int)
	 */
	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		connection.setTransactionIsolation(level);
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#setTypeMap(java.util.Map)
	 */
	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		connection.setTypeMap(map);
	}

	//*************** END of java.sql.Connection INTERFACE ******************//
	
	/**
	 * This method optimizes all java.sql.Statements returned by this interface (createStatement(?)).
	 * @param statement
	 * @return
	 * @throws SQLException
	 */
	protected Statement optimizeStatement(Statement statement) throws SQLException {
		switch (operationType) {
			case READ:
				try{
					statement.setFetchDirection(ResultSet.FETCH_FORWARD);
				}catch(SQLException ex){
					//TODO: for now, do nothing;
				}
				break;
		}
		
		return statement;
	}

	/**
	 * Optimizes inner java.sql.Connection according given operation type.
	 * @param operationType
	 */
	protected void optimizeConnection(OperationType operationType) throws Exception {
		switch (operationType) {
		case READ:
			connection.setAutoCommit(false);
			connection.setReadOnly(true);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			connection.setHoldability(ResultSet.CLOSE_CURSORS_AT_COMMIT);
			break;
		case WRITE:
		case CALL:
			connection.setAutoCommit(false);
			connection.setReadOnly(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			connection.setHoldability(ResultSet.CLOSE_CURSORS_AT_COMMIT);
			break;

		case TRANSACTION:
			connection.setAutoCommit(true);
			connection.setReadOnly(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			connection.setHoldability(ResultSet.CLOSE_CURSORS_AT_COMMIT);
			break;
		}
	}

	@Override
	public boolean isTransactionsSupported() {
		return isTransactionsSupported(this.connection);
	}
	
	public static boolean isTransactionsSupported(Connection connection) {
		try {
			return connection.getTransactionIsolation() != Connection.TRANSACTION_NONE;
		} catch (SQLException e) {
			return false;
		}
	}

	/* JDBC 4 methods */

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
	    return iface.isAssignableFrom(getClass()) || connection.isWrapperFor(iface);
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
	    if (iface.isAssignableFrom(getClass())) {
	        return iface.cast(this);
	    } else if (iface.isAssignableFrom(connection.getClass())) {
	        return iface.cast(connection);
	    } else {
	        return connection.unwrap(iface);
	    }
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements)
			throws SQLException {
		return connection.createArrayOf(typeName, elements);
	}

	@Override
	public Blob createBlob() throws SQLException {
		return connection.createBlob();
	}

	@Override
	public Clob createClob() throws SQLException {
		return connection.createClob();
	}

	@Override
	public NClob createNClob() throws SQLException {
		return connection.createNClob();
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		return connection.createSQLXML();
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes)
			throws SQLException {
		return connection.createStruct(typeName, attributes);
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		return connection.getClientInfo();
	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		return connection.getClientInfo(name);
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		return connection.isValid(timeout);
	}

	@Override
	public void setClientInfo(Properties properties)
			throws SQLClientInfoException {
		connection.setClientInfo(properties);
	}

	@Override
	public void setClientInfo(String name, String value)
			throws SQLClientInfoException {
		connection.setClientInfo(name, value);
	}

	/* JDBC 4.1 methods */

	@Override
	public void setSchema(String schema) throws SQLException {
		connection.setSchema(schema);
	}

	@Override
	public String getSchema() throws SQLException {
		return connection.getSchema();
	}

	@Override
	public void abort(Executor executor) throws SQLException {
		connection.abort(executor);
	}

	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		connection.setNetworkTimeout(executor, milliseconds);
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		return connection.getNetworkTimeout();
	}

}
