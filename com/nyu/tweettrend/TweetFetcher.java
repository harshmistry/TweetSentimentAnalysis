package com.nyu.tweettrend;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import javax.servlet.ServletContext;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

import com.nyu.tweettrend.TweetsData;

public class TweetFetcher implements Runnable{
	private TwitterStream twitterStream;
	final String[] keywordsArray = { "##HowDidWeEndUpHere","#NationalCatDay","#newsisback","#BuyFocusOniTunes","#FridayFeeling","#halloween","USA","#thanks","job"};
    static ArrayList<TweetsData> tweetList=new ArrayList<TweetsData>();
    private ConfigurationBuilder cb;
    private PrintWriter writer;
	//private ServletContext context;
    
    {
    	if(null==cb)
    	{
    		cb = new ConfigurationBuilder();
    		cb.setDebugEnabled(true)
    		.setOAuthConsumerKey(YOUR_KEY)
    		.setOAuthConsumerSecret(YOUR_KEY)
    		.setOAuthAccessToken(YOUR_KEY)
    		.setOAuthAccessTokenSecret(YOUR_KEY);
    		//session.getUserProperties().put("cb",cb);
    	}
    }
    
    public TweetFetcher() {
		// TODO Auto-generated constructor stub
	}
    
    public void run() {
    	twitterStream = new TwitterStreamFactory(cb.build()).getInstance();

		StatusListener listener = new StatusListener() {

			private int totalCount=0;

			@Override
			public void onException(Exception arg0) {
				//System.out.println("onEXception" + arg0.getMessage()+"*******************");

			}

			@Override
			public void onTrackLimitationNotice(int arg0) {
				//System.out.println("onTrackLimitation");
			}

			@Override
			public void onStatus(Status status) {
			
				try {
		
					if(status.getGeoLocation()!=null)
					{
						++totalCount;
						//System.out.println("Total Tweets Received : "+totalCount);
						for(String keyword : keywordsArray)
						{
							if(status.getText().contains(keyword))
							{
								//System.out.println("Tweet received");
								//session.getBasicRemote().sendText(status.getGeoLocation().getLatitude()+","+status.getGeoLocation().getLongitude());
								System.out.println("Adding to list:"+status.getGeoLocation().getLatitude()+","+status.getGeoLocation().getLongitude());
								Utility.getUtility().addCoOrdinate(status.getGeoLocation().getLatitude()+","+status.getGeoLocation().getLongitude());
								//writer.write(status.getGeoLocation().getLatitude()+","+status.getGeoLocation().getLongitude());
								//tweetList.add(new TweetsData(UUID.randomUUID().toString(), keyword, status.getGeoLocation().getLatitude(), status.getGeoLocation().getLongitude()));
								break;	
								
							}
							
						}
												
					}
				}
						
				 catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onStallWarning(StallWarning stat) {
				//System.out.println("onStallWarning");
			}

			@Override
			public void onScrubGeo(long arg0, long arg1) {
				//System.out.println("onScrubGeo");

			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice arg0) {
				// TODO Auto-generated method stub

			}
		};

		twitterStream.addListener(listener);
		twitterStream.filter(new FilterQuery().track(keywordsArray));
    };
    
}
