<?php
// -------------------------------------
// SESSION.PHP
// contains php script to set/reset
// super global $_SESSION array
// -------------------------------------

// Set $_SESSION array with logged user data
function set_session_array($email, $username, $name, $surname, $bio, $avatar, $cover) {
    $_SESSION['email'] = $email;
    $_SESSION['username'] = $username;
    $_SESSION['name'] = $name;
    $_SESSION['surname'] = $surname;
    $_SESSION['bio'] = $bio;
    $_SESSION['avatar'] = $avatar;
    $_SESSION['cover'] = $cover;
}

// Check if user is logged in
function is_user_logged() {
    if(isset($_SESSION['username']))
        return true;
    else
        return false;
}

// Return username of logged user
function get_user_username() {
    return $_SESSION['username'];
}

// Return username of logged user
function get_user_email() {
    return $_SESSION['email'];
}

// Return name of logged user
function get_user_name() {
    return $_SESSION['name'];
}

// Return surname of logged user
function get_user_surname() {
    return $_SESSION['surname'];
}

// Return bio of logged user
function get_user_bio() {
    return $_SESSION['bio'];
}

// Return avatar of logged user
function get_user_avatar() {
    return $_SESSION['avatar'];
}

// Return cover of logged user
function get_user_cover() {
    return $_SESSION['cover'];
}

?>