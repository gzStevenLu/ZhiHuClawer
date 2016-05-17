package com.stevenlu.crawler;

import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.stevenlu.crawler.bean.Detail;
import com.stevenlu.crawler.dao.DetailDao;
import com.stevenlu.crawler.utils.BloomFilter;


/**
 * Main Controller
 */
public class App 
{
	private BlockingQueue<String> getPageTasks = new ArrayBlockingQueue<>(100);		// 网页下载任务队列
	private BlockingQueue<String> aboutParseTasks = new ArrayBlockingQueue<>(100);		// About解析队列
	private BlockingQueue<String> followeeParseTasks = new ArrayBlockingQueue<>(100);	// Followee解析队列
	private BlockingQueue<Detail> detailList = new ArrayBlockingQueue<>(10);			// Followee解析队列
	private BloomFilter<String> bloomFilter = new BloomFilter<>(0.1, 110);
	
	private boolean stop = false;
	
	public void pageDownloaderCtrl() {
		try {
			getPageTasks.put("/people/steven-lu-93");
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		ExecutorService es = Executors.newFixedThreadPool(50);
		int count = 0;
		while (!stop) {
			try {
				String next = getPageTasks.take();
				es.execute(new PageDownloader(followeeParseTasks, next, PageDownloader.FOLLOWEE));
				es.execute(new PageDownloader(aboutParseTasks, next, PageDownloader.ABOUT));
				count+=2;
			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
		}
		es.shutdown();
		System.out.println("PageDownloader已停止工作\nPageDownloader共下载" + count + "个页面");
	}
	
	public void followeeParserCtrl() {
		ExecutorService es = Executors.newFixedThreadPool(50);
		int count = 0;
		while (!stop) {
			try {
				String next = followeeParseTasks.take();
				es.execute(new FolloweePageParser(getPageTasks, next, bloomFilter));
				//count++;
			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
		}
		es.shutdown();
		System.out.println("FolloweeParser已停止工作\nFolloweeParser共解析" + count + "个页面");
	}
	
	public void aboutParserCtrl() {
		ExecutorService es = Executors.newFixedThreadPool(50);
		int count = 0;
		while (!stop) {
			try {
				String next = aboutParseTasks.take();
				es.execute(new AboutPageParser(detailList, next));
				count++;
			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
		}
		es.shutdown();
		System.out.println("AboutParser已停止工作\nAboutParser共解析" + count + "个页面");
	}
	
	public void savingMonitor() {
		DetailDao dao = new DetailDao();
		while (true) {
			try {
				Thread.currentThread().sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (detailList.remainingCapacity() == 0 || stop == true) {
				dao.batchAddDetail(detailList);
				detailList.clear();
				if (stop) break;
			}
		}
		System.out.println("savingMonitor已停止工作");
	}
	
    public static void main( String[] args )
    {
        App app = new App();
        Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setName("Thread-PageDownloader");
				app.pageDownloaderCtrl();
			}
		});
        
        Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setName("Thread-FolloweeParserCtrl");
				app.followeeParserCtrl();
			}
		});
        
        Thread t3 = new Thread(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setName("Thread-AboutParserCtrl");
				app.aboutParserCtrl();
			}
		});
        
        Thread t4 = new Thread(new Runnable() {
			@Override
			public void run() {
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
