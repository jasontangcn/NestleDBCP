package com.fairchild.dbcp.test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.fairchild.dbcp.DBCPFactory;
import com.fairchild.dbcp.DBConnectionPool;

public class DBConnectionPoolTest {
	public static void main(String[] args) {
		Connection conn = null;
		Statement statment = null;
		ResultSet resultSet = null;
		try {
			DBConnectionPool dbcp = DBCPFactory.getDBConnectionPool();
			conn = dbcp.getDBConnection();
			statment = conn.createStatement();
			resultSet = statment.executeQuery("SELECT * FROM Employees");
			while (resultSet.next()) {
				System.out.println("The employee LastName is " + resultSet.getString("LastName"));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != statment) {
					statment.close();
				}
			} catch (SQLException e) {
			}
			try {
				if (null != conn) {
					conn.close();
				}
			} catch (SQLException e) {
			}
		}
	}
}