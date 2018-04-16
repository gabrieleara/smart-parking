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
const LOGOUT_RES = "servlets/logout";
const SIGNUP_RES = "servlets/register";
const PAYMENT_RES = "servlets/payments";

// Client endpoint
const FRONT_INDEX = "index.html";
const DASH_INDEX = "dashboard.html";

// Google MAP API endpoint
const GMAP_RES = "https://www.google.com/maps/dir/?api=1";

// Date-picker main settings
const MAX_PAYMENTS_PER_PAGE = 4;