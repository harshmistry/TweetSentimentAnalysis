package com.nyu.tweettrend;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


public class BackgroundJobManager implements ServletContextListener {

	private ScheduledExecutorService scheduler;
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("Inside contextDestroyed");
		scheduler.shutdownNow();
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		System.out.println("Inside contextInitialized");
		//scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler = Executors.newScheduledThreadPool(10);
		UpdateTweets updateTweets = new UpdateTweets();	
		try {
			UpdateTweets.init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Task task = new Task();
		scheduler.scheduleWithFixedDelay(updateTweets, 0, 1, TimeUnit.DAYS);
		scheduler.scheduleWithFixedDelay(task, 0, 1,TimeUnit.SECONDS);
	}

}
