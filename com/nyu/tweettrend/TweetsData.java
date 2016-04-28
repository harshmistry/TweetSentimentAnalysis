package com.nyu.tweettrend;

public class TweetsData {

	String uuid;
	String keyword;
	Double latitude;
	Double longitude;
	
	public TweetsData(String uuid, String keyword, Double latitude,Double longitude) 
	{
		this.uuid = uuid;
		this.keyword = keyword;
		this.latitude = latitude;
		this.longitude = longitude;
	}
}
