package com.stevenlu.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BestMatchSpecFactory;
import org.apache.http.impl.cookie.BrowserCompatSpecFactory;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Login {
	private String email = "hedy901@163.com";
	private String password = "123456";
	private String _xsrf = "";
	private String captcha = "";
	private String remember_me = "true";
	
	private String timestamp = "";
	private String loginUrl = "https://www.zhihu.com/login/email";
	private String captchaUrl = "";
	
	private CloseableHttpClient client;
	private HttpClientContext context;
	
	public Login() {
		super();
		this.timestamp = Long.toString(System.currentTimeMillis());
		this.captchaUrl = "http://www.zhihu.com/captcha.gif?r="+ timestamp +"&type=login";
		this.client = getHttpClient();
		this.context = getHttpClientContext();
	}
	
	/**
	 * 获取登录页面
	 */
	public void getLoginPage() {
		HttpGet httpGet = new HttpGet("http://www.zhihu.com");
		CloseableHttpResponse response = null;
		try {
			httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36");
			response = client.execute(httpGet, context);
			if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					Document doc = Jsoup.parse(EntityUtils.toString(entity));
					Element e = doc.select("div[data-za-module~=(SignInForm)").first();
					_xsrf = e.select("input[name='_xsrf']").first().attr("value");
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				response.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取验证码
	 */
	public void getCaptcha() {
		HttpGet httpGet = new HttpGet(getCaptchaUrl());
		CloseableHttpResponse response = null;
		try {
			httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36");
			response = client.execute(httpGet, context);
			if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
				HttpEntity entity = response.getEntity();
				savePic(entity.getContent(), "D:/Zhihu/");
				System.out.println("请输入验证码：");
				Scanner sc = new Scanner(System.in);
				captcha = sc.nextLine();
				sc.close();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (response != null)
					response.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 正式登录
	 */
	public boolean login() {
		HttpPost httpPost = new HttpPost(loginUrl);
		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("password", password));
		params.add(new BasicNameValuePair("captcha", captcha));
		params.add(new BasicNameValuePair("email", email));
		params.add(new BasicNameValuePair("_xsrf", _xsrf));
		params.add(new BasicNameValuePair("remember_me", remember_me));
		CloseableHttpResponse response = null;
		UrlEncodedFormEntity uefEntity;
		try {
			uefEntity = new UrlEncodedFormEntity(params, "UTF-8");
			httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36");
			httpPost.setEntity(uefEntity);
			try {
				response = client.execute(httpPost, context);
				if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
					HttpEntity entity = response.getEntity();
					System.out.println(EntityUtils.toString(entity, "UTF-8"));
					serializeObject(context.getCookieStore(), "myCookie.dat");
					
					BufferedReader reader = new BufferedReader(
		                    new InputStreamReader(response.getEntity().getContent(), "utf-8"));
					StringBuffer sb = new StringBuffer();
					String line = "";
					while ((line = reader.readLine()) != null) {
						sb.append(line);
					}
					
					JSONObject json = new JSONObject(sb.toString());
					if (json.get("r").toString().equals("0")) {
						serializeObject(context.getCookieStore(), "myCookie.dat");
						return true;
					} else {
						return false;
					}
					
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
			try {
				response.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	private CloseableHttpClient getHttpClient() {
		RequestConfig globalConfig = RequestConfig.custom()  
		        .setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY)
		        .build();  
		CloseableHttpClient client = HttpClients.custom()  
		        .setDefaultRequestConfig(globalConfig)
		        .build();
		
		return client;
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
		return context;
	}
	
	/**
	 * 下载图片
	 * @param in
	 * @param path
	 */
	private void savePic(InputStream in, String path) {
		File dir = new File(path);
		if (dir == null || !dir.exists()) {
			dir.mkdirs();
		}
		
		String filename = Long.toString(System.currentTimeMillis()) + ".gif";
		File file = new File(path + filename);
		if (file == null || !file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			FileOutputStream fos = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len = 0;
			while ((len = in.read(buf)) != -1) {
				fos.write(buf, 0, len);
			}
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private String getCaptchaUrl() {
		timestamp = Long.toString(System.currentTimeMillis());
		captchaUrl = "http://www.zhihu.com/captcha.gif?r="+ timestamp +"&type=login";
		return captchaUrl;
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

	
	public static void main(String[] args) {
		Login test = new Login();
		test.getLoginPage();
		test.getCaptcha();
		test.login();
	}
	
}
