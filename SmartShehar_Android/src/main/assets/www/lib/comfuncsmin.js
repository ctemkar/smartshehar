function gup(name){name=name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");var regexS="[\\?&]"+name+"=([^&#]*)";var regex=new RegExp(regexS);var results=regex.exec(window.location.href);if(results===null)return"";else{return results[1].replace(/%20/g,' ')}}function distanceLatLon(lat1,lon1,lat2,lon2){var R=6371;var dLat=(lat2-lat1)*Math.PI/180;var dLon=(lon2-lon1)*Math.PI/180;var a=Math.sin(dLat/2)*Math.sin(dLat/2)+Math.cos(lat1*Math.PI/180)*Math.cos(lat2*Math.PI/180)*Math.sin(dLon/2)*Math.sin(dLon/2);var c=2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));var d=R*c;return Math.ceil(d*1000)}function getLocation(){var trackerId=0;var theUser={};var map={};var gps=navigator.geolocation;if(navigator.geolocation){var options={timeout:60000,maximumAge:330000};gps.getCurrentPosition(showLoc,errorHandler,options)}if(gps!=undefined){trackerId=gps.watchPosition(function(pos){setGeoPos(pos);showLocation(pos)})}}function showLoc(pos){setGeoPos(pos);showLocation(pos)}function errorHandler(err){if(err.code==1){alert("Error: Access is denied!")}else if(err.code==2){alert("Error: Position is unavailable!")}}function timeStr(dt){var currentTime=dt;var hours=currentTime.getHours();var minutes=currentTime.getMinutes();var suffix="AM";if(hours>=12){suffix="PM";hours=hours-12}if(hours===0){hours=12}if(minutes<10)minutes="0"+minutes;return hours+":"+minutes+" "+suffix}function distFormat(d){if(d>=1000)return Math.round((d/1000)*100)/100+'&nbsp;km.';else return Math.round(d*1000)/1000+'&nbsp;m.'}function getFile(fileName){oxmlhttp=null;try{oxmlhttp=new XMLHttpRequest();oxmlhttp.overrideMimeType("text/xml")}catch(e){try{oxmlhttp=new ActiveXObject("Msxml2.XMLHTTP")}catch(e){return null}}if(!oxmlhttp)return null;try{oxmlhttp.open("GET",fileName,false);oxmlhttp.send(null)}catch(e){return null}return oxmlhttp.responseText}function stopTracking(){if(trackerId){navigator.geolocation.clearWatch(trackerId)}}function setCookie(c_name,value,exdays){var exdate=new Date();exdate.setDate(exdate.getDate()+exdays);var c_value=escape(value)+((exdays===null)?"":"; expires="+exdate.toUTCString());document.cookie=c_name+"="+c_value}function getCookie(c_name){var i,x,y,ARRcookies=document.cookie.split(";");for(i=0;i<ARRcookies.length;i++){x=ARRcookies[i].substr(0,ARRcookies[i].indexOf("="));y=ARRcookies[i].substr(ARRcookies[i].indexOf("=")+1);x=x.replace(/^\s+|\s+$/g,"");if(x==c_name){return unescape(y)}}return''}function dBug(sModule,sDebugStr,lvl){if(dBug&&lvl<dBugLvl)alert(sModule+': '+sDebugStr)}function BrowserLevel(){browser=navigator.userAgent;if(browser.search('Safari')||browser.search('Android')||browser.search('Firefox')||browser.search('Mozilla/5'))return'A';else return'C'}var STOPNAMEID=1;var SEARCHSTR=0;function findStopSs(stopnameid){stopnameid=stopnameid.toLowerCase();stops=getStopNames();nStopsFound=0;for(var x=0;x<stops.length;x++){if(stopnameid==stops[x][STOPNAMEID])return stops[x][SEARCHSTR]}return null}function findStopId(stopstr){stopstr=stopstr.toLowerCase();stops=getStopNames();nStopsFound=0;stopsFound=[];stopsSrchStr=[];for(var x=0;x<stops.length;x++){searchstr=stops[x][1].toLowerCase();if(searchstr.search(stopstr)!=-1){nStopsFound++;stopsFound[stopsFound.length]=stops[x][1];stopsSrchStr[stopsSrchStr.length]=stops[x][0]}}return[nStopsFound,stopsFound,stopsSrchStr]}function findStop(stopstr){stopstr=stopstr.replace(/[^0-9a-zA-Z]+/g,'').toLowerCase();stops=getStopNames();nStopsFound=0;stopsFound=[];stopsSrchStr=[];for(var x=0;x<stops.length;x++){searchstr=stops[x][0].replace(/[^0-9a-zA-Z]+/g,'').toLowerCase();if(searchstr.search(stopstr)!=-1){nStopsFound++;stopsFound[stopsFound.length]=stops[x][1];stopsSrchStr[stopsSrchStr.length]=stops[x][0];if(searchstr.length==stopstr.length)break}}return[nStopsFound,stopsFound,stopsSrchStr]}function getSearchStr(stopid){stops=getStopNames();for(var x=0;x<stops.length;x++){if(stopid==stops[x][1]){return stops[x][0]}}return''}function loadjscssfile(filename,filetype){var fileref=null;if(filetype=="js"){fileref=document.createElement('script');fileref.setAttribute("type","text/javascript");fileref.setAttribute("src",filename)}else if(filetype=="css"){fileref=document.createElement("link");fileref.setAttribute("rel","stylesheet");fileref.setAttribute("type","text/css");fileref.setAttribute("href",filename)}if(typeof fileref!="undefined")document.getElementsByTagName("head")[0].appendChild(fileref)}function loadStyles(){bl=BrowserLevel();if(bl=='A'){document.write('<meta name="viewport" content="user-scalable=no, width=device-width" />')}loadjscssfile("css/basic.css",'css');loadjscssfile("css/main.css",'css')}function getWinDim(){var winW=630,winH=460;if(document.body&&document.body.offsetWidth){winW=document.body.offsetWidth;winH=document.body.offsetHeight}if(document.compatMode=='CSS1Compat'&&document.documentElement&&document.documentElement.offsetWidth){winW=document.documentElement.offsetWidth;winH=document.documentElement.offsetHeight}if(window.innerWidth&&window.innerHeight){winW=window.innerWidth;winH=window.innerHeight}return{"wd":parseInt(winW,10),"ht":parseInt(winH,10)}}