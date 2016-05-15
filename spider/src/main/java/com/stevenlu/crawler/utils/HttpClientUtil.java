package com.stevenlu.crawler.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

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

public class HttpClientUtil {
	
	public static final String HOST = "http://www.zhihu.com"; 
	
	private static CloseableHttpClient httpClient;
	private static CookieStore cookieStore;
	
	static {
		// 配置连接池
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(200);
		cm.setDefaultMaxPerRoute(20);
		HttpHost host = new HttpHost(HOST, 80);
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
		cookieStore = (CookieStore) antiSerializeMyHttpClient("myCookie.dat");
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
		context.setCookieStore(cookieStore);
		
		return context;
	}
	
	/**
	 * 反序列化对象
	 * @param name
	 * @throws Exception
	 */
	public static Object antiSerializeMyHttpClient(String name){
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
