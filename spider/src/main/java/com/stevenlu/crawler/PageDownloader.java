package com.stevenlu.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;

import com.stevenlu.crawler.utils.HttpClientUtil;

public class PageDownloader implements Runnable {

	public static final int ABOUT = 1;
	public static final int FOLLOWEE = 2;
	
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
			uri += "/followee";
		}
		System.out.println("下载页面：" + uri);
		
		HttpGet httpGet = new HttpGet(HttpClientUtil.HOST + uri);
		CloseableHttpResponse response = null;
		try {
			response = client.execute(httpGet, context);
			if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
				System.out.println("访问成功");
				
				result = saveInMemory(response.getEntity().getContent());
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		if (result != null && !result.isEmpty()) {
			try {
				tasks.put(result);
			} catch (InterruptedException e) {
				e.printStackTrace();
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
			e.printStackTrace();
		}
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
