// -----------------------------------------
// MAP.JS
// Permit to manage the map
// ------------------------------------------

// Map main object
var map;

// Call as soon as the document is ready
$(document).ready(function(){
    initMap();
});

// Initialize the map with custom options
function initMap() {
    var mapContainer = document.getElementById('map');
    var mapOptions = {
        mapTypeControl: true,
        fullscreenControl: false,
        mapTypeControlOptions: {
            style: google.maps.MapTypeControlStyle.HORIZONTAL_BAR,
            position: google.maps.ControlPosition.TOP_CENTER
        },
    };

    map = new google.maps.Map(mapContainer, mapOptions);
}

// Search on map using the form in coverboard
function searchOnMap() {
    if (!mapUpdatePlace())
        return;

    showMapHideCover();
    setAutocomplete('search');
    addChangeListener(mapUpdatePlace);
}

// Get current selected place and update the map bounds
function mapUpdatePlace() {
    var place = getValidPlace('#place-input');

    if (place == null)
        return false;

    mapSetBounds(place);
    //mapSetMarkers
    return true;
}

// Set map bound and zoom level based on argument place
function mapSetBounds(place) {
    if (place.geometry.viewport)
        map.fitBounds(place.geometry.viewport);
    else
        map.setCenter(place.geometry.location);

    map.setZoom(17);
}

// Show the map and hide main coverboard
function showMapHideCover() {
    $('#main-container').fadeOut(400);
    $('#map-container').fadeIn(400);

    $('#search').val($('#place-input').val());
}

function addParkMarker(place) {
      var marker = new google.maps.Marker({
        position: map.getCenter(),
        icon: "img/marker.svg",
        map: map
      });
}

function addSpotMarker(coord) {
    // TODO
}