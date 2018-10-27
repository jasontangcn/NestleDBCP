package com.fairchild.dbcp;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.lang.reflect.*;

class DBConnection implements InvocationHandler, Serializable {
	private final static String METHOD_NAME_CLOSE = "close";

	private int used = 0;
	private boolean isUsing = false;
	private int maxUseAllowed = 10;
	public Connection jdbcConnection = null;
	private DBConnectionPool dbcp = null;
	private long lastAccessTime = 0;

	public DBConnection() {
	}

	public DBConnection(Connection jdbcConnection, DBConnectionPool dbcp) {
		if (null != jdbcConnection)
			this.jdbcConnection = jdbcConnection;
		this.dbcp = dbcp;
		this.lastAccessTime = System.currentTimeMillis();
	}

	public void setJDBCConnection(Connection connection) {
		this.jdbcConnection = connection;
	}

	public Connection getJDBCConnection() {
		return this.jdbcConnection;
	}

	public Connection getConnectionProxy() {
		Class[] interfaces = { java.sql.Connection.class };
		Connection connProxy = (Connection) Proxy.newProxyInstance(jdbcConnection.getClass().getClassLoader(), interfaces, this);
		return connProxy;
	}

	public void setUsing(boolean isUsing) {
		this.isUsing = isUsing;
	}

	public boolean isUsing() {
		return this.isUsing;
	}

	public long getLastAccessTime() {
		return this.lastAccessTime;
	}

	public void increaseUsed() {
		used++;
	}

	private int getUsed() {
		return this.used;
	}

	public boolean overUsed() {
		return getUsed() > maxUseAllowed;
	}

	public void close() {
		try {
			if ((null != jdbcConnection) && (!jdbcConnection.isClosed())) {
				jdbcConnection.close();
			}
			jdbcConnection = null;
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object obj = null;
		if (METHOD_NAME_CLOSE.equals(method.getName())) {
			dbcp.releaseDBConnetion(this.jdbcConnection);
			this.lastAccessTime = System.currentTimeMillis();
		} else {
			obj = method.invoke(jdbcConnection, args);
		}
		return obj;
	}
}