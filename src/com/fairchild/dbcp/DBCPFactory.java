/*
 * Created on 2004-10-27
 *
 */
package com.fairchild.dbcp;

/**
 * @author TomHornson@hotmail.com
 *
 */
public class DBCPFactory {
	public static final int INITIAL_CAPACITY = 40;
	public static final int MAX_CAPACITY = 240;
	public static final long WAITING_TIME = 1000; // in MS
	public static final int RESIZE_CAPACITY = 40;
	public static final String JDBC_DRIVER_NAME = "";
	public static final String DB_URL = "";
	public static final String DB_USERNAME = "";
	public static final String DB_PASSWORD = "";

	public static DBConnectionPool dbcp = null;

	private DBCPFactory() {

	}

	public static final DBConnectionPool getDBConnectionPool() {
		if (dbcp == null) {
			try {
				dbcp = new DBConnectionPool(INITIAL_CAPACITY, MAX_CAPACITY, WAITING_TIME, RESIZE_CAPACITY, JDBC_DRIVER_NAME, DB_URL, DB_USERNAME, DB_PASSWORD);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return dbcp;
		} else {
			return dbcp;
		}
	}

}
