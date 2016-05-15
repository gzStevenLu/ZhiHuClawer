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

	private CloseableHttpClient client;
	private HttpClientContext context;
	private BlockingQueue<String> getPeopleTasks;
	private String href;

	public PageDownloader(BlockingQueue<String> getPeopleTasks, String href) {
		super();
		this.getPeopleTasks = getPeopleTasks;
		this.href = href;
	}

	@Override
	public void run() {
		System.out.println("下载页面：" + href);
		getPage(href);
	}

	public void getPage(String href) {
		client = HttpClientUtil.getHttpClient();
		context = HttpClientUtil.getHttpClientContext();
		
		//HttpGet httpGet = new HttpGet(HttpClientUtil.HOST + "/people/stormzhang/about");
		HttpGet httpGet = new HttpGet(HttpClientUtil.HOST + href + "/followees");
		CloseableHttpResponse response = null;
		try {
			response = client.execute(httpGet, context);
			if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
				System.out.println("访问成功");
				
				saveInMemory(response.getEntity().getContent());
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
	}
	
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
	
	private void saveInMemory(InputStream in) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			StringBuffer sb = new StringBuffer();
			String line ="";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			getPeopleTasks.put(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
