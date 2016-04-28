package com.nyu.tweettrend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.nyu.tweettrend.DynamoDataRetrieval;
import com.nyu.tweettrend.TLocation;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Servlet implementation class AjaxServlet
 */
@WebServlet("/AjaxServlet")
public class AjaxServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AjaxServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	//	ServletContext context = getServletContext();
		
		System.out.println("Inside doGet()");
		response.setContentType("text/html");
		response.setHeader("Cache-Control", "no-cache");
		//response.setHeader("Connection", "keep-alive");
		PrintWriter writer=response.getWriter();
		
		DynamoDataRetrieval dynamoDataRetrieval = new DynamoDataRetrieval();
		List<TLocation> locationData = dynamoDataRetrieval.getItems("job");
		String st = "",lat="[",longi="[";
		List<String> items = new ArrayList<String>();
		int i=0;
		for (TLocation temp : locationData) {
			st = "{lat:" + temp.getLatitude() + ",lng:"+ temp.getLongitude() + "}";
			System.out.println(st);
			writer.write(temp.getLatitude()+","+temp.getLongitude());
			writer.close();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
