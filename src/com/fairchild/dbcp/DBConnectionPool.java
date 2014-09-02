package com.fairchild.dbcp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

/*
 * 实现的功能：
 * 总体来说就是实现对Pool中DBConnection的管理，而DBConnection是对java.sql.Connection的封装�?
 * 1、Pool初始化�?
 *    DBConnectionPool(int initialCapacity, int maxCapacity, long waitingTime, String driverName, String username, String password)
 *    根据driverName, username, password可以获得Connetion�?
 *    根据initialCapacity初始化一个ConnectionPool�?
 * 2、提供释放Connection的能力�?
 * 3、提供获得Connetion的能力�?

 */
public class DBConnectionPool extends Object {
	private ArrayList dbConnections;

	private int initialCapacity;
	private int maxCapacity;
	private long waitingTime;
	private int resize;
	private String driverName;
	private String dbURL;
	private String username;
	private String password;

	public DBConnectionPool(int initialCapacity, int maxCapacity, long waitingTime, int resize, String driverName, String dbURL, String username, String password) throws Exception {
		// 处理逻辑�?
		// 1、initialCapacity必须大于等于1
		// 2、maxCapacity必须大于等于1
		// 3、initialCapacity必须小于等于maxCapacity
		// 4、关于driverName和dbURL的处理�?辑�?
		// 这里我限定：driverName和dbURL都不能为NULL�?
		// 我觉得更好的办法：设置一个默认driverName和dbURL，当传�?值为NULL时，使用默认值�?
		// 5、username和password允许都为空�?
		//
		initialCapacity = Math.max(1, initialCapacity);
		maxCapacity = Math.max(1, maxCapacity);
		waitingTime = Math.max(5000, waitingTime);
		resize = Math.max(1, resize);

		if ((initialCapacity > maxCapacity) || (null == driverName) || (null == dbURL))
			throw new Exception("Failed to initialize pool for illegal arguments.");

		this.driverName = driverName;
		this.dbURL = dbURL;
		this.username = username;
		this.password = password;
		initializeDBPool();
	}

	/*
	 * 理论上应该加上synchronized，以防止其它线程竞争�?不过由于修饰符为private，无法调用，�?��可以省略synchronized�?
	 */
	private Connection createDBConnection() {
		try {
			return DriverManager.getConnection(dbURL, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	private synchronized void initializeDBPool() {
		try {
			Class.forName(driverName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (int i = 0; i < initialCapacity; i++) {
			Connection connection = createDBConnection();
			// 这里事实上，我们�?��考虑Connection获取失败的可能，在这样的情况下，连接池的size可能小于initialCapacity，甚至为0�?
			//
			if (null != connection) {
				DBConnection connectionWrap = new DBConnection(connection, this);
				addDBConnection(connectionWrap);
			}
		}
	}

	// 同理，private决定不需要synchronized
	private void addDBConnection(DBConnection connection) {
		if (null == dbConnections)
			dbConnections = new ArrayList(this.initialCapacity);
		dbConnections.add(connection);
	}

	synchronized void releaseDBConnetion(Connection conn) {
		for (int i = 0; i < dbConnections.size(); i++) {
			DBConnection dbConnection = (DBConnection) dbConnections.get(i);
			// 是是否引用向同一个对象�?
			if (dbConnection.getJDBCConnection() == conn)
				dbConnection.setUsing(false);
			break;
		}
	}

	public synchronized Connection getDBConnection() {
		/*
		 * 功能描述�?1、遍历Pool，寻找空闲并且使用次数没有达到最大�?的DBConnection�?
		 * 2、如果使用次数达到最大�?，就删除该DBConnection�?
		 * 3、如果没有达到maxCapacity，那么就向Pool中添加DBConnection�?
		 * 4、如果没有Pool已经满了，并且没有空闲的DBConnection，那么就等待直到有可用的DBConnection�?
		 */
		for (int i = 0; i < dbConnections.size(); i++) {
			// 这一句感觉是多余的，因为在上面，我限制了NULL进入Pool�?
			DBConnection dbConnection = (DBConnection) dbConnections.get(i);
			if (null == dbConnection)
				continue;

			if ((!dbConnection.isUsing()) && (!dbConnection.overUsed())) {
				dbConnection.setUsing(true);
				dbConnection.increaseUsed();
				return dbConnection.getConnectionProxy();
			}

			if (dbConnection.overUsed()) {
				dbConnection.close();
				dbConnections.remove(i);
				Connection conn = createDBConnection();
				if (null != conn) {
					dbConnection = new DBConnection(conn, this);
					dbConnection.setUsing(true);
					dbConnections.add(i, dbConnection);
				}
				return dbConnection.getConnectionProxy();
			}
		}

		if (dbConnections.size() < maxCapacity) {
			int remainingCapacity = ((maxCapacity - dbConnections.size()) > resize) ? resize : (maxCapacity - dbConnections.size());

			for (int i = 0; i < remainingCapacity; i++) {
				Connection conn = createDBConnection();
				if (null != conn) {
					dbConnections.add(new DBConnection(conn, this));
				}
			}
		} else {
			try {
				Thread.sleep(waitingTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return getDBConnection();
	}

	public void shutdown() {
		for (int i = 0; i < dbConnections.size(); i++) {
			DBConnection dbConnection = (DBConnection) dbConnections.get(i);
			dbConnection.close();
			dbConnections.remove(i);
		}
	}
	//
	// 上面的方法中，我们需要注意一个问题：
	// getDBConnection是一个synchronized方法�?
	// 自然，在执行过程中，调用getDBConnection的线程获得对象级互斥锁�?
	// 那么这时，我们是否可以正常使用从它的成员变量connections中获得的Connection(对象引用)�?
	// 答案是肯定的，因为我们不�?��操作DBConnectionPool对象�?
	//
}