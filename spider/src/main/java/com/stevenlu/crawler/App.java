package com.stevenlu.crawler;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.stevenlu.crawler.utils.BloomFilter;


/**
 * Main Controller
 */
public class App 
{
	private BlockingQueue<String> getPageTasks = new ArrayBlockingQueue<>(100);
	private BlockingQueue<String> getPeopleTasks = new ArrayBlockingQueue<>(100);
	private BloomFilter<String> bloomFilter = new BloomFilter<>(0.1, 110);
	
	public void pageDownloaderCtrl() {
		try {
			getPageTasks.put("/people/steven-lu-93");
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		ExecutorService es = Executors.newFixedThreadPool(50);
		int count = 0;
		while (count < 100) {
			try {
				String next = getPageTasks.take();
				es.execute(new PageDownloader(getPeopleTasks, next));
				count++;
			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
		}
		es.shutdown();
		System.out.println("共下载" + count + "个页面");
	}
	
	public void pageParserCtrl() {
		ExecutorService es = Executors.newFixedThreadPool(50);
		int count = 0;
		while (count < 100) {
			try {
				String next = getPeopleTasks.take();
				es.execute(new PageParser(getPageTasks, next, bloomFilter));
				count++;
			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
		}
		es.shutdown();
		System.out.println("共解析" + count + "个页面");
	}
	
    public static void main( String[] args )
    {
        App app = new App();
        Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				app.pageDownloaderCtrl();
			}
		});
        Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				app.pageParserCtrl();
			}
		});
        
        t1.start();
        t2.start();
    }
}
