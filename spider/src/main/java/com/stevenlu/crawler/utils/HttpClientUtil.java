package com.stevenlu.crawler.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BestMatchSpecFactory;
import org.apache.http.impl.cookie.BrowserCompatSpecFactory;

import com.stevenlu.crawler.bean.Proxy;

public class HttpClientUtil {
	
	public static final String HOST = "https://www.zhihu.com"; 
	
	private static CloseableHttpClient httpClient;
	private static ArrayList<CookieStore> cookieList;
	private static List<Proxy> proxyList;
	private static List<CloseableHttpClient> clientList;
	
	static {
		// 配置连接池
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(50);
		cm.setDefaultMaxPerRoute(20);
		HttpHost host = new HttpHost(HOST, 433);
		cm.setMaxPerRoute(new HttpRoute(host), 50);
		
		// 配置Cookie规则
		RequestConfig globalConfig = RequestConfig.custom()  
		        .setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY)
		        .build();
		
		// 创建HttpClient
		httpClient = HttpClients.custom()
				.setConnectionManager(cm)
				.setDefaultRequestConfig(globalConfig)
				.build();
		
		// 反序列化Cookie
		cookieList = buildCookieList();
		//cookieStore = (CookieStore) antiSerializeMyHttpClient("myCookie.dat");
		
		// 建立ip库
		proxyList = (List<Proxy>) antiSerializeByName("proxy.dat");
	}
	
	public static CloseableHttpClient getHttpClient() {
		return httpClient;
	}
	
	public static HttpClientContext getHttpClientContext(){
		HttpClientContext context = null;
		context = HttpClientContext.create();
		Registry<CookieSpecProvider> registry = RegistryBuilder
				.<CookieSpecProvider> create()
				.register(CookieSpecs.BEST_MATCH, new BestMatchSpecFactory())
				.register(CookieSpecs.BROWSER_COMPATIBILITY,
						new BrowserCompatSpecFactory()).build();
		context.setCookieSpecRegistry(registry);
		context.setCookieStore(getRandomCookie());
		
		return context;
	}
	
	public static ArrayList<CookieStore> buildCookieList() {
		ArrayList<CookieStore> list = new ArrayList<>();
		File file = new File("./cookies");
		for (String filename : file.list()) {
			File cookieFile = new File(file, filename);
			if (cookieFile.isFile()) {
				CookieStore cookie = (CookieStore) antiSerialize(cookieFile);
				list.add(cookie);
			}
		}
		System.out.println(list.size());
		return list;
	}
	
	public static CookieStore getRandomCookie() {
		Random rand = new Random(System.currentTimeMillis());
		int next = rand.nextInt(cookieList.size());
		return cookieList.get(next);
	}
	
	public static HttpHost getRandomProxy() {
		Random rand = new Random(System.currentTimeMillis());
		int next = rand.nextInt(proxyList.size());
		Proxy p = proxyList.get(next);
		return new HttpHost(p.IP, p.port);
	}
	
	/**
	 * 反序列化对象
	 * @param file
	 * @throws Exception
	 */
	public static Object antiSerialize(File file){
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		Object object = null;
		try {
			fis = new FileInputStream(file);
			ois = new ObjectInputStream(fis);
			object = ois.readObject();
			fis.close();
			ois.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NullPointerException e){
			e.printStackTrace();
		}
		System.out.println("反序列化成功 ");
		return object;
	}
	
	/**
	 * 反序列化对象
	 * @param name
	 * @throws Exception
	 */
	public static Object antiSerializeByName(String name){
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		Object object = null;
		try {
			fis = new FileInputStream(name);
			ois = new ObjectInputStream(fis);
			object = ois.readObject();
			fis.close();
			ois.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NullPointerException e){
			e.printStackTrace();
		}
		System.out.println("反序列化成功 ");
		return object;
	}

}