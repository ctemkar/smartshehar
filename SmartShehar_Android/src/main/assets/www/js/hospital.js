var map;
var infowindow;
var placeStr = "";

function initialize() {
	var latitude = 0;
	var longitude = 0;
	if (window.interface){
		latitude = window.interface.getLatitude();
		longitude = window.interface.getLongitude();
	} else {
		latitude = 19.1; longitude = 72.85;
	}
	var pyrmont = new google.maps.LatLng(latitude, longitude);
			map = new google.maps.Map(document.getElementById('map'), {
			  mapTypeId: google.maps.MapTypeId.ROADMAP,
			  center: pyrmont,
			  zoom: 13
			});
	var request = {
		location: pyrmont,
		radius: 2500,
		types: ['hospital']
	};
	var service = new google.maps.places.PlacesService(map);
	service.search(request, callback);
}

      function callback(results, status) {
        if (status == google.maps.places.PlacesServiceStatus.OK) {
          for (var i = 0; i < results.length; i++) {
            createMarker(results[i]);
//			placeStr = placeStr + "<p>" + results[i].name + "</p>";
			var detailsRequest = { reference: results[i].reference }
			serviceDetails = new google.maps.places.PlacesService(map);
			serviceDetails.getDetails(detailsRequest, callbackPlaceDetails);
          }
        }
      }


function callbackPlaceDetails(place, status) {
  if (status == google.maps.places.PlacesServiceStatus.OK) {
    createMarker(place);
	placeStr = placeStr + "<li><div id='place'>" + place.name + "</div>" + 
		"<div id='phone'><a href='tel:" + place.formatted_phone_number + "'>" + 
			place.formatted_phone_number + "</a></div>" +
		"<div id='address'>" + place.formatted_address + "</div>" + 
		"<div id='phone'><a href='google.navigation:q=" + 
		place.name + ", " + place.formatted_address +
		"'>Get Directions</a></div>";
// place.geometry.location.$a + "," + place.geometry.location.ab + 	
	document.getElementById("nlists").innerHTML = placeStr;
  }
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
