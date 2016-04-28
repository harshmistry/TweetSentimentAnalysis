package com.nyu.tweettrend;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * Servlet implementation class ServerWebSocket
 */
@WebServlet("/ServerWebSocket")
public class ServerWebSocket extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ServerWebSocket() {
        super();
        // TODO Auto-generated constructor stub
    }   

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//System.out.println("Inside servlet\n"+request.getLocalAddr()+"\n"+request.getRemoteAddr()+"\n"+request.getRemoteHost()
				//+"\n"+request.getRequestURL().toString());
		//response.sendRedirect("Home.jsp");
		InetAddress ip;
		  try {

			ip = InetAddress.getLocalHost();
			System.out.println("Current IP address : " + ip.getHostAddress());
			request.setAttribute("IP", ip.getHostAddress());
			request.getRequestDispatcher("Display.jsp").forward(request, response);

		  } catch (UnknownHostException e) {

			e.printStackTrace();

		  }
	}

}
