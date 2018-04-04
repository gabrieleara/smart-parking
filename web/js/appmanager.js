// -----------------------------------------
// APPMANAGER.JS
// It contains js code to manage application
// ------------------------------------------

var map;
var sidebar;
var source; 

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
    map.addChangeBoundListener(forceRefresh);
}

// Search on map using the form in coverboard
function searchOnMap() {
    if (!map.update())
        return;

    map.unbindAutocomplete();
    showMapHideCover();
}

// -----------------------
// REQUESTS
// ---------------------

function createRequestHandler() {
    // CREA RICHIESTA PER IL SERVER SSE/WEBSOCKET
    console.log("Apro connession con server..");
}

function forceRefresh() {
    // FORZA IL SERVER A COMUNICARE DATI
    console.log("Il server verrà interrogato..");

    // SIMULO INTERROGAZIONE SERVER
    var locations = requestParkLocations(); 
    var spots = requestSpotLocations();
    var infos = requestParkInfo();
    var ids = requestParkID();

    var data = {
        locations: locations,
        spots: spots,
        infos: infos,
        ids: ids
    };

    // AGGIORNO L'APP CON I DATI
    manageUpdate(data);
}

// Manage receive information from server
function manageUpdate(data) {

    sidebar.buildItemsList(data.infos, open);
    sidebar.buildDetailsList(data.infos, close);

    switch(sidebar.getState()) {
        case STATE.EMPTY:
            sidebar.appendItemsList();
            map.mapAddMarkers(data.locations, data.ids, open);
            break;
        case STATE.LIST:
            sidebar.appendItemsList();
            map.mapAddMarkers(data.locations, data.ids, open);
            break;
        case STATE.DETAILS:
            var parkId = sidebar.getDisplayedId();
            var listIndex = sidebar.idToListIndex(parkId);
            sidebar.appendDetailsList(listIndex);
            map.mapAddMarkers(data.locations, data.ids, open);
            map.mapAddSpot(data.spots);
            break;
        default:
            console.log("Something bad happen. Please contact webmaster.");
    }

}

function open(id) {
    sidebar.appendDetailsList(id);
    var spots = requestSpotLocations();
    map.mapAddSpot(spots);
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