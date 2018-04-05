var dataPark = `{
    "parks" : [{
		"park_data" : {
			"id": "12",
			"lat": 43.53911887290961,
			"lon": 10.325283900000045,
			"name": "Park Via Galilei",
			"address": "Via Galileo Galilei - Pisa PI 56126",
			"price": 1.2,
			"openT": "12:00",
			"closeT": "24:00",
			"totalParks": 20,
			"freeParks": 12,
			"available": true
		}},
		{
		"park_data": {
			"id": "24",
			"lat": 43.55911887290961,
			"lon": 10.355283900000045,
			"name": "Park Via Buono",
			"address": "Via Buono Buoni - Pisa PI 56126",
			"price": 2,
			"openT": "12:00",
			"closeT": "24:00",
			"totalParks": 30,
			"freeParks": 25,
			"available": false
		}}
	]}`;

var dataSpot = `{
	"spot_data": [
		{ "id": "99", "lat": 43.53912887290961, "lon": 10.325383900000045, "free": true, "user": null},
		{ "id": "11", "lat": 43.53912917290961, "lon": 10.325393900000045, "free": true, "user": null},
		{ "id": "33", "lat": 43.53912927290961, "lon": 10.325413900000045, "free": false, "user": 12}
	]}`;

function requestParkInfo() {
	var parkinfo = JSON.parse(dataPark);
	var parkname, price, free, total, status;
	var elements = [];

	for(var i = 0; i < parkinfo.parks.length; i++) {
		id = parkinfo.parks[i].park_data.id;
		parkname = parkinfo.parks[i].park_data.name;
		price = parkinfo.parks[i].park_data.price;
		free = parkinfo.parks[i].park_data.freeParks;
		total = parkinfo.parks[i].park_data.totalParks;
		status = parkinfo.parks[i].park_data.available;
		opening = parkinfo.parks[i].park_data.openT;
		closing = parkinfo.parks[i].park_data.closeT;
		address = parkinfo.parks[i].park_data.address;

		elements[i] = { 
						id: id,
						parkname: parkname,
						price: price,
						free: free,
						total: total,
						status: status,
						opening: opening,
						closing: closing,
						address: address
					};
	}

	return elements;
}

function requestParkID() {
	return elements = [0, 1];
}

function requestParkLocations() {
	var parkinfo = JSON.parse(dataPark);
	var location = [];
	var latP, lngP;

	for(var i = 0; i < parkinfo.parks.length; i++) {
		latP = parkinfo.parks[i].park_data.lat;
		lngP = parkinfo.parks[i].park_data.lon;
		location[i] = { lat: latP, lng: lngP };
	}

	return location;
}

function requestSpotLocations(parkID) {
	var spotInfo = JSON.parse(dataSpot);
	var location = [];
	var latP, lnpP, free, loc, id;

	for(var i = 0; i < spotInfo.spot_data.length; i++) {
		latP = spotInfo.spot_data[i].lat;
		lngP = spotInfo.spot_data[i].lon;
		loc = { lat: latP, lng: lngP }
		free = spotInfo.spot_data[i].free;
		id = spotInfo.spot_data[i].id;
		location[i] = { loc: loc, id: id, free: free };
	}

	return location;
}