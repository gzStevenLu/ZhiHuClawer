package com.stevenlu.crawler.bean;

import java.io.Serializable;

public class Proxy implements Serializable{

	private static final long serialVersionUID = -8676901246823215914L;
	
	public String IP;
	public int port;
	
	public Proxy(String iP, String port) {
		super();
		IP = iP;
		this.port = Integer.parseInt(port);
	}

	@Override
	public String toString() {
		return "Proxy [IP=" + IP + ", port=" + port + "]";
	}
	
}
