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
    addIdleListener(callback) {
        this.map.addListener('idle', callback);
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

    /*
    // Return map bounds in custom format
    getBounds() {
        return {
            minLat : this.map.getBounds().getSouthWest().lat(),
            minLon : this.map.getBounds().getSouthWest().lng(),
            maxLat : this.map.getBounds().getNorthEast().lat(),
            maxLon : this.map.getBounds().getNorthEast().lng(),
        }
    }
    */

    getBounds() {
        return this.map.getBounds();
    }

    // Calculate map bounds with custom padding
    // See: https://stackoverflow.com/questions/34894732/add-padding-to-google-maps-bounds-contains
    getPaddedBounds(npad, spad, epad, wpad) {
        var SW = this.map.getBounds().getSouthWest();
        var NE = this.map.getBounds().getNorthEast();
        var topRight = this.map.getProjection().fromLatLngToPoint(NE);
        var bottomLeft = this.map.getProjection().fromLatLngToPoint(SW);
        var scale = Math.pow(2, this.map.getZoom());
    
        var SWtopoint = this.map.getProjection().fromLatLngToPoint(SW);
        var SWpoint = new google.maps.Point(((SWtopoint.x - bottomLeft.x) * scale) + wpad, ((SWtopoint.y - topRight.y) * scale) - spad);
        var SWworld = new google.maps.Point(SWpoint.x / scale + bottomLeft.x, SWpoint.y / scale + topRight.y);
        var pt1 = this.map.getProjection().fromPointToLatLng(SWworld);
    
        var NEtopoint = this.map.getProjection().fromLatLngToPoint(NE);
        var NEpoint = new google.maps.Point(((NEtopoint.x - bottomLeft.x) * scale) - epad, ((NEtopoint.y - topRight.y) * scale) + npad);
        var NEworld = new google.maps.Point(NEpoint.x / scale + bottomLeft.x, NEpoint.y / scale + topRight.y);
        var pt2 = this.map.getProjection().fromPointToLatLng(NEworld);
    
        return new google.maps.LatLngBounds(pt1, pt2);
    }

    // Return true if the point (lat, lon) is contained in the map bounds
    contains(lat, lon) {
        map.getBounds().contains({lat: lat, lng: lon});
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

    // Create the marker object and bind it to the current instance of the map
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