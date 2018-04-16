// -----------------------------------------
// APPMANAGER.JS
// It contains js code to manage application
// ------------------------------------------

var map;
var sidebar;
var request; 

$(document).ready(function(){
    createMap();
    createSidebar();
    createRequestHandler();
});

// ----------------
// SIDEBAR
// ---------------

// Create a sidebar and build it
function createSidebar() {
    sidebar = new Sidebar();
}

// ---------------
// MAP
// --------------

// Create and build map
function createMap() {
    map = new MyMap();
    map.bindAutocomplete('place-input');
}

// Search on map using the form in coverboard
function searchOnMap() {
    if (!map.update())
        return;

    map.addIdleListener(makeRequest);
    map.unbindAutocomplete();
    showMapHideCover();
}

// -----------------------
// REQUESTS
// ---------------------

function createRequestHandler() {
    request = new Request();
}

function makeRequest() {
    var mapBounds = calculateBounds(0.3);

    request.cancelSubscription();
    request.makeSubscription(mapBounds, manageUpdate);
}

// -----------------------------
// GRAPHICAL UTILITY & MAIN LOOP
// -----------------------------

// Manage receive information from server
function manageUpdate() {
    var mapBounds = calculateBounds(0.03);    
    var parksInfo = request.getFilteredParksInfo(mapBounds);

    sidebar.buildItemsList(parksInfo, open);
    sidebar.buildDetailsList(parksInfo, close);

    switch(sidebar.getState()) {
        case STATE.EMPTY:
            updateParks();
            break;
        case STATE.LIST:
            updateParks();
            break;
        case STATE.DETAILS:
            updateSpots();
            break;
        default:
            console.log("Something bad happen. Please contact webmaster.");
    }

}

// Get parks location and refresh sidebar/map according to
function updateParks() {
    var parksLoc = request.getParksLocations();

    sidebar.appendItemsList();
    map.mapAddMarkers(parksLoc, open);
}

// Get spot location and refresh sidebar/map according to
function updateSpots() {
    var parkId = sidebar.getDisplayedId();
    var listIndex = sidebar.idToListIndex(parkId);
    var parksLoc = request.getParksLocations();
    var spotsLoc = request.getSpotsLocations(parkId);

    map.mapAddMarkers(parksLoc, open);

    if(listIndex == -1) {
        close();
    } else {
        sidebar.appendDetailsList(listIndex);
        map.mapAddSpot(spotsLoc);
    }
}

// Hide park details and remove spot marker from map
function close() {
    sidebar.appendItemsList();
    map.mapResetSpot();
}

// Show park details and add spot marker to map
function open(id) {
    var listIndex = sidebar.idToListIndex(id);
    var spotsLoc = request.getSpotsLocations(id);

    close();
    sidebar.appendDetailsList(listIndex);
    map.mapAddSpot(spotsLoc);
}

// Show the map and hide main coverboard
function showMapHideCover() {
    $('#main-container').fadeOut(400);
    $('#map-container').fadeIn(400);
}

// Create an onject filled with map dimension and return it
function getMapDimension() {
    return {
        width: $('#map').width(),
        height: $('#map').height()
    }
}

// Calculate map bounds starting from map dimension
function calculateBounds(ratio) {
    var mapDimension = getMapDimension();
    var padWidth = mapDimension.width * (-ratio);
    var padHeight = mapDimension.height * (-ratio);
    return map.getPaddedBounds(padHeight, padHeight, padWidth, padWidth);
}