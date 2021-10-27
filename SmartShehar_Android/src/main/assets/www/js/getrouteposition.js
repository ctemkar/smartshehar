function getRoutePosition(routecd, dirn, successCallBack)
{
var xmlhttp;
//var routetrackurl = getPhpScriptsPath() + "routemapping1.php?routecode=" + routecd + 
var routetrackurl = "php/routemapping.php?routecode=" + routecd + 
"&direction=" + dirn ;
if (window.XMLHttpRequest)
  {// code for IE7+, Firefox, Chrome, Opera, Safari
  xmlhttp=new XMLHttpRequest();
  }
else
  {// code for IE6, IE5
  xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
  }
  
  try
  {
	xmlhttp.onreadystatechange = function() {
		if (xmlhttp.readyState==4 && (xmlhttp.status==200 || xmlhttp.readyState == 0))
		{
			var result = xmlhttp.responseText;
			successCallBack($.parseJSON(xmlhttp.responseText));
			//successCallBack(result);
			//jsonTowardsStation(result, successCallBack);
		}
		
		if (xmlhttp.readyState==4 && xmlhttp.status !=200)
		{
			alert("Error! Status " + xmlhttp.status + '-' + xmlhttp.statusText);
		}	
	}
		xmlhttp.open("GET", routetrackurl , false);
		xmlhttp.send();	
		
	}
	catch(err)
	{
				alert('Error while performing the request: ' + err.name + '; ' + err.message);
	}	
}
