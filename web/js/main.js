// -----------------------------------------
// MAIN.JS
// It contains js code to permit login/signup
// ------------------------------------------

var navbar;

$(document).ready(function(){
    createNavbar();
});

// -------------------
// NAVBAR
// -------------------

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