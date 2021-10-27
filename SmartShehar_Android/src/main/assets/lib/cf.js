/*!
 * Common funcs 
 * 
 * Copyright 2011, Chetan Temkar
 *
 * Date: Thu Mar 31 15:28:23 2011 -0400
 */
 
var CTIME = 100;	// Cookie expires after 100 days 
function loadStyles() {
	bl = BrowserLevel(); 
	
	if(bl == 'A') {
		document.write('<meta name="viewport" content="user-scalable=no, width=device-width" />');
	}  
	loadjscssfile("css/basic.css", 'css');
	loadjscssfile("css/main.css", 'css');
}	

// Disable screen and display wait gif
function screenWait(waitFlag) {
	if(waitFlag) {
		document.getElementById("loadingDiv").className = "loading-visible";
	} else {
		document.getElementById("loadingDiv").className = "loading-invisible";
	}
}


// See if smartphone or lower
function BrowserLevel() {
	browser = navigator.userAgent.toLowerCase();

	if(browser.search('safari') || browser.search('android') )
			return 'A';
	if(browser.search('firefox'))
		return 'FF'; // Firefox problems with offline storage
	if(browser.search('blackberry'))
		return 'BB'; // Firefox problems with offline storage
		
	else
			return 'C';
}


// distance between 2 lat, lon pairs
function distanceLatLon(lat1, lon1, lat2, lon2) {
	var R = 6371; // km
	var dLat = (lat2-lat1) * Math.PI / 180;
	var dLon = (lon2-lon1) * Math.PI / 180; 
	var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
			Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2* Math.PI / 180) * 
			Math.sin(dLon/2) * Math.sin(dLon/2); 
	var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
	var d = R * c;
	return Math.ceil(d * 1000);	// return distance in meters
}


function getLocation(successCallBack, failureCallBack) {
	var lat, lon, gps, options, geo, watch;
	gps = navigator.geolocation;
	options = {timeout:60000, maximumAge:330000};

	if(gps) {
		browserSupportFlag = true;
		navigator.geolocation.getCurrentPosition(function(position) {
			lat = position.coords.latitude;
			lon = position.coords.longitude;
		}, function() {
			handleNoGeolocation(browserSupportFlag, failureCallBack);
		});
	// Try Google Gears Geolocation
	} else if (google.gears) {
		browserSupportFlag = true;
		geo = google.gears.factory.create('beta.geolocation');
		geo.getCurrentPosition(function(position) {
			//setGeoPos(pos);
			successCallBack(pos);
			
			//showLocation(position);
		}, function() {
		handleNoGeoLocation(browserSupportFlag, failureCallBack);
		});
	// Browser doesn't support Geolocation
	} else {
		browserSupportFlag = false;
		handleNoGeolocation(browserSupportFlag, failureCallBack);
	}

	if(gps != undefined) {	
		trackerId = gps.watchPosition(function(pos){
			//setGeoPos(pos);
			successCallBack(pos);
			//showLocation(pos);

		}, errorHandler, options);
	}
	else if (geo != undefined) {
		watch = geo.watchPosition(function(pos){
			//setGeoPos(pos);
			successCallBack(pos);
			//showLocation(pos);

		}, errorHandler, options); 
	}
	
}

  function handleNoGeolocation(browserFlag, failureCallBack) {
    if (!browserFlag) {
		failureCallBack({"errno":"-1", "errormsg":"Browser does not support GeoLocation"});
    } else {
		failureCallBack({"errrno":"-2", "errormsg":"Have you given permission for tracking your position?"});
    }
//    map.setCenter(initialLocation);
  }

function geoErrorHandler(err) {

/*  if(err.code == 1) {
    dBug("geo:", "Error: Access is denied!", 5);
  }else if( err.code == 2) {*/
    dBug("geo", "Error: Position is unavailable!", 5);
//  }
}

function errorHandler(err) {
  if(err.code == 1) {
    dBug("geo:", "Error: Access is denied!", 5);
  }else if( err.code == 2) {
    dBug("geo", "Error: Position is unavailable!", 5);
  }
}



