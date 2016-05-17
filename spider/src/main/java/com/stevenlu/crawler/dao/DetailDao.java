package com.stevenlu.crawler.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import com.stevenlu.crawler.bean.Detail;
import com.stevenlu.crawler.utils.DBUtil;

public class DetailDao {
	
	private static final String ADD_DETAIL = "";
	
	public int batchAddDetail(BlockingQueue<Detail> queue) {
		Connection conn = DBUtil.getConnection();
		PreparedStatement pstmt = null;
		int[] count = null;
		try {
			pstmt = conn.prepareStatement(ADD_DETAIL);
			conn.setAutoCommit(false);
			Iterator<Detail> it = queue.iterator();
			while (it.hasNext()) {
				Detail detail = it.next();
				
				pstmt.addBatch();
			}
			count = pstmt.executeBatch();
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count.length;
	}
	
}
