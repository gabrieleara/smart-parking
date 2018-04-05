// -----------------------------------------
// MARKER.JS
// Represent the marker object on map
// ------------------------------------------

// -----------------------------------------
// 
// MARKER CLASS
//
// ------------------------------------------

class Marker {

    // -----------------------------------------
    // 
    // PUBLIC METHODS
    //
    // ------------------------------------------

    constructor (map) {
        this.map = map;
        this.markers = [];
        this.spots = [];
    }

    // Bind marker object to the specified map
    setMap(map) {
        this.map = map;
    }

    // Return the map binded to the marker object
    getMap(map) {
        return this.map;
    }

    // Delete the specified marker
    resetMarker(id) {
        for(var i = 0; i < this.markers.length; i++) {
            if(this.markers[i].id == id) {
                this.markers[i].setMap(null);
                this.markers.splice(i, 1);
                return;
            }
        }
    }

    // Delete all markers on the map
    resetMarkers() {
        for(var i = 0; i < this.markers.length; i++)
            this.markers[i].setMap(null);
        this.markers = [];
    }

    // Create a new marker on the map
    createParkMarker(location, i, clickCallback) {
        var newMarker = new google.maps.Marker({
            id: location.id,
            position: location.coord,
            icon: "components/map/img/marker.svg",
            map: this.map
        });
        newMarker.addListener('click', function (id) {
            return function() { clickCallback(id) }
        }(i));
        this.markers.push(newMarker);
    }

    // Create a new set of markers
    createParkMarkers(locations, clickCallback) {
        for(var i = 0; i < locations.length; i++)
            this.createParkMarker(locations[i], clickCallback);
    }

    // Update a set of markers
    updateParkMarkers(locations, clickCallback) {

        // check for new markers and draw it
        for(var i = 0; i < locations.length; i++) {
            var alreadyIn = false;

            for(var j = 0; j < this.markers.length && !alreadyIn; j++)         
                if(locations[i].id == this.markers[j].id)
                    alreadyIn = true;

            if(!alreadyIn)
                this.createParkMarker(locations[i], i, clickCallback);
        }

        // check for deleted markers and hide it
        for(var i = 0; i < this.markers.length; i++) {
            var noMoreIn = true;

            for(var j = 0; j < locations.length && noMoreIn; j++)         
                if(this.markers[i].id == locations[j].id)
                    noMoreIn = false;

            if(noMoreIn)
                this.resetMarker(locations[i].id);
        }
    }

    // Add one spot to the list
    createSpotMarker(spotlocation) {
        var newSpot = new google.maps.Marker({
            id: spotlocation.id,
            zIndex: 99,
            position: spotlocation.coord,
            icon: spotlocation.free ? "components/map/img/pin-green.svg" 
                                    : "components/map/img/pin-red.svg",
            map: this.map
        });
        this.spots.push(newSpot);
    }

    // Add spot markers to the locations provided
    createSpotMarkers(spotlocations) {
        for(var i = 0; i < spotlocations.length; i++)
            this.createSpotMarker(spotlocations[i]);
    }

    updateSpotMarkers(spotlocations) {
        // Check for new spots and draw it
        for(var i = 0; i < spotlocations.length; i++) {
            var alreadyIn = false;

            for(var j = 0; j < this.spots.length && !alreadyIn; j++)         
                if(spotlocations[i].id == this.spots[j].id)
                    alreadyIn = true;

            if(!alreadyIn)
                this.createSpotMarker(spotlocations[i]);
        }

        // Check for deleted markers and hide it
        for(var i = 0; i < this.spots.length; i++) {
            var noMoreIn = true;

            for(var j = 0; j < spotlocations.length && noMoreIn; j++)         
                if(this.spots[i].id == spotlocations[j].id)
                    noMoreIn = false;

            if(noMoreIn)
                this.resetMarker(spotlocations[i]);
        }
    }

    // Remove one spot marker
    removeSpotMarker(id) {
        for(var i = 0; i < this.spots.length; i++) {
            if(this.spots[i].id == id) {
                this.spots[i].setMap(null);
                this.spots.splice(i, 1);
                return;
            }
        }
    }

    // Remove spot markers
    removeSpotMarkers() {
        for(var i = 0; i < this.spots.length; i++)
            this.spots[i].setMap(null);
        this.spots = [];
    }

}

