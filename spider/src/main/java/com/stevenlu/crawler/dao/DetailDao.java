package com.stevenlu.crawler.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.stevenlu.crawler.utils.DBUtil;

public class DetailDao {
	
	private static final String ADD_DETAIL = "";
	
	public int batchAddDetail() {
		Connection conn = DBUtil.getConnection();
		PreparedStatement pstmt = null;
		int[] count = null;
		try {
			pstmt = conn.prepareStatement(ADD_DETAIL);
			conn.setAutoCommit(false);
			
			count = pstmt.executeBatch();
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count.length;
	}
	
}
