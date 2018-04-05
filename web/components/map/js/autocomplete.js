// -----------------------------------------
// AUTOCOMPLETE.JS
// Manage the location autocomplete field
// ------------------------------------------

// -----------------------------------------
// 
// AUTOCOMPLETE CLASS
//
// ------------------------------------------

class Autocomplete {

    // -----------------------------------------
    // 
    // PUBLIC METHODS
    //
    // ------------------------------------------

    constructor(id, callback) {
        this.id;
        this.autocomplete;
        this.setAutocomplete(id);
        
        if(callback != null)
            this.addChangeListener(callback);
    }

    // Create and nind the autocomplete field to an input with specified id
    setAutocomplete(id) {
        var input = document.getElementById(id);
        var options = { types: ['geocode'] };

        if(this.id != null)
            $('#'+id).val($('#'+this.id).val());

        input.addEventListener("focus", this.getUserLocation.bind(this));
        
        this.autocomplete = new google.maps.places.Autocomplete(input, options);
        this.id = id;  
    }

    // Return previously created autocomplete field
    getAutocomplete() {
        return this.autocomplete;
    }

    // Return a valid place or set an error message in selector element
    getValidPlace() {
        var place = this.autocomplete.getPlace();

        if (place == null || !place.geometry) {
            $('#'+this.id).attr("placeholder", "Please insert a valid (world) place");
            return null;
        }

        return place;
    }

    // Set place_changed listener
    addChangeListener(callback) {
        this.autocomplete.addListener('place_changed', callback);
    }

    // -----------------------------------------
    // 
    // PRIVATE METHODS
    //
    // ------------------------------------------

    // Get user's geographical location and call the callback
    getUserLocation() {
        if (navigator.geolocation)
            navigator.geolocation.getCurrentPosition(this.setBounds.bind(this));
    }

    // Bias the autocomplete object to the user's geographical location
    setBounds(position) {
        var geolocation = {
            lat: position.coords.latitude,
            lng: position.coords.longitude
        };

        var circle = new google.maps.Circle({
            center: geolocation,
            radius: position.coords.accuracy
        });

        this.autocomplete.setBounds(circle.getBounds());
    }
}