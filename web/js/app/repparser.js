// -----------------------------------------
// RepParser.JS
// Parse reply from/to server
// ------------------------------------------

// -----------------------------------------
// 
// REPPARSER CLASS
//
// ------------------------------------------

class RepParser {
    
    // -----------------------------------------
    // 
    // PUBLIC METHODS
    //
    // ------------------------------------------

    // ------------------------------------------
    // PARSING NEW REPLY
    // ------------------------------------------

	// Parse object returned by the server and build a new one
	static parseParksInfo(reply) {
		var parks = [];

		for(var i = 0; i < reply.parks.length; i++)
            parks[i] = this.buildParkInfoObj(reply.parks[i]);

		return parks;
    }
    
    // Filter object returned by the server, parse it and build new one
    static filterParseParksInfo(reply, newMapBounds) {
        return this.parseParksInfo(this.filterReply(newMapBounds, reply));
    }

	// Parse object returned by the server and build one with geo information
	static parseParksLocations(reply) {
		var locations = [];

		for(var i = 0; i < reply.parks.length; i++)
			locations[i] = this.buildParkLocationObj(reply.parks[i]);

		return locations;
	}

	// Parse object returned by the server and build a new one
	static parseSpotsInfo(parkId, reply) {
		var spots = [];

		for(var i = 0; i < reply.parks.length; i++)
            if(reply.parks[i].id == parkId) {
                spots = this.buildSpotInfoObj(reply.parks[i].spots);
                break;
            }

		return spots;
	}

	// Parse object returned by the server and build one with geo information
	static parseSpotsLocations(parkId, reply) {
		var spots = [];

		for(var i = 0; i < reply.parks.length; i++) {
			if(reply.parks[i].id == parkId) {
				for(var j = 0; j < reply.parks[i].spots.length; j++)
					spots[j] = this.buildSpotLocationObj(reply.parks[i].spots[j]);
				break;
			}
		}
		
		return spots;
    }

    // ------------------------------------------
    // PARSING UPDATE PACKETS
    // ------------------------------------------

    // Parse park update packet and update reply object
    static parseParkUpdating(reply, update) {
        for(var i = 0; i < reply.parks.length; i++) {
            if(reply.parks[i].id == update.parkID) {
                this.updateParkStatus(reply.parks[i], update.price);
                return;
            }
        }
    }

    // Parse spot update packet and update reply object
    static parseSpotUpdating(reply, update) {
        for(var i = 0; i < reply.parks.length; i++) {
            if(reply.parks[i].id == update.parkID) {
                this.updateSpotStatus(reply.parks[i], update.id, update.free);
                return;
            }
        }
    }

    // -----------------------------------------
    // 
    // PRIVATE METHODS
    //
    // ------------------------------------------
    
    // ---------------------------------------
	// OBJECT BUILDERS
    // ---------------------------------------

    // Build park info obj starting from single park object
    static buildParkInfoObj(park) {
        var parkInfo = {};

        parkInfo = this.copyObjectByValue(park);
        parkInfo.link = this.dirUrlBuilder(park.lat, park.lon);
        parkInfo.free = this.countFreeSpot(park);
        parkInfo.total = this.countTotalSpot(park);
        delete parkInfo.spots;

        return parkInfo;
    }
    
    // Build park location obj starting from single park object
    static buildParkLocationObj(park) {
        return {
            id	: park.id,
            coord : {
                lat : parseFloat(park.lat),
                lng : parseFloat(park.lon)
            },
        };
    }

    // Build all spots info obj starting from spots array
    static buildSpotInfoObj(spots) {
        return this.copyObjectByValue(spots);
    }

    // Build spot location obj starting from single spot object
    static buildSpotLocationObj(spot) {
        return {
            id		:	spot.id,
            free	:	spot.free,
            coord 	: {
                lat :	parseFloat(spot.lat),
                lng :	parseFloat(spot.lon)
            }
        };
    }

    // Update park status with the one passed
    static updateParkStatus(park, newPrice) {
        park.price = newPrice;
    }

    // Update park status with the one passed
    static updateSpotStatus(park, spotId, spotStatus) {
        for(var i = 0; i < park.spots.length; i++) {
            if(park.spots[i].id == spotId) {
                park.spots[i].free = spotStatus;
                return;
            }
        }
    }

    // ---------------------------------------
	// UTILITY
    // ---------------------------------------

    // Return a new "reply" object after filtering using map bounds
    static filterReply(mapBounds, reply) {
        var filteredReply = { parks: [] };

        for(var i = 0; i < reply.parks.length; i++)
            if(this.mapContains(mapBounds, reply.parks[i].lat, reply.parks[i].lon))
                filteredReply.parks.push(reply.parks[i]);

        return filteredReply;
    }

    // Return true if the point (lat, lon) is contained in the map bounds
    // See: https://developers.google.com/maps/documentation/javascript/reference/3/#LatLngBounds
    static mapContains(mapBounds, lat, lon) {
        return mapBounds.contains({lat: lat, lng: lon});        
    }

	// Take lat/lon and build Google MAP direction url
	static dirUrlBuilder(lat, lon) {
		return GMAP_RES + "&destination="+lat+","+lon;
    }

    // Copy an object by value and return it
    static copyObjectByValue(src) {
        return Object.assign({}, src);
    }

    // Count free spot in a single park
    static countFreeSpot(park) {
        var free = 0;

        for(var i = 0; i < park.spots.length; i++)
            if(park.spots[i].free)
                free += 1;
        
        return free;
    }

    // Count total spot in a single park
    static countTotalSpot(park) {
        return park.spots.length;
    }

}