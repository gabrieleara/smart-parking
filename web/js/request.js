// -----------------------------------------
// REQUEST.JS
// Manage request from/to server
// ------------------------------------------

// ---------------------------
// JSON SAMPLES
// ---------------------------

var reply = `{
    "parks" : [
		{
			"id"		: 	"12",
			"address"	: 	"Via Galileo Galilei - Pisa PI 56126",
			"price"		: 	1.2,
			"name"		: 	"Park Via Galilei",
			"spots"		: 
							[
								{ 
									"id"	: 	"99",
									"lon"	: 	"10.325383900000045", 
									"free"	: 	true, 
									"user"	: 	null,
									"lat"	: 	"43.53912887290961"
								},
								{ 
									"id"	: 	"11",
									"lon"	: 	"10.325393900000045", 
									"free"	: 	true, 
									"user"	: 	null,
									"lat"	: 	"43.53912917290961"
								},
								{ 
									"id"	: 	"33", 
									"lon"	: 	"10.325413900000045", 
									"free"	: 	false, 
									"user"	: 	12,
									"lat"	: 	"43.53912927290961"
								}
							],
			"closeT"	: 	"24:00",
			"lon"		: 	"10.325283900000045",
			"openT"		: 	"12:00",
			"lat"		: 	"43.53911887290961",
			"available"	: 	true
		},
		{
			"id"		: 	"24",
			"address"	: 	"Via Buono Buoni - Pisa PI 56126",
			"price"		: 	2,
			"name"		: 	"Park Via Buono",
			"spots"		: 
							[
								{ 
									"id"	: 	"99",
									"lon"	: 	"10.355383900000045", 
									"free"	: 	true, 
									"user"	: 	null,
									"lat"	: 	"43.55921887290961"
								},
								{ 
									"id"	: 	"11",
									"lon"	: 	"10.355483900000045", 
									"free"	: 	true, 
									"user"	: 	null,
									"lat"	: 	"43.55931887290961"
								},
								{ 
									"id"	: 	"33", 
									"lon"	: 	"10.355583900000045", 
									"free"	: 	false, 
									"user"	: 	12,
									"lat"	: 	"43.55941887290961"
								}
							],
			"closeT"	: 	"24:00",
			"lon"		: 	"10.355283900000045",
			"openT"		: 	"12:00",
			"lat"		: 	"43.55911887290961",
			"available"	: 	true
		}
	]}`;

// -----------------------------------------
// 
// REQUEST CLASS
//
// ------------------------------------------

class Request {

    // -----------------------------------------
    // 
    // PUBLIC METHODS
    //
    // ------------------------------------------

	constructor() {
		this.reply;
		this.source;
	}

	// -----------------------------------------------
	// MAKE SERVER SUBSCRIPTION/REQUEST
	// -----------------------------------------------

	// Register to server updates
	makeSubscription(callback) {
		this.source = new EventSource(SUB_RES);
		this.source.onmessage = function(event) {
			this.reply = event.data;
			callback(); 
		}
	}

	// Cancel server subscription (if any)
	cancelSubscription() {
		if(this.source)
			this.source.close();
	}

	// Request data to server and save it internally
	makeRequest(mapBounds) {
		// TO-DO
		// var info = objectifyCoord(minLat, minLon, maxLat, maxLon)
		this.reply = this.ajax_req_get(PARK_RES, mapBounds);
		console.log(this.reply);

		// WILL BE REMOVED
		// this.reply = JSON.parse(reply);
	}

	// ---------------------------------------
	// GET PARK INFOS
	// ---------------------------------------

	// Get park general info and return it
	getParksInfo() {
		return this.parseParksInfo(this.reply);
	}
	
	// Get park locations info
	getParksLocations() {
		return this.parseParksLocations(this.reply);
	}

	// ---------------------------------------
	// GET SPOT INFOS
	// ---------------------------------------

	// Get spot general info and return it
	getSpotsInfo(parkId) {
		return this.parseSpotsInfo(parkId, this.reply);
	}

	// Get spot locations info
	getSpotsLocations(parkId) {
		return this.parseSpotsLocations(parkId, this.reply);
	}

	// -----------------------------------------
    // 
    // PRIVATE METHODS
    //
    // ------------------------------------------

	// ---------------------------------------
	// UTILITY
	// ---------------------------------------

	// Take vars and make them object-shaped
	objectifyCoord(minLat, minLon, maxLat, maxLon) {
		return {
			minLat: minLat,
			minLon: minLon,
			maxLat: maxLat,
			maxLon: maxLon
		};
	}

	// Take LAT/LON and build Google MAP direction url
	gmapUrlEncoder(lat, lon) {
		return GMAP_RES + "&destination="+lat+","+lon;
	}

	// Parse object returned by the server and build a new one
	parseParksInfo(reply) {
		var parks = [];

		for(var i = 0; i < reply.parks.length; i++) {

			var free = 0;
			var total = reply.parks[i].spots.length;
			
			for(var j = 0; j < total; j++)
				if(reply.parks[i].spots[i].free)
					free += 1;

			parks[i] = Object.assign({}, reply.parks[i]);
			parks[i].free = free;
			parks[i].total = total;
			parks[i].link = this.gmapUrlEncoder(parks[i].lat, parks[i].lon);
			delete parks[i].spots;

		}

		return parks;
	}

	// Parse object returned by the server and build one with geo information
	parseParksLocations(reply) {
		var locations = [];

		for(var i = 0; i < reply.parks.length; i++) {
			locations[i] = {
				id	:	reply.parks[i].id,
				coord : {
					lat :	parseFloat(reply.parks[i].lat),
					lng :	parseFloat(reply.parks[i].lon)
				},
			};
		}

		return locations;
	}

	// Parse object returned by the server and build a new one
	parseSpotsInfo(parkId, reply) {
		var spots = [];

		for(var i = 0; i < reply.parks.length; i++)
			if(reply.parks[i].id == parkId)
				spots[i] = Object.assign({}, reply.parks[i].spots);

		return spots;
	}

	// Parse object returned by the server and build one with geo information
	parseSpotsLocations(parkId, reply) {
		var spots = [];

		for(var i = 0; i < reply.parks.length; i++) {
			if(reply.parks[i].id == parkId) {
				for(var j = 0; j < reply.parks[i].spots.length; j++)
					spots[j] = {
						id		:	reply.parks[i].spots[j].id,
						free	:	reply.parks[i].spots[j].free,
						coord 	: {
							lat :	parseFloat(reply.parks[i].spots[j].lat),
							lng :	parseFloat(reply.parks[i].spots[j].lon)
						}
					};
				break;
			}
		}
		
		return spots;
	}
	
	// Perform a sync ajax GET query and return the result
	ajax_req_get(dest, info) {
		var serverReply;

		$.ajax({
			type		: "GET",
			async		: true,
			url			: dest,
			data		: info,
			dataType	: "json",
			success		: function (reply) {
				serverReply = reply;
			},
			error		: function (reply) {
				serverReply = {error: "Error in request."};
			}
		});

		return serverReply;
	}

	// Perform an async ajax POST query and return the result
	ajax_req_post(dest, info, succ, err) {
		$.ajax({
			type: "POST",
			url: dest,
			data: info,
			dataType: "json",
			success: succ,
			error: err
		});
	}

}