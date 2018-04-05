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

    map.unbindAutocomplete();
    map.addChangeBoundListener(makeRequest);
    showMapHideCover();
}

// -----------------------
// REQUESTS
// ---------------------

function createRequestHandler() {
    request = new Request();
}

function makeRequest() {
    request.cancelSubscription();
    request.makeRequest(map.getBounds());
    manageUpdate();
    // request.makeSubscription(manageUpdate)
}

// Manage receive information from server
function manageUpdate() {
    
    // parse up-to-date info coming from server
    var parksInfo = request.getParksInfo();

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

function updateParks() {
    var parksLoc = request.getParksLocations();

    sidebar.appendItemsList();
    map.mapAddMarkers(parksLoc, open);
}

function updateSpots() {
    var parkId = sidebar.getDisplayedId();
    var listIndex = sidebar.idToListIndex(parkId);
    var parksLoc = request.getParksLocations();
    var spotsLoc = request.getSpotsLocations(parkId);

    sidebar.appendDetailsList(listIndex);
    map.mapAddMarkers(parksLoc, open);
    map.mapAddSpot(spotsLoc);
}

function open(id) {
    sidebar.appendDetailsList(id);
    
    var parkId = sidebar.getDisplayedId();
    var spotsLoc = request.getSpotsLocations(parkId);

    map.mapAddSpot(spotsLoc);
}

function close(id) {
    sidebar.appendItemsList();
    map.mapResetSpot();
}

// ------------------
// GRAPHICAL UTILITY
// ------------------

// Show the map and hide main coverboard
function showMapHideCover() {
    $('#main-container').fadeOut(400);
    $('#map-container').fadeIn(400);
}