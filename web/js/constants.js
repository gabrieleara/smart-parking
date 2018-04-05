// -----------------------------------------
// CONSTANTS.JS
// Contains all application constant
// ------------------------------------------

// Templates ids
const TMPL_NAVBAR = 0;
const TMPL_EMPTY = 1;
const TMPL_ITEMS = 2;
const TMPL_DETAILS = 3;
const TMPL_MAP = 4;

// App state
const STATE = Object.freeze({
    EMPTY   : Symbol(0),
    LIST    : Symbol(1),
    DETAILS : Symbol(2)
});

// Server endpoint
const SUB_RES = "servlets/parks";
const PARK_RES = "servlets/parks";
const LOGIN_RES = "servlets/login";
const SIGNUP_RES = "servlets/signup";

// Google MAP API endpoint
const GMAP_RES = "https://www.google.com/maps/dir/?api=1";