// -----------------------------------------
// MAP.JS
// Permit to manage the map
// ------------------------------------------

var autocomplete;
var map;

$(document).ready(function(){
    
});

// -------------------------------------
// PLACE AUTOCOMPLETE FUNCTIONS
// -------------------------------------

// Initialize place autocomple field
function initAutocomplete(id) {
    if (id == null)
        id = 'place-input';

    var input = document.getElementById(id);
    var options = { types: ['geocode'] };

    autocomplete = new google.maps.places.Autocomplete(input, options);
}

// Get user's geographical location
function getLocation() {
    if (navigator.geolocation)
        navigator.geolocation.getCurrentPosition(placeSetBounds);
}

// Bias the autocomplete object to the user's geographical location
function placeSetBounds(position) {
    var geolocation = {
        lat: position.coords.latitude,
        lng: position.coords.longitude
    };

    var circle = new google.maps.Circle({
        center: geolocation,
        radius: position.coords.accuracy
    });

    autocomplete.setBounds(circle.getBounds());
}

// -------------------------------------
// MAP FUNCTIONS
// -------------------------------------

// Build and show the map
function showMap() {
    var place = autocomplete.getPlace();
    var mapContainer = document.getElementById('map');
    var mapOptions = {
        mapTypeControl: true,
        fullscreenControl: false,
        mapTypeControlOptions: {
            style: google.maps.MapTypeControlStyle.HORIZONTAL_BAR,
            position: google.maps.ControlPosition.TOP_CENTER
        },
    };

    if (place == null || !place.geometry) {
        $('#place-input').attr("placeholder", "Please insert a valid (world) place");
        return;
    }

    map = new google.maps.Map(mapContainer, mapOptions);
    mapSetBounds();

    $('#main-container').fadeOut(400);
    $('#map-container').fadeIn(400);

    $('#search').val($('#place-input').val());
    initAutocomplete('search');
    autocomplete.addListener('place_changed', mapSetBounds);
}

function mapSetBounds() {
    var place = autocomplete.getPlace();

    if (place.geometry.viewport)
        map.fitBounds(place.geometry.viewport);
    else
        map.setCenter(place.geometry.location);

    map.setZoom(17);
}