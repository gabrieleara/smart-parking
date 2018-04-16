// ----------------------------
// DASHBOARD.JS
// It contains js function
// to prepare home dashboard page
// ------------------------------

// ----------------------------
// PAGE CODE
// ----------------------------

getUserData();

$(document).ready(function(){
    getTodayData();
});

// ----------------------------
// GET USER FUNCTIONS
// ----------------------------

// AJAX-REQ
// Change navbar link if already logged in
function getUserData() {
    ajaxReq(
        LOGIN_RES, 
        "",     
        getSucc, 
        getErr
    );
}

// AJAX-REP
// Action done in case of success
function getSucc(reply) {
    if (reply.err == null)
        preparePage(reply);
    else
        window.location.replace(FRONT_INDEX);
}

// AJAX-ERR
// Action done in case of failure
function getErr() {
    alert("Error: The server is unreachable, maybe you are not logged in.");
    window.location.replace(FRONT_INDEX);
}

// Prepare page with custom user data
function preparePage(userdata) {
    $('.card-name').html(userdata.username);
    $('.card-about a').html('<img avatar="'+userdata.username+'" class="card-avatar">');
    $('.nav-user-a').attr("title", userdata.username + " - Logout");
    $('.nav-user-a').html('<img avatar="'+userdata.username+'" class="nav-avatar">');
    toggleTooltip();
    initLetterAvatar();
}

// -------------------------------------
// LOGOUT PROCESS
// -------------------------------------

// Submit logout request to server
function submitLogout() {
    ajaxReq(
        LOGOUT_RES,
        "",     
        logoutSucc, 
        logoutErr
    );
}

// Redirect to index if logout was successful or display error
function logoutSucc(reply) {
    if (reply.err == null)
        window.location.replace(FRONT_INDEX);
    else
        alert("Error: " + reply.err);

}

// Show an alert if logout was not possible
function logoutErr() {
    alert("Error: server unreachable.");
}

// -------------------------
// GET LAST TRIP FUNCTION
// -------------------------

// AJAX-REQ
// Get last trip data with AJAX req
function getTodayData() {
    var serialized = "date="+moment().format('YYYYMMDD')+'T000000';
    ajaxReq(
        PAYMENT_RES,
        serialized,     
        getTodayDataSucc, 
        getTodayDataErr
    );
}

// AJAX-REP
// Set gauge data and counter
function getTodayDataSucc(reply) {
    if(reply.err != null)
        return getTodayDataErr();

    var totalMinutes = 0,
        totalCost = 0;

    for(var i = 0; i < reply.payments.length; i++) {
        var startT = moment(reply.payments[i].startT);
        var endT = moment(reply.payments[i].endT);

        totalCost += reply.payments[i].cost;
        totalMinutes += endT.diff(startT, "minutes");
    }

    counterSet(totalMinutes, totalCost);
}

// AJAX-ERR
// Alert with error text in case of failure
function getTodayDataErr() {
    alert("Server unreachable.");
}

// Prepare counter with last trip data
function counterSet(totalMinutes, totalCost) {
    $('#counter-time').html(totalMinutes+'<small class="unit green">m</small>');
    $('#counter-cost').html(totalCost+'<small class="unit green">€</small>');
}

// -------------------------------------
// UTILITY
// -------------------------------------

// Send an ajax req
function ajaxReq(dest, info, succ, err) {
    $.ajax({
        type: "GET",
        url: dest,
        data: info,
        dataType: "json",
        success: succ,
        error: err
    });
}

// Toggle on boostrap tooltip
function toggleTooltip() {
    $('[data-toggle="tooltip"]').tooltip(); 
}