// Format a date into hours, minutes
function timeStr(dt) {
	var currentTime = dt;
	var hours = currentTime.getHours();
	var minutes = currentTime.getMinutes();

	var suffix = "AM";
	if (hours >= 12) {
		suffix = "PM";
		hours = hours - 12;
	}
	if (hours === 0) {
		hours = 12;
	}

	if (minutes < 10)
		minutes = "0" + minutes;

	return hours + ":" + minutes + " " + suffix;

}

function distFormat(d, maxd) {	// d in meters, maxd -max distance allowed in km., else show as NA
	if(maxd === undefined)
		maxd = 10;
	if(d / 1000 > maxd || typeof(d) == undefined || isNaN(d))
		return "N/A";
	if( d >= 1000)
		return Math.round((d/ 1000)*100)/100 + '&nbsp;km';
	else
		return Math.round(d*1000)/1000 + '&nbsp;m';

}
	
function getFile(fileName){
    oxmlhttp = null;
    try{
        oxmlhttp = new XMLHttpRequest();
        oxmlhttp.overrideMimeType("text/xml");
    }
    catch(e){
        try{
            oxmlhttp = new ActiveXObject("Msxml2.XMLHTTP");
        }
        catch(e){
            return null;
        }
    }
    if(!oxmlhttp) return null;
    try{
       oxmlhttp.open("GET",fileName,false);
       oxmlhttp.send(null);
    }
    catch(e){
       return null;
    }
    return oxmlhttp.responseText;
}



function stationTracking(){
	if (trackerId){
		navigator.geolocation.clearWatch(trackerId);
	}
}


function setCookie(c_name,value,exdays)
{
	var exdate=new Date();
	exdate.setDate(exdate.getDate() + exdays);
	var c_value=escape(value) + ((exdays===null) ? "" : "; expires="+exdate.toUTCString());
	document.cookie=c_name + "=" + c_value;
}

function getCookie(c_name)
{
	var i,x,y,ARRcookies=document.cookie.split(";");
	for (i=0;i<ARRcookies.length;i++)
	{
		x=ARRcookies[i].substr(0,ARRcookies[i].indexOf("="));
		y=ARRcookies[i].substr(ARRcookies[i].indexOf("=")+1);
		x=x.replace(/^\s+|\s+$/g,"");
		if (x==c_name)
		{
			return unescape(y);
		}
	}
	return '';
}
	
	
function dBug(sModule, sDebugStr, lvl) {
	if(dBug && lvl < dBugLvl)
		alert(sModule + ': ' + sDebugStr);
	
	
}	


var STATIONNAMEID = 1;
var SEARCHSTR = 0;


function getWinDim() {
	 winH = 460, winW = 630;

	if (document.body && document.body.offsetWidth) {
	 winW = document.body.offsetWidth;
	 winH = document.body.offsetHeight;
	}
	if (document.compatMode=='CSS1Compat' &&
	    document.documentElement &&
	    document.documentElement.offsetWidth ) {
	 winW = document.documentElement.offsetWidth;
	 winH = document.documentElement.offsetHeight;
	}
	if (window.innerWidth && window.innerHeight) {
		winW = window.innerWidth;
		winH = window.innerHeight;
	}
	// winW = screen.width < winW ? screen.width : winW;
	// winH = screen.height < winH ? screen.height : winH;
//	dBug('$window.width: ' + $(window).width() + ', $document.width(): ' +  $(document).width());
	return {
		"width": parseInt(winW, 10),
		"height": parseInt(winH, 10)
	};
}

// get frequency, first bus time, last bus time from busmaster
function getTrainInfo(busno) {
	busno = busno.replace(/[^0-9a-zA-Z]+/g,'').toLowerCase(); 
	busno = busno.replace('expacexp', 'e');
	for(var x=0; x<busMaster.length; x++) {
		if(busmaster[x][0] == busno) {
			return { "firstFrom":busmaster[x][1], "lastFrom":busmaster[x][2],
						"frequency":busmaster[x][3]};
		}	
	}
}

