package com.stevenlu.crawler.tool;

import java.util.ListIterator;
import java.util.concurrent.BlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.stevenlu.crawler.bean.People;
import com.stevenlu.crawler.utils.BloomFilter;

public class FolloweePageParser implements Runnable{
	
	private static final Logger logger = LogManager.getLogger(FolloweePageParser.class);
	
	private BlockingQueue<String> getPageTasks;
	private BloomFilter<String> bloomFilter;
	private String page;
	
	public FolloweePageParser(BlockingQueue<String> getPageTasks, String page, BloomFilter<String> bloomFilter) {
		super();
		this.getPageTasks = getPageTasks;
		this.page = page; 
		this.bloomFilter = bloomFilter;
	}
	
	@Override
	public void run() {
		parseFollowees(page);
	}

	public void parseFollowees(String page) {
		Document doc = Jsoup.parse(page);
		Elements es = doc.select("a.zm-item-link-avatar");
		ListIterator<Element> it = es.listIterator();
		while (it.hasNext()) {
			Element e = it.next();
			String title = e.attr("title");
			String href = e.attr("href");
			People p = new People(title, href);
			try {
				// 数据去重
				if (!bloomFilter.contains(title)) {
					logger.info(p.toString());
					bloomFilter.add(title);
					getPageTasks.put(href);
				}
			} catch (InterruptedException e1) {
				logger.catching(e1);
			}
		}
	}
	
}
