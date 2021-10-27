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


// setup location handler to get location by gps or cell towers
/*
function getCachedLocation(successCallBack, errorCallback) {
    // Request a position. We only accept cached positions, no matter what 
    // their age is. If the user agent does not have a cached position at
    // all, it will immediately invoke the error callback.
    navigator.geolocation.getCurrentPosition(csuccessCallback,
                                             cacheerrorCallback,
                                             {maximumAge:Infinity, timeout:0});

    function successCallback(position) {
      // By setting the 'maximumAge' to Infinity, the position
      // object is guaranteed to be a cached one.
      // By using a 'timeout' of 0 milliseconds, if there is
      // no cached position available at all, the user agent 
      // will immediately invoke the error callback with code
      // TIMEOUT and will not initiate a new position
      // acquisition process.
      if (position.timestamp < freshness_threshold && 
          position.coords.accuracy < accuracy_threshold) {
			successCallBack(position);
      } else {
        // The position is quite old and/or inaccurate.
		successCallBack(position);
      }
    }

    function errorCallback(error) {
      switch(error.code) {
        case error.TIMEOUT:
          // Quick fallback when no cached position exists at all.
          doFallback();
          // Acquire a new position object.
          navigator.geolocation.getCurrentPosition(successCallback, errorCallback);
          break;
        case ... // treat the other error cases.
      };
    }

    function doFallback() {
      // No cached position available at all.
      // Fallback to a default position.
    }
    
}
*/

function getLocation(successCallBack, failureCallBack) {
	var lat, lon, gps, options, geo, watch;
	gps = navigator.geolocation;
	options = {timeout:60000, maximumAge:330000};
	if(gps) {
		browserSupportFlag = true;
		navigator.geolocation.getCurrentPosition(function(position) {
			if(successCallBack) successCallBack(position);
		}, function() {
			handleNoGeolocation(browserSupportFlag);
		});
	// Try Google Gears Geolocation
	} else if (google.gears) {
		browserSupportFlag = true;
		geo = google.gears.factory.create('beta.geolocation');
		geo.getCurrentPosition(function(position) {
			if(successCallBack) successCallBack(position);
		}, function() {
		handleNoGeoLocation(browserSupportFlag);
		});
	// Browser doesn't support Geolocation
	} else {
		browserSupportFlag = false;
		handleNoGeolocation(browserSupportFlag);
	}

	if(gps != undefined) {	
		trackerId = gps.watchPosition(function(position){
			if(successCallBack) successCallBack(position);

		}, geoErrorHandler, options);
	}
	else if (geo != undefined) {
		watch = geo.watchPosition(function(position){
			if(successCallBack) successCallBack(position);
		}, geoErrorHandler, options); 
	}
}


  function handleNoGeolocation(errorFlag) {
    if (errorFlag == true) {
//     alert("Geolocation service failed.");
      initialLocation = null;
    } else {
//      alert("Your browser doesn't support geolocation");
      initialLocation = null;
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

// return the first search string for a stationnameid
function findStationSs(stationnameid)
{
	stationnameid = stationnameid.toLowerCase();
	stations = getStationNames();
	nStationsFound = 0;
	for(var x=0; x < stations.length; x++) {
		if(stationnameid == stations[x][STATIONNAMEID])
			return stations[x][SEARCHSTR];
	}
	return null;
}

// Find a stationnameid in the stationnames array and returns information about it
// returns no. of stations found, 
function findStationId(stationid) {
	stationid = stationid.toLowerCase();
	stationNames = getStationNames();
	nStationsFound = 0;
	stationsFound = [];
	stationsSrchStr = [];
	for(var x=0; x < stationNames.length; x++) {
		searchstr = stationNames[x][3].toLowerCase();
		if(searchstr.search(stationid) != -1) {
			nStationsFound++;
			stationsFound = stationNames[x][3];
			stationsSrchStr = stationNames[x][0] + ': ' + stationNames[x][1] + ' ' + stationNames[x][2];
			break;
		}
	}
	return {'nostationsFound':nStationsFound, 'stationsfound':stationsFound, 'stationsrchstr':stationsSrchStr};
}


// Find a station searchstr in the stationnames array and returns information about it
// returns no. of stations found, 
function findStation(stationstr) {
	stationstr = stationstr.replace(/[^0-9a-zA-Z]+/g,'').toLowerCase(); // remove special characters, spaces
	stations = getStationNames();
	nStationsFound = 0;
	stationsFound = [];
	stationsSrchStr = [];
	for(var x=0; x < stations.length; x++) {
		searchstr = stations[x][0].replace(/[^0-9a-zA-Z]+/g,'').toLowerCase();
		if(searchstr.search(stationstr) != -1) {
			nStationsFound++;
			stationsFound[stationsFound.length] = stations[x][1];
			stationsSrchStr[stationsSrchStr.length] = stations[x][0];
			if(searchstr.length == stationstr.length)
				break;	// exact match, so we have found our station
		}
	}
	return [nStationsFound, stationsFound, stationsSrchStr];
}

// Find a searchstr based on the station name id	
function getSearchStr(stationid) {
	stations = getStationNames();
	for(var x=0; x < stations.length; x++) {
		if(stationid == stations[x][1]) {
//			startStationLat = stationll[STATIONLAT];
//			startStationLon = stationll[STATIONLON];
//			startstationidx = x;
			return stations[x][0];
		}
	}
	return '';
}


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
//		busfreq = "&nbsp;(&nbsp;" + busInfo.frequency + "&nbsp;mins)";
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
				busfreq = "&nbsp;(&nbsp;" + busInfo.frequency + "&nbsp;mins)";
				
			busInfo = getBusInfo(busNo);
			busfreq = "";
			if(busInfo != undefined && busInfo.frequency !="")
				busfreq = busInfo.frequency + "&nbsp;mins";
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

function showRoute(busNo)
{
		goLinkBusRoute(busNo, 0, 0);
}
// Show a complete bus route
function showRouteComplete() {

	bTxt = $('#busNo').val();
	// Find the route in array
	if(bTxt.length > 0) {
		bTxt = bTxt.replace(/ /g, "");
		bTxt = bTxt.toUpperCase();
		var busFound = false;
		for(var x=0; x<arBusList.length; x++) {
			if(bTxt == arBusList[x][1].replace(/ /g, "").toUpperCase()) {
				bTxt = arBusList[x][1];
				busFound = true;
				break;
			}
		}
		if(!busFound) {
			alert('Bus not found, please re-enter');
			return false;
		}
		busNo = bTxt;
		setCookie('cBusNo', bTxt, 365);
	}
	else {
		bCombo = document.getElementById('busNoCombo');
		busNo = bCombo.options[bCombo.selectedIndex].value;
	}

	goLinkBusRoute(busNo, 0, 0);

return;

}				

// Populate the All buses List combo 
function PopulateAllBusList() {

	bCombo = document.getElementById('busNoCombo');
	for(var x=0; x<arBusList.length; x++) {
		var busInfo = getBusInfo(arBusList[x][1]);
		var busfreq = "";
		if(busInfo != undefined && busInfo.frequency !="")
			busfreq = '(' + busInfo.frequency + " mins)";
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