// Call this link when bus is selected to set cookies and then fire the link.
// Done so that parameters are passed through cookies not link 
//	because offline storage does not work with changing links
function goLinkBusRoute(busNo, startStationNo, destStationNo, direction) {
	url = 'journey.html?b=' + busNo + '&s=' + startStationNo + 
			'&d=' + destStationNo + '&dir=' + direction;
	setCookie('cBusNo', busNo, 1);
	setCookie('cStartStationNo', startStationNo, CTIME);
	setCookie('cDestStationNo', destStationNo, CTIME);
	setCookie('cDirection', direction, CTIME);
	
	
	window.location = url;
	return false;
}

// Build the url for showstations
function buildUrl(busNo, startStationNo, destStationNo) {
//	busInfo = getBusInfo(busNo);
	busfreq = "";
	var direction = 'd';
	if(startStationNo < destStationNo)
		direction = 'u';
//	if(busInfo != undefined && busInfo.frequency !="")
//		busfreq = "&nbsp;(&nbsp;" + busInfo.frequency + "&nbsp;min)";
	url = '<a href="#"' + ' onClick="return goLinkBusRoute(' 
				+ "'" + busNo + "'" + ',' + startStationNo + ',' + destStationNo + 
				", '" + direction + "'" + ')">' 
				+ busNo + busfreq + "</a>" ;
	return url;

}

// Show all bestbus cookies for debugging
function showBBCookies() {
alert(printCookie('cMyLat') + printCookie('cMyLon') + printCookie('cStartStation') 
		+ printCookie('cStartStationId') + printCookie('cDestStation') 
		+ printCookie('cDestStationId') + printCookie('cStartStationNameId')  
		+ printCookie('cDestStationNameId') + printCookie('cBusNo') 
		+ printCookie('cStartStationNameid') + printCookie('cDestStationnameId'));
	return;	
}

function printCookie(cookieName) {
	return cookieName + " : " + getCookie(cookieName) + ", ";
}

// This disables enter key for BB where you can select from auto suggest only with enter
function disableEnterKey(e)
{
     var key;      
     if(window.event)
          key = window.event.keyCode; //IE
     else
          key = e.which; //firefox      

     return (key != 13);
}

function showRoutesAtStation(oStation, title) {	// Bus routes at station
	var busList = null;

	document.getElementById("busroutesatstart").innerHTML = title + '(' + oStation.area + ')'; 

	for(var i = 0; i < stationBus.length; i++) {
		if(stationBus[i][1] == oStation.id) {
				busList = stationBus[i][5];
				break;
		}
	}
	var firstStation, lastStation;
	if(busList != null) {
		arNearBuses = busList.split(",");

		routesAtStationStr = '<table id="routesAtStation" >';
		for(var j = 0; j < arNearBuses.length; j++) {
			busNo = arNearBuses[j].split("|")[0].trim();
			for(var a = 0; a < arBusList.length; a++) {
				if(arBusList[a][1] == busNo) {
					firstStation =  arBusList[a][2];
					lastStation =  arBusList[a][3];
					
				}
			}

			busInfo = getBusInfo(busNo);
			busfreq = "";
			if(busInfo != undefined && busInfo.frequency !="")
				busfreq = "&nbsp;(&nbsp;" + busInfo.frequency + "&nbsp;min.)";
				
			busInfo = getBusInfo(busNo);
			busfreq = "";
			if(busInfo != undefined && busInfo.frequency !="")
				busfreq = busInfo.frequency + "&nbsp;min.";
			url = '<a href="#"' + ' onClick="return goLinkBusRoute(' + "'" + busNo + "'" 
				+ ', 0, 0)' + '">' + '<span style="font-weight:bold;">' + busNo + '</span>' +
				'</td><td>' + firstStation + ' - ' + lastStation + 
				'</td><td>' + busfreq + "</a>" ;

			routesAtStationStr = routesAtStationStr + '<tr><td>' + url +  "</a></td></tr>";
//			routesAtStationStr = routesAtStationStr + '<tr><td>' + recentStations[i] + '</td></tr>';
		}

		routesAtStationStr = routesAtStationStr + '</table>';
		document.getElementById("routesAtStation").innerHTML = routesAtStationStr;
	}
}

