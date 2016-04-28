package com.nyu.tweettrend;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ReadPropertiesFile {
	private static ReadPropertiesFile instance;
	private Properties properties;
	private InputStream stream;
	
	public ReadPropertiesFile(String fileName) throws IOException {
		stream=getClass().getClassLoader().getResourceAsStream(fileName);
		if(null==stream)
			System.out.println("Unable to load properties file");
		else
		{
			properties=new Properties();
			properties.load(stream);
		}		
	}
	
	public static ReadPropertiesFile getInstance(String fileName) throws IOException
	{
		if(null == instance)
			instance=new ReadPropertiesFile(fileName);
		return instance;
	}
	
	public String getKey(String key)
	{
		return properties.getProperty(key);
	}
}
