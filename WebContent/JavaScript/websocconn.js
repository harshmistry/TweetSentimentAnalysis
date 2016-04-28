function showIp()
{
	var ip=document.getElementById("ip").value
	var coord = []
	var points = new google.maps.MVCArray();
	//alert("Hi "+ip)
	var link="ws://"+ip+":8080/TweetTrends/webserver";
	var ws=new WebSocket(link);
	ws.onopen = function(){
    	console.log("Socket connection successful")
    };
    ws.onmessage = function(message){
    	//console.log(message.data)
    	//coord.push(getLatLngFromString(message.data));
    	points.push(getLatLngFromString(message.data))
    	heatmap.setData(points)
    }
}

function getLatLngFromString(ll) {
	//console.log("converting to latlong:"+ll)
    var latlng = ll.split(',')
    return new google.maps.LatLng(parseFloat(latlng[0]), parseFloat(latlng[1])); 
}