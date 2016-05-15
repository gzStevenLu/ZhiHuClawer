package com.stevenlu.crawler;

import java.io.File;
import java.io.IOException;
import java.util.ListIterator;
import java.util.concurrent.BlockingQueue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.stevenlu.crawler.bean.People;
import com.stevenlu.crawler.utils.BloomFilter;

public class PageParser implements Runnable{
	
	private BlockingQueue<String> getPageTasks;
	private BloomFilter<String> bloomFilter;
	private String page;
	
	public PageParser(BlockingQueue<String> getPageTasks, String page, BloomFilter<String> bloomFilter) {
		super();
		this.getPageTasks = getPageTasks;
		this.page = page; 
		this.bloomFilter = bloomFilter;
	}
	
	@Override
	public void run() {
		System.out.print("解析页面：");
		parseFollowees(page);
	}

	public void parseFollowees(String page) {
		//File file = new File("./webpage/a.txt");
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

	public void parseAbout() {
		File file = new File("./webpage/b.txt");
		try {
			Document doc = Jsoup.parse(file, "UTF-8");
			Element main = doc.select("div.zm-profile-header-main").first();
			String name = main.select("a.name").html();
			String bio = main.select("span.bio").html();
			if (main.select("div.weibo-wrap").html() != null) {
				String weibo = main.select("a.zm-profile-header-user-weibo").attr("href");
			}
			String location = main.select("span.location").attr("title");
			String bussiness = main.select("span.bussiness").attr("title");
			String gender = "";
			if (main.select("span.gender").select("i").attr("class").equals("icon icon-profile-male")) {
				gender = "male";
			} else {
				gender = "female";
			}
			String employment = main.select("span.employment").html();
			String position = main.select("span.position").html();
			
			Elements navbar = doc.select("div.profile-navbar").select("span");
			String asks = navbar.get(1).html();
			String answers = navbar.get(2).html();
			String posts = navbar.get(3).html();
			String collections = navbar.get(4).html();
			
			Element reputation = doc.select("div.zm-profile-details-reputation").first();
			Elements rs = reputation.getElementsByTag("strong");
			String vote = rs.get(0).html();
			String thank = rs.get(1).html();
			String fav = rs.get(2).html();
			String shares = rs.get(3).html();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
