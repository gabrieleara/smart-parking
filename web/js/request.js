// -----------------------------------------
// REQUEST.JS
// Manage request from/to server
// ------------------------------------------

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
	makeSubscription(mapBound, callback) {
		var requiredRes = SUB_RES + this.serialize(mapBound);

		this.source = new EventSource(requiredRes);
		this.source.addEventListener("newParkList", function(event) {
				this.reply = JSON.parse(event.data);
				console.log(this.reply);
				callback();
			}.bind(this), false);
		this.source.addEventListener("parkUpdate", function(event) {
				console.log(JSON.parse(event.data));
				// TODO: implement this
			}.bind(this), false);
		this.source.addEventListener("spotUpdate", function(event) {
				console.log(JSON.parse(event.data));
				// TODO: implement this
			}.bind(this), false);

		/*
		this.source.onmessage = function(event) {
			console.log(event);
			this.reply = JSON.parse(event.data);
			console.log(this.reply);
			callback(); 
		}.bind(this);
		*/
	}

	// Cancel server subscription (if any)
	cancelSubscription() {
		if(this.source) 
			this.source.close();
	}

	// Request data to server and save it internally

	/*
	makeRequest(mapBounds, callback) {
		// TO-DO
		// var info = objectifyCoord(minLat, minLon, maxLat, maxLon)
		if(this.reply == null)
			this.reply = this.sjax_req_get(PARK_RES, mapBounds);
		else
			this.ajax_req_get(PARK_RES, mapBounds, callback);
		
		//console.log(this.reply);
		// WILL BE REMOVED
		// this.reply = JSON.parse(reply);
	}
	*/

	// ---------------------------------------
	// GET PARK INFOS
	// ---------------------------------------

	// Get park general info and return it
	getParksInfo() {
		return RepParser.parseParksInfo(this.reply);
	}
	
	// Get park locations info
	getParksLocations() {
		return RepParser.parseParksLocations(this.reply);
	}

	// ---------------------------------------
	// GET SPOT INFOS
	// ---------------------------------------

	// Get spot general info and return it
	getSpotsInfo(parkId) {
		return RepParser.parseSpotsInfo(parkId, this.reply);
	}

	// Get spot locations info
	getSpotsLocations(parkId) {
		return RepParser.parseSpotsLocations(parkId, this.reply);
	}

	// -----------------------------------------
    // 
    // PRIVATE METHODS
    //
    // ------------------------------------------

	// ---------------------------------------
	// UTILITY
	// ---------------------------------------

	// Serialize into string a map bound object
	serialize(mapB) {
		return "?minLat="+mapB.minLat +
				"&minLon="+mapB.minLon +
				"&maxLat="+mapB.maxLat +
				"&maxLon="+mapB.maxLon;
	}

	// Perform a sync ajax GET query and return the result
	sjax_req_get(dest, info) {
		var serverReply;

		$.ajax({
			type		: "GET",
			async		: false,
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

	// Perform a async ajax GET query and return the result
	ajax_req_get(dest, info, callback) {
		$.ajax({
			type		: "GET",
			async		: true,
			url			: dest,
			data		: info,
			dataType	: "json",
			success		: function (reply) {
				this.reply = reply;
				if (typeof(callback) == 'function')
					callback();
			}.bind(this)
		});
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