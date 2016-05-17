package com.stevenlu.crawler;

import java.util.ListIterator;
import java.util.concurrent.BlockingQueue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.stevenlu.crawler.bean.People;
import com.stevenlu.crawler.utils.BloomFilter;

public class FolloweePageParser implements Runnable{
	
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
		System.out.println("FolloweeParser解析页面");
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
					System.out.println(p);
					bloomFilter.add(title);
					getPageTasks.put(href);
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}
	
}
