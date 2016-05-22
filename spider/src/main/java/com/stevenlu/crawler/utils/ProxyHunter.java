package com.stevenlu.crawler.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.stevenlu.crawler.bean.Proxy;

/**
 * Hello world!
 *
 */
public class ProxyHunter 
{	
	public CountDownLatch latch;  
	
	public String getWebPage(String url) {
		StringBuffer sb = new StringBuffer();
		HttpClient client = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36");
		CloseableHttpResponse response = null;
		try {
			response = (CloseableHttpResponse) client.execute(httpGet);
			
			if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
				System.out.println("访问成功");
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String line = "";
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (response != null)
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return sb.toString();
	}
	
	public List<Proxy> getProxyFromWeb(String web) {
		ArrayList<Proxy> list = new ArrayList<Proxy>();
		
		Document doc = Jsoup.parse(web);
		Elements tbodys = doc.getElementsByTag("tbody");
		for (Element e : tbodys) {
			Elements trs = e.getElementsByTag("tr");
			trs.remove(0);
			for (Element tr : trs) {
				Elements tds = tr.getElementsByTag("td");
				String ip = tds.get(1).html();
				String port = tds.get(2).html();
				list.add(new Proxy(ip, port));
			}
		}
		return list;
	}
	
	public List<Proxy> getProxyFromFile(String filename) {
		ArrayList<Proxy> list = new ArrayList<Proxy>();
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filename));
			String line = "";
			while ((line = br.readLine()) != null) {
				String str[] = line.split(":");
				String ip = str[0];
				String port = str[1];
				list.add(new Proxy(ip, port));
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return list;
	}
	
	public void testIpCtrl(List<Proxy> list, final List<Proxy> goodList) {
		latch = new CountDownLatch(list.size());
		
		for (final Proxy proxy : list) {
			new Thread(new Runnable() {
				public void run() {					
					HttpHost proxyHost = new HttpHost(proxy.IP, proxy.port);
					HttpClient client = HttpClients.createDefault();
					RequestConfig requestConfig = RequestConfig.custom()
							.setSocketTimeout(5000)
							.setConnectTimeout(5000)
							.setProxy(proxyHost)
							.build();
					HttpGet httpGet = new HttpGet("https://www.zhihu.com");
					httpGet.setConfig(requestConfig);
					CloseableHttpResponse response = null;
					try {
						response = (CloseableHttpResponse) client.execute(httpGet);
						
						if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
							goodList.add(proxy);
							System.out.println(proxy);
						} else {
							System.out.println("失败：" + proxy + "  HttpCode：" + response.getStatusLine().getStatusCode());
						}
					} catch (ConnectTimeoutException e) {
						System.out.println("超时：" + proxy);
					} catch (Exception e) {
						
					} finally {
						latch.countDown();
					}
				}
			}).start();
		}
	}
	
	/**
	 * 序列化对象
	 * @param object
	 * @throws Exception
	 */
	public static void serializeObject(Object object,String filePath){
		OutputStream fos = null;
		try {
			fos = new FileOutputStream(filePath);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(object);
			System.out.println("序列化成功");
			oos.flush();
			fos.close();
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    public static void main( String[] args )
    {
    	String web = "";
    	List<Proxy> list = null;
    	List<Proxy> goodList = new CopyOnWriteArrayList<Proxy>();
        ProxyHunter demo = new ProxyHunter();
        
        //web = demo.getWebPage("http://www.xicidaili.com/nn");
        //list = demo.getProxyFromWeb(web);
        list = demo.getProxyFromFile("ip.txt");
        demo.testIpCtrl(list, goodList);
        try {
			demo.latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        serializeObject(goodList, "proxy.dat");
    }
}
