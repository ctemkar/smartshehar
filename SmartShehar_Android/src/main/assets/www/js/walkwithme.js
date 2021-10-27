"use strict";
var map;
var infowindow;
var placeStr = "";
var marker;
var circle;
var user_email = gup("user_email");
var id = gup("id");
var firstTime = true;
var latitude = 19.1;
var longitude = 72.85;
var accuracy = 500;;
var centerPosition;
function initialize() {
	var latitude = 0;
	var longitude = 0;
	var rad = 0;
	var accuracy = 0;
	var latitude = 19.1; longitude = 72.85;
	user_email = gup("user_email");
	id = gup("id");
	var len = 0;
	
	updateLocation();
  
}

function updateLocation() {

	getWalkWithmeLoc(user_email, id, 
		function(aoFriendLoc) {
			var len = aoFriendLoc.length;
			var row;
			var html = "";
			var datetm = "";
			if(len == 0) {
				document.getElementById("nlists").innerHTML = 
					"<li>NO Friend Location information found </li>";
				return;
			} else {
				row = aoFriendLoc[0];
				latitude = row.lat;
				longitude = row.lon;
				accuracy = parseInt(row.acc);
				datetm = row.date;
			}
			
		if(latitude === undefined || longitude === undefined)
			return;
		document.getElementById("nlists").innerHTML = 
			"<li>Friend's location at " + datetm + " </li>";
		centerPosition = new google.maps.LatLng(latitude, longitude);
		if(firstTime) {	
			var options = {
				zoom: 14,
				center: centerPosition,
				mapTypeId: google.maps.MapTypeId.ROADMAP
			};
			map = new google.maps.Map($('#map')[0], options);
			marker = new google.maps.Marker({
				position: centerPosition,
				map: map,
				icon: 'http://smartshehar.com/images/smiley_neutral.png'
			});
/*			
			circle = new google.maps.Circle({
				center: centerPosition,
				radius: accuracy,
				map: map,
				fillColor: '#0000FF',
				fillOpacity: 0.1,
				strokeColor: '#0000FF',
				strokeOpacity: .2
			 });
			circle.bindTo('center', marker, 'position');
*/			
		} else {
		
//		latitude += .003;
//		var x = new google.maps.LatLng(latitude, longitude);
			
			marker.setPosition(centerPosition);
			map.setCenter(centerPosition);
//			circle.setPosition(x);

		}


      //set the zoom level to the circle's size
//      map.fitBounds(circle.getBounds());
	setTimeout(updateLocation, 5000);
	firstTime = false;

});


}

// "google.navigation:q=19,72.84"	  
      function createMarker(place) {
        var placeLoc = place.geometry.location;
        var marker = new google.maps.Marker({
          map: map,
          position: place.geometry.location
        });

        google.maps.event.addListener(marker, 'click', function() {
          infowindow.setContent(place.name);
          infowindow.open(map, this);
        });
      }

      google.load('maps', '3.0', {
        callback: initialize,
        other_params: 'sensor=false&libraries=places',
      });
