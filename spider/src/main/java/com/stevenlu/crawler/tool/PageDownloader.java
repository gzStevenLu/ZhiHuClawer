package com.stevenlu.crawler.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.stevenlu.crawler.utils.HttpClientUtil;


public class PageDownloader implements Runnable {

	public static final int ABOUT = 1;
	public static final int FOLLOWEE = 2;

	private static final Logger logger = LogManager.getLogger(PageDownloader.class);
	
	private CloseableHttpClient client;
	private HttpClientContext context;
	private BlockingQueue<String> tasks;
	private String href;
	private int mode;

	public PageDownloader(BlockingQueue<String> tasks, String href, int mode) {
		super();
		this.tasks = tasks;
		this.href = href;
		this.mode = mode;
	}

	@Override
	public void run() {
		getPage(href, mode);
	}

	public void getPage(String href, int mode) {
		client = HttpClientUtil.getHttpClient();
		context = HttpClientUtil.getHttpClientContext();
		
		String uri = href;
		String result = "";
		
		if (mode == ABOUT) {
			uri += "/about";
		} else {
			uri += "/followees";
		}
		
		RequestConfig rc = RequestConfig.custom()
				.setConnectTimeout(5000)
				.setProxy(HttpClientUtil.getRandomProxy())
				.setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY)
				.build();
		
		HttpGet httpGet = new HttpGet(HttpClientUtil.HOST + uri);
		httpGet.setConfig(rc);
		httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36");
		CloseableHttpResponse response = null;
		try {
			response = client.execute(httpGet, context);
			if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
				logger.info("成功访问：" + uri);
				result = saveInMemory(response.getEntity().getContent());
				
				if (result != null && !result.isEmpty()) {
					try {
						tasks.put(result);
					} catch (InterruptedException e) {
						logger.catching(e);
					}
				}
			} else {
				logger.debug("访问失败：" + uri + " 返回代码：" + response.getStatusLine().getStatusCode());
			}
		} catch (ConnectTimeoutException e) {
			getPage(href, mode);
		} catch (ClientProtocolException e) {
			logger.catching(e);
		} catch (IOException e) {
			logger.catching(e);
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					logger.catching(e);
				}
			}
		}
	}
	
	private String saveInMemory(InputStream in) {
		StringBuffer sb = new StringBuffer();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line ="";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			logger.catching(e);
		}
		//System.out.println(sb.toString());
		return sb.toString();
	}

	@SuppressWarnings("unused")
	private void saveWebPage(InputStream in) {
		File file = new File("./webpage/b.txt");
		//File file = new File("./webpage/" + System.currentTimeMillis() + ".txt");
		try {
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			StringBuffer sb = new StringBuffer();
			String line ="";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			fos.write(sb.toString().getBytes());
			fos.flush();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
