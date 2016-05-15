package com.stevenlu.crawler.bean;

public class People {
	private String title;
	private String href;
	
	public People(String title, String href) {
		super();
		this.title = title;
		this.href = href;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}

	@Override
	public String toString() {
		return "People [title=" + title + ", href=" + href + "]";
	}
	
}
