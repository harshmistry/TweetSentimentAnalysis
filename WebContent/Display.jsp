<!DOCTYPE html>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Display</title>
<link rel="stylesheet" type="text/css" href="CSS/MapStyle.css">
<!-- <script src="JavaScript/websocconn.js" type="text/javascript"></script>  -->
<script src="JavaScript/ajaxServConn.js" type="text/javascript"></script>
</head>
<body onload="showIp();">
	<div id="floating-panel">
      <button onclick="toggleHeatmap()">Toggle Heatmap</button>
      <button onclick="changeGradient()">Change gradient</button>
      <button onclick="changeRadius()">Change radius</button>
      <button onclick="changeOpacity()">Change opacity</button>
      <select id="tag" name="hashTag" onchange="if(this.selectedIndex) doSomething();">
            <option value="-1" selected>--</option>
            <option value="sport">sports</option>
            <option value="#halloween">Halloween</option>
            <option value="music">Music</option>
            <option value="game">Games</option>
            <option value="job">Jobs</option>
            <option value="worldcup">World Cup</option>
            <option value="mobile">Mobile</option>
            <option value="us">USA</option>
            <option value="food">Food</option>
            <option value="restaurant">Restaurant</option>
        </select>
      <input type="hidden" id="ip" name="ip" value="<%=request.getAttribute("IP")%>" />
    </div>
    <div id="map"></div>
	<script src="JavaScript/MapPlot.js" type="text/javascript"></script> 
	<script async defer
        src="https://maps.googleapis.com/maps/api/js?key=AIzaSyCmEaFbuyp5Rz5iJ7YMfywzdgGWDncg5zE&signed_in=true&libraries=visualization&callback=initMap">
    </script>
</body>
</html>