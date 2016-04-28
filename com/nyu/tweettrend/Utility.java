package com.nyu.tweettrend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Utility {
	public List<String> coOrdinateList;
	private static Utility utility;
	
	public Utility() {
		coOrdinateList=new ArrayList<String>();
	}
	
	public void addCoOrdinate(String coOrdinate)
	{
		synchronized (coOrdinateList) {
			coOrdinateList.add(coOrdinate);
		}	
	}
	
	public List<String> getCoOrdinateList()
	{
		synchronized (coOrdinateList) {
			return  coOrdinateList;
		}
		 
		//return coOrdinateList;
	}
	
	public static Utility getUtility()
	{
		if(null == utility)
			utility=new Utility();
		return utility;
	}
	
    public boolean remove(Object o) {
        synchronized(coOrdinateList) {return coOrdinateList.remove(o);}
    }
}
