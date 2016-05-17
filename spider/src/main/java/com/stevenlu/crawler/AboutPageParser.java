package com.stevenlu.crawler;

import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.stevenlu.crawler.bean.Detail;

public class AboutPageParser implements Runnable{
	
	private BlockingQueue<Detail> list;
	private String page;
	
	public AboutPageParser(BlockingQueue<Detail> list, String page) {
		super();
		this.list = list;
		this.page = page; 
	}
	
	@Override
	public void run() {
		System.out.println("AboutParser解析页面");
		parseAbout(page);
	}
	
	public void parseAbout(String page) {
		Detail data = new Detail();
		Document doc = Jsoup.parse(page);
		
		Element main = doc.select("div.zm-profile-header-main").first();
		data.name = main.select("a.name").html();
		data.href = regexHref(main.select("a.zm-profile-icon-return").attr("href"));
		data.bio = main.select("span.bio").attr("title");
		if (main.select("div.weibo-wrap").html() != null) {
			data.weibo = main.select("a.zm-profile-header-user-weibo").attr("href");
		}
		data.location = main.select("span.location").attr("title");
		data.bussiness = main.select("span.bussiness").attr("title");
		if (main.select("span.gender").select("i").attr("class").equals("icon icon-profile-male")) {
			data.gender = "male";
		} else {
			data.gender = "female";
		}
		data.employment = main.select("span.employment").html();
		data.position = main.select("span.position").html();
		data.description = main.select("span.description.unfold-item").select("span.content").html();
		
		Elements navbar = doc.select("div.profile-navbar").select("span");
		data.asks = navbar.get(1).html();
		data.answers = navbar.get(2).html();
		data.posts = navbar.get(3).html();
		data.collections = navbar.get(4).html();
		
		Element reputation = doc.select("div.zm-profile-details-reputation").first();
		Elements rs = reputation.getElementsByTag("strong");
		data.vote = rs.get(0).html();
		data.thank = rs.get(1).html();
		data.fav = rs.get(2).html();
		data.shares = rs.get(3).html();
		
		try {
			list.put(data);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private String regexHref(String href) {
		String pattern = "/(\\S)*//";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(pattern);
		if (m.find()) {
			return href.substring(m.end());
		} else {
			return null;
		}
	}
}
