
function showIp(){
	console.log("ajax loaded");
	var xmlhttp = new XMLHttpRequest();
	var points = new google.maps.MVCArray();
	xmlhttp.onreadystatechange=function(){
		var temp = xmlhttp.responseText;
		console.log("state:"+xmlhttp.readyState+"   status:"+xmlhttp.status)
		if(xmlhttp.readyState == 4 && xmlhttp.status==200){
			if(""!=temp){
			console.log("plotting:"+temp)
			points.push(getLatLngFromString(temp))
	    	heatmap.setData(points)
			}
		}
	};
	xmlhttp.open("GET","AjaxServlet",true);
	xmlhttp.send();
}

function getLatLngFromString(ll) {
	console.log("converting to latlong:"+ll)
    var latlng = ll.split(',')
    return new google.maps.LatLng(parseFloat(latlng[0]), parseFloat(latlng[1]));
}