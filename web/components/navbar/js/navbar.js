// -----------------------------------------
// NAVBAR.JS
// Build a navbar and permit to show it
// ------------------------------------------

// -----------------------------------------
// 
// NAVBAR CLASS
//
// ------------------------------------------

class Navbar {

    // -----------------------
    // 
    // PUBLIC METHODS
    //
    // -----------------------

    constructor(elements, toggles) {
        this.navbarTemplate;
        this.importTemplate();
        
        for (var i = 0; i < elements.length; i++)
            this.addElement(elements[i].id, elements[i].caption, elements[i].url);
        
        for (var i = 0; i < toggles.length; i++)
            this.addToggle(toggles[i].id, toggles[i].caption, toggles[i].modalId);

        this.appendTemplate();
    }

    showElement(id) {
        $('#btn-'+id).show();
    }

    hideElement(id) {
        $('#btn-'+id).hide();
    }

    // -----------------------
    // 
    // PRIVATE METHODS
    //
    // -----------------------

    // Import the using HTML imports
    importTemplate() {
        var link, template;

        link = $('link[rel="import"]')[TMPL_NAVBAR];
        template = link.import.querySelector('template');
        this.navbarTemplate = document.importNode(template.content, true);
    }

    // Add an element to the navbar
    addElement(id, caption, url) {
        var btnName = "btn-" + id;
        var liElement = '<li class="nav-item" id="'+btnName+'"></li>';
        var aElement = '<a class="nav-link" href="'+url+'" class="btn btn-primary">'+caption+'</a>';

        $(this.navbarTemplate).find('ul.navbar-nav').append(liElement);
        $(this.navbarTemplate).find('#'+btnName).append(aElement);
    }

    // Add a modal toggle
    addToggle(id, caption, modalId) {
        var btnName = "btn-" + id;
        var liElement = '<li class="nav-item" id="'+btnName+'"></li>';
        var aElement = '<a class="nav-link" href="#" class="btn btn-primary" data-toggle="modal" data-target="#'+modalId+'">'+caption+'</a>';

        $(this.navbarTemplate).find('ul.navbar-nav').append(liElement);
        $(this.navbarTemplate).find('#'+btnName).append(aElement);
    }

    // Append navbar to the header of the page
    appendTemplate() {
        $('header').append(this.navbarTemplate);
    }

}


