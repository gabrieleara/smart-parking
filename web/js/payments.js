// -------------------------
// PAYMENTS.JS
// It contains all js code to
// fill up payments page
// -----------------------

getUserData();

$(document).ready(function(){
    datepickerBuild();
    datepickerchangeEventRegister();
    datepickerchangeEventTrigger();
});

// -------------------------------------
// REDIRECT USER FUNCTION
// -------------------------------------

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
    alert("Server unreachable.");
    window.location.replace(FRONT_INDEX);
}

// Build page with user data
function preparePage(userdata) {
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

// -----------------------------
// DATE PICKER BUILDER FUNCTION
// -----------------------------

// build the datepicker
function datepickerBuild() {
    $('#datetimepicker').datetimepicker({
        format: 'DD/MM/YYYY',
        inline: true
    });
}

// -----------------------------
// PAYMENTS TABLE AJAX FUNCTION
// -----------------------------

// AJAX-REQ
// register event function
function datepickerchangeEventRegister() {
    $('#datetimepicker').on('dp.change', function(e){
        var date;

        if(e != null)
            date = moment(e.date).format('YYYYMMDD');
        else
            date = moment().format('YYYYMMDD');

        var serialized = "date="+date+'T000000';
        ajaxReq(PAYMENT_RES, serialized, tableFill, tableFillError);
    });
}

// trigger datepicker change event
function datepickerchangeEventTrigger() {
    $('#datetimepicker').trigger('dp.change');
}

// AJAX-REP
// crash table fill
function tableFill(reply) {
    if(reply.err != null) {
        tableFillEmpty();
        return;
    }
    
    paginationReset();
    paginationCreate(reply.payments.length);
    tableReset();

    for(var i = 0; i < reply.payments.length; i++) {
        $('.payment-table tbody').append(`
        <tr style="display: none;">
            <th>`+(i+1)+`</th>
            <td>`+reply.payments[i].cost.toLocaleString('it-IT', {style: 'currency', currency: 'EUR'})+`</td>
            <td>`+moment(reply.payments[i].startT).format("LLL")+`</td>
            <td>`+moment(reply.payments[i].endT).format("LLL")+`</td>
        </tr>
        `);
    }

    showRows(0);
}

// AJAX-ERR
// alert with an error in case of server error
function tableFillError() {
    tableFillEmpty();
    alert("Unable to fill table - Server unrechable!");
}

// --------------------------
// TABLE UTILITY FUNCTION
// --------------------------

// reset table content
function tableReset() {
    $('.payment-table tbody').html("");
}

// fill the table with empty row
function tableFillEmpty() {
    paginationReset();
    $('.payment-table tbody').html(`
        <tr>
            <th>-</th>
            <td>-</td>
            <td>-</td>
            <td>-</td>
        </tr>`);
}

// show only limited number of rows
function showRows(init_number) {
    for(var i = 0; i < $('.payment-table tbody tr').length; i++) {
        if(i >= init_number && i < MAX_PAYMENTS_PER_PAGE+init_number)
            $('.payment-table tbody tr').eq(i).css("display", "table-row");
        else
            $('.payment-table tbody tr').eq(i).css("display", "none");
    }
}

// ---------------------------
// PAGINATION UTILITY FUNCTION
// ----------------------------

// reset pagination to default one
function paginationReset() {
    $('.pagination').html(`<li class="page-item active">
                                <a class="page-link" href="javascript:pagechange(1)">1</a>
                        </li>`);
}

// create button for pagination
function paginationCreate(number) {
    if(number > MAX_PAYMENTS_PER_PAGE) {
        for(var i = 0; i < (number % MAX_PAYMENTS_PER_PAGE); i++) {
            $('.pagination').append(`
                <li class="page-item">
                    <a class="page-link" href="javascript:pagechange(`+(i+2)+`)">`+(i+2)+`</a>
                </li>
            `);
        }
    }
}

// display the selected page
function pagechange(pagenum) {
    $('.pagination li').removeClass("active");
    $('.pagination li').eq(pagenum-1).addClass("active");
    showRows((pagenum-1) * MAX_PAYMENTS_PER_PAGE);
}

// -------------------------------------
// GENERAL UTILITY
// -------------------------------------

// make an ajax req
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

// toggle on boostrap tooltip
function toggleTooltip() {
    $('[data-toggle="tooltip"]').tooltip(); 
}
