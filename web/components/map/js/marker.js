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
    resetMarker(index) {
        this.markers[index].setMap(null);
        this.markers.splice(index, 1);
    }

    // Delete all markers on the map
    resetMarkers() {
        for(var i = 0; i < this.markers.length; i++)
            this.markers[i].setMap(null);
        this.markers = [];
    }

    // Create a new marker on the map
    createParkMarker(location, clickCallback) {
        var newMarker = new google.maps.Marker({
            id: location.id,
            position: location.coord,
            icon: "components/map/img/marker.svg",
            map: this.map
        });
        newMarker.addListener('click', function (id) {
            return function() { clickCallback(id) }
        }(location.id));
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
        for(var i = locations.length-1; i >= 0; i--) {
            var alreadyIn = false;

            for(var j = this.markers.length-1; j >= 0 && !alreadyIn; j--)         
                if(locations[i].id == this.markers[j].id)
                    alreadyIn = true;

            if(!alreadyIn)
                this.createParkMarker(locations[i], clickCallback);
        }

        // check for deleted markers and hide it
        for(var i = this.markers.length-1; i >= 0; i--) {
            var noMoreIn = true;

            for(var j = locations.length-1; j >= 0 && noMoreIn; j--)         
                if(this.markers[i].id == locations[j].id)
                    noMoreIn = false;

            if(noMoreIn)
                this.resetMarker(i);
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

    // Change dinamically spot icon and update the state
    changeSpotIcon(index) {
        this.spots[index].free = !this.spots[index].free;

        if(this.spots[index].free)
            this.spots[index].setIcon("components/map/img/pin-green.svg");
        else
            this.spots[index].setIcon("components/map/img/pin-red.svg");
    }

    updateSpotMarkers(spotlocations) {
        // check for new spots and draw it
        for(var i = spotlocations.length-1; i >= 0; i--) {
            var alreadyIn = -1;

            for(var j = this.spots.length-1; j >= 0 && alreadyIn < 0; j--)         
                if(spotlocations[i].id == this.spots[j].id)
                    alreadyIn = j;
                
            if(alreadyIn >= 0 && spotlocations[i].free != this.spots[alreadyIn].free)
                this.changeSpotIcon(alreadyIn);
            else if(alreadyIn == -1)
                this.createSpotMarker(spotlocations[i]);
        }

        // check for deleted markers and hide it
        for(var i = this.spots.length-1; i >= 0; i--) {
            var noMoreIn = true;

            for(var j = spotlocations.length-1; j >= 0 && noMoreIn; j--)         
                if(this.spots[i].id == spotlocations[j].id)
                    noMoreIn = false;

            if(noMoreIn)
                this.removeSpotMarker(i);
        }
    }

    // Remove one spot marker
    removeSpotMarker(index) {
        this.spots[index].setMap(null);
        this.spots.splice(index, 1);
    }

    // Remove spot markers
    removeSpotMarkers() {
        for(var i = 0; i < this.spots.length; i++)
            this.spots[i].setMap(null);
        this.spots = [];
    }

}

