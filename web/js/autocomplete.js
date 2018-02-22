// -----------------------------------------
// AUTOCOMPLETE.JS
// Manage the location autocomplete field
// ------------------------------------------

// Autocomplete main object
var autocomplete;

// Call as soon as the document is ready
$(document).ready(function(){
    setAutocomplete('place-input');
});

// Get an input element and set it as autocomplete field
function setAutocomplete(id) {
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

// Return a valid place or set an error message in selector element
function getValidPlace(selector) {
    var place = autocomplete.getPlace();

    if (place == null || !place.geometry) {
        $(selector).attr("placeholder", "Please insert a valid (world) place");
        return null;
    }

    return place;
}

// Set place_changed listener
function addChangeListener(callback) {
    autocomplete.addListener('place_changed', callback);
}