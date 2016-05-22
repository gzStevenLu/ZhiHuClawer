package com.stevenlu.crawler;

import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.stevenlu.crawler.bean.Detail;
import com.stevenlu.crawler.dao.DetailDao;
import com.stevenlu.crawler.tool.AboutPageParser;
import com.stevenlu.crawler.tool.FolloweePageParser;
import com.stevenlu.crawler.tool.PageDownloader;
import com.stevenlu.crawler.utils.BloomFilter;


/**
 * Main Controller
 */
public class App 
{
	private static final Logger logger = LogManager.getLogger(App.class);
	
	private static final String ENTER = "/people/steven-lu-93";
	
	private BlockingQueue<String> getPageTasks = new ArrayBlockingQueue<>(200);			// 网页下载任务队列
	private BlockingQueue<String> aboutParseTasks = new ArrayBlockingQueue<>(100);		// About解析队列
	private BlockingQueue<String> followeeParseTasks = new ArrayBlockingQueue<>(100);	// Followee解析队列
	private BlockingQueue<Detail> detailList = new ArrayBlockingQueue<>(100);			// 产品：等候数据库存储队列
	private BloomFilter<String> bloomFilter = new BloomFilter<>(0.01, 200000);
	
	private boolean stop = false;
	
	public void pageDownloaderCtrl() {
		try {
			getPageTasks.put(ENTER);
		} catch (InterruptedException e1) {
			logger.catching(e1);
		}
		ExecutorService es = Executors.newFixedThreadPool(100);
		int count = 0;
		while (!stop) {
			try {
				String next = getPageTasks.take();
				logger.info("获取第"+ count++ +"个网页：Followee");
				es.execute(new PageDownloader(followeeParseTasks, next, PageDownloader.FOLLOWEE));
				Thread.currentThread().sleep(150L);
				logger.info("获取第"+ count++ +"个网页：About");
				es.execute(new PageDownloader(aboutParseTasks, next, PageDownloader.ABOUT));
				Thread.currentThread().sleep(150L);
			} catch (InterruptedException e) {
				logger.catching(e);
				Thread.currentThread().interrupt();
			}
		}
		es.shutdown();
		logger.info("PageDownloader Thread stop.");
	}
	
	public void followeeParserCtrl() {
		ExecutorService es = Executors.newFixedThreadPool(20);
		int count = 0;
		while (!stop) {
			try {
				String next = followeeParseTasks.take();
				logger.info("解析第"+ count++ +"个网页：Followee");
				es.execute(new FolloweePageParser(getPageTasks, next, bloomFilter));
			} catch (InterruptedException e) {
				logger.catching(e);
				Thread.currentThread().interrupt();
			}
		}
		es.shutdown();
		logger.info("FolloweeParser Thread stop.");
	}
	
	public void aboutParserCtrl() {
		ExecutorService es = Executors.newFixedThreadPool(20);
		int count = 0;
		while (!stop) {
			try {
				String next = aboutParseTasks.take();
				logger.info("解析第"+ count++ +"个网页：About");
				es.execute(new AboutPageParser(detailList, next));
			} catch (InterruptedException e) {
				logger.catching(e);
				Thread.currentThread().interrupt();
			}
		}
		es.shutdown();
		logger.info("AboutParser Thread stop.");
	}
	
	public void savingMonitor() {
		DetailDao dao = new DetailDao();
		int count = 0;
		while (true) {
			if (detailList.remainingCapacity() == 0 || stop == true) {
				dao.batchAddDetail(detailList);
				detailList.clear();
				count += 10;
				logger.info("已存入数据库"+ count +"个数据");
				if (stop) break;
			}
		}
		logger.info("savingMonitor Thread stop.");
	}
	
    public static void main( String[] args )
    {
        App app = new App();
        Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				logger.info("PageDownloader Thread start.");
				Thread.currentThread().setName("Thread-PageDownloader");
				app.pageDownloaderCtrl();
			}
		});
        
        Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				logger.info("FolloweeParserCtrl Thread start.");
				Thread.currentThread().setName("Thread-");
				app.followeeParserCtrl();
			}
		});
        
        Thread t3 = new Thread(new Runnable() {
			@Override
			public void run() {
				logger.info("AboutParserCtrl Thread start.");
				Thread.currentThread().setName("Thread-AboutParserCtrl");
				app.aboutParserCtrl();
			}
		});
        
        Thread t4 = new Thread(new Runnable() {
			@Override
			public void run() {
				logger.info("SavingMonitor Thread start.");
				Thread.currentThread().setName("Thread-SavingMonitor");
				app.savingMonitor();
			}
		});
        
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        
/*        Scanner sc = new Scanner(System.in);
        while (!sc.hasNext()) {
        	
        }
        sc.close();*/
        
    }
}
