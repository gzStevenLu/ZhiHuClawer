package com.stevenlu.crawler.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import com.stevenlu.crawler.bean.Detail;
import com.stevenlu.crawler.utils.DBUtil;

public class DetailDao {
	
	private static final String ADD_DETAIL = "INSERT INTO detail (name, href, "
			+ "bio, weibo, location, bussiness, gender, employment, position, "
			+ "description, asks, answers, posts, collections, vote, thank, "
			+ "fav, shares, education, eduextra) VALUES (?, ?, ?, ?, ?, "
			+ " ?, ?, ?, ?, ?,  ?, ?, ?, ?, ?,  ?, ?, ?, ?, ?)";
	
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
				pstmt.setString(1, detail.name);
				pstmt.setString(2, detail.href);
				pstmt.setString(3, detail.bio);
				pstmt.setString(4, detail.weibo);
				pstmt.setString(5, detail.location);
				
				pstmt.setString(6, detail.bussiness);
				pstmt.setString(7, detail.gender);
				pstmt.setString(8, detail.employment);
				pstmt.setString(9, detail.position);
				pstmt.setString(10, detail.description);
				
				pstmt.setInt(11, Integer.parseInt(detail.asks));
				pstmt.setInt(12, Integer.parseInt(detail.answers));
				pstmt.setInt(13, Integer.parseInt(detail.posts));
				pstmt.setInt(14, Integer.parseInt(detail.collections));
				pstmt.setInt(15, Integer.parseInt(detail.vote));
				
				pstmt.setInt(16, Integer.parseInt(detail.thank));
				pstmt.setInt(17, Integer.parseInt(detail.fav));
				pstmt.setInt(18, Integer.parseInt(detail.shares));
				pstmt.setString(19, detail.education);
				pstmt.setString(20, detail.education_extra);
				
				pstmt.addBatch();
			}
			count = pstmt.executeBatch();
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
		return count != null ? count.length : 0;
	}
	
}