// Find the station nearest to this lat, lon
function findNearestStation(lat, lon) {
	var nearStation, retNearStation, x, y, dLat, dLon, di;
	nearStation = [];
	retNearStation = [];
	y = 0;
	for (x = 0; x < stationLatLon.length; x += 1) {
		dLat = parseFloat(stationLatLon[x][1]);
		dLon = parseFloat(stationLatLon[x][2]);
		if (Math.abs(position.coords.latitude - dLat) < NEARLAT  
			|| Math.abs(position.coords.longitude - dLon) < NEARLAT) {
			di = distanceLatLon(lat, lon, dLat, dLon);
			if (di < 750) {
				nearStation[di] = stationLatLon[x][0];
				y += 1;
			}
		}
	}
	for (x = 0; x < nearStation.length; x += 1) {
		if (nearStation[x] !== undefined) {
			for (y = 0; y < stationNames.length; y += 1) {
				if (stationNames[y][3] === nearStation[x]) {
					var sname = stationNames[y][1];
					var lmark = stationNames[y][2];
					
					stationnameext = lmark !== null ? sname + ' /' + lmark : sname;
					retNearStation = {"di": x, "id": stationNames[y][3], 
						"area": stationNames[y][0], "stationname": stationNames[y][1], 
						"landmark": stationNames[y][2], 'stationnameext':stationnameext};
					return retNearStation;
				}
			}
//			retNearStation[retNearStation.length] = {"di":x, "stationnameid":nearStation[x]};
		}
	}
	return retNearStation;
}


// Populate the All buses List combo 
function PopulateAllBusList() {

	bCombo = document.getElementById('busNoCombo');
	for(var x=0; x<arBusList.length; x++) {
		var busInfo = getBusInfo(arBusList[x][1]);
		var busfreq = "";
		if(busInfo != undefined && busInfo.frequency !="")
			busfreq = '(' + busInfo.frequency + " min.)";
		var option = document.createElement("option");
		option.text = arBusList[x][1] + " - " + arBusList[x][2] + " : " 
			+ arBusList[x][3]; // + ' ' + busfreq; 
		option.value = arBusList[x][1];
		bCombo.add(option, null); 
	}
//	$("#busNoCombo").selectmenu('refresh', true);
}

function getStationInfo(stationList, stationId)
{
	for(var i=0; i++; i<stationList.length) {
		if(stationId === i)
			return stationList[i];
	}

}

function loadIframe(iframeName, url) {
var $iframe = $('#' + iframeName);
if ( $iframe.length ) {
$iframe.attr('src',url); 
return false;
}
return true;
}

function autoResize(id){
    var newheight;
    var newwidth;

    if(document.getElementById){
        newheight=document.getElementById(id).contentWindow.document .body.scrollHeight;
        newwidth=document.getElementById(id).contentWindow.document .body.scrollWidth;
    }

    document.getElementById(id).height= (newheight) + "px";
    document.getElementById(id).width= (newwidth) + "px";
}

function showAlert (type, message) {
   $("#alert-area").append($("<div class='alert-message " + type + " fade in' data-alert><p> " + message +
   " </p> </div>"));
   $(".alert-message").delay(2000).fadeOut("slow", function () { $(this).remove(); });
}

// Distance between two points (lat, lon)
function distanceBetween(lat1, lon1, lat2, lon2) {
	var R = 6371; // Radius of the earth in km
	var dLat = toRad(lat2-lat1);  // Javascript functions in radians
	var dLon = toRad(lon2-lon1); 
	var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
			Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * 
			Math.sin(dLon/2) * Math.sin(dLon/2); 
	var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
	var d = R * c; // Distance in km
	return d;
}

function toRad(val) {

   return val * Math.PI / 180;;
}

// Split the url and get the params
function gup( name )
{
	name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
	var regexS = "[\\?&]"+name+"=([^&#]*)";
	var regex = new RegExp( regexS );
	var results = regex.exec( window.location.href );
	if( results === null )
		return "";
	else {
		return results[1].replace(/%20/g, ' ');
	}
}
