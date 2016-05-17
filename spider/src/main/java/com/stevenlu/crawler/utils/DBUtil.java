package com.stevenlu.crawler.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 数据库工具类
 * @author Administrator
 */
public class DBUtil {
	
	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	private static final String DB_URL = "jdbc:mysql://localhost/zhihu";
	private static final String USER = "root";
	private static final String PASS = "";
	
	private static Connection conn = null;
	
	static {
		try {
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static Connection getConnection() {
		return conn;
	}
	
	public static void closeConnection() throws SQLException {
		conn.close();
	}
}
