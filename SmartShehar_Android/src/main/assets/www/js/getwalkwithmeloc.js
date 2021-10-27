function getWalkWithmeLoc(user_emailId, id, successCallBack)
{
var xmlhttp;
var walkwithmeurl = getPhpScriptsPath() + "walkwithmeloc.php?user_email=" + user_emailId + "&id=" + id;
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
			try {
				successCallBack($.parseJSON(xmlhttp.responseText));
			} catch(err) {
				successCallBack("");
			}
		}
		if (xmlhttp.readyState==4 && xmlhttp.status !=200)
		{
			alert("Error! Status " + xmlhttp.status + '-' + xmlhttp.statusText);
		}	
	}
		xmlhttp.open("GET", walkwithmeurl , false);
		xmlhttp.send();	
		
	}
	catch(err)
	{
				alert('Error while performing the request: ' + err.name + '; ' + err.message);
	}	
}

