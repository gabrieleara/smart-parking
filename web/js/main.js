// -----------------------------------------
// MAIN.JS
// It contains js code to permit login/signup
// ------------------------------------------

var navbar;

$(document).ready(function(){
    createNavbar();
    checkIfLogged();
});

// --------------------------------------
// NAVBAR
// --------------------------------------

// Create navbar and add objects to it
function createNavbar() {
    var elements = [
        { id: "about", caption: "About", url: "about.html" },
        { id: "dashboard", caption: "Dashboard", url: "dashboard.html"}
    ];
    var toggles = [
        { id: "login", caption: "Login", modalId: "login-modal" },
        { id: "signup", caption: "Sign up", modalId: "signup-modal"}
    ];
    navbar = new Navbar(elements, toggles);
}

// -------------------------------------
// LOGGED/NOT-LOGGED USER FUNCTION
// -------------------------------------

// Make an ajax req to know if user is logged or not
function checkIfLogged() {
    ajaxReq(LOGIN_RES, "", checkSucc, checkErr);
}

// Hide login/signup button and show dashboard one
function checkSucc(reply) {
    if (reply.err == null) {
        navbar.hideElement("login");
        navbar.hideElement("signup");
    } else {
        navbar.hideElement("dashboard");
    }
}

// Hide dashboard button (server not reachable)
function checkErr() {
    navbar.hideElement("dashboard");
}

// -------------------------------------
// SUBMIT LOGIN FORM SUCCESS
// -------------------------------------

// Submit validation form login
function submitLogin() {
    ajaxReq(
        LOGIN_RES,
        $("#form-login").serialize(),     
        loginSucc, 
        loginErr
    );
}

// Redirect to dashboard if login was successful or display error
function loginSucc(reply) {
    if (reply.err == null) {
        navbar.hideElement("login");
        navbar.hideElement("signup");
        navbar.showElement("dashboard");
        $('#login-modal').modal('hide');
    } else {
        $(".login-error-title").html("Error: ");
        $(".login-error-text").html(reply.err);
        showAlert(".alert-login");
    }
}

// Show an alert if login was not possible
function loginErr() {
    $(".login-error-title").html("Error: ");
    $(".login-error-text").html("server unreachable.");
    showAlert(".alert-login");
}

// -------------------------------------
// SUBMIT SIGNUP FORM SUCCESS
// -------------------------------------

// Submit validation form sign up
function submitSignup() {
    ajaxReq(
        SIGNUP_RES,
        $("#form-signup").serialize(),     
        signupSucc, 
        signupErr
    );
}

// Action done in case of success
function signupSucc(reply) {
    if (reply.err == null) {
        navbar.hideElement("login");
        navbar.hideElement("signup");
        navbar.showElement("dashboard");
        $(".signup-success-title").html("Success: ");
        $(".signup-success-text").html("registered with success!");
        showAlert(".alert-signup-succ");
        $("#form-signup :input").prop('disabled', true);
        $("#signup-modal .btn-primary").prop('disabled', true);
    } else {
        $(".signup-error-title").html("Error: ");
        $(".signup-error-text").html(reply.err);
        showAlert(".alert-signup");
    }
}

// Action done in case of failure
function signupErr() {
    $(".signup-error-title").html("Error: ");
    $(".signup-error-text").html("server unreachable.");
    showAlert(".alert-signup");
}

// -------------------------------------
// UTILITY
// -------------------------------------

// Perform an AJAX request
function ajaxReq(dest, info, succ, err) {
    $.ajax({
        type: "POST",
        url: dest,
        data: info,
        dataType: "json",
        success: succ,
        error: err
    });
}

// Display an alert to explain server unreachability
function showAlert(htmlclass) {
    $(htmlclass).removeClass("d-none");
    $(htmlclass).addClass("show");
}