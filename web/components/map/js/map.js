// -----------------------------------------
// MAP.JS
// Permit to manage the map
// ------------------------------------------

// -----------------------------------------
// 
// MAP CLASS
//
// ------------------------------------------

class MyMap {

    // -----------------------------------------
    // 
    // PUBLIC METHODS
    //
    // ------------------------------------------

    constructor() {
        this.map;
        this.autocomplete;
        this.marker;
        this.importTemplate();
        this.initMap();
        this.initMarker();
        this.initAutocomplete();
    }

    // Bind the map autocomplete object to the object with specified id
    bindAutocomplete(id) {
        this.autocomplete.setAutocomplete(id);            
    }

    unbindAutocomplete() {
        this.autocomplete.setAutocomplete('search');
    }

    // Get current selected place and update the map center
    update() {
        var place = this.autocomplete.getValidPlace();

        if (place == null)
            return false;

        this.mapSetCenter(place);
        return true;
    }

    // Set bounds_changed listener
    addChangeBoundListener(callback) {
        this.map.addListener('bounds_changed', callback);
    }

    // Add markers to map
    mapAddMarkers(locations, clickCallback) {
        this.marker.updateParkMarkers(locations, clickCallback)
    }

    // Reset map markers
    mapResetMarkers() {
        this.marker.resetMarkers();
    }

    mapAddSpot(spotlocation) {
        this.marker.updateSpotMarkers(spotlocation);
    }

    mapResetSpot() {
        this.marker.removeSpotMarkers();
    }

    // Return map bounds
    getBounds() {
        return {
            minLat : this.map.getBounds().getNorthEast().lat(),
            minLon : this.map.getBounds().getNorthEast().lng(),
            maxLat : this.map.getBounds().getSouthWest().lat(),
            maxLon : this.map.getBounds().getSouthWest().lng(),
        }
    }

    // -----------------------------------------
    // 
    // PRIVATE METHODS
    //
    // ------------------------------------------

    // Import the map using HTML imports and append it
    importTemplate() {
        var link, template, mapTemplate;

        link = $('link[rel="import"]')[TMPL_MAP];
        template = link.import.querySelector('template');
        mapTemplate = document.importNode(template.content, true);
        $('.map-col').append(mapTemplate);
    }

    // Initialize the map with custom options
    initMap() {
        var mapContainer = document.getElementById('map');
        var mapOptions = {
            mapTypeControl: true,
            fullscreenControl: false,
            mapTypeControlOptions: {
                style: google.maps.MapTypeControlStyle.HORIZONTAL_BAR,
                position: google.maps.ControlPosition.TOP_CENTER
            },
        };

        this.map = new google.maps.Map(mapContainer, mapOptions);
    }

    initMarker() {
        this.marker = new Marker(this.map);
    }

    // Create an autocomplete object and bind the map to an geographical autocomplete object
    initAutocomplete() {
        this.autocomplete = new Autocomplete('search', this.update.bind(this));
    }

    // Set map center and zoom level based on argument place
    mapSetCenter(place) {
        if (place.geometry.viewport)
            this.map.fitBounds(place.geometry.viewport);
        else
            this.map.setCenter(place.geometry.location);

        this.map.setZoom(17);
    }

}