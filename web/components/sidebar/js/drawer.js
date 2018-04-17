// -----------------------------------------
// DRAWER.JS
// Manage sidebar graphical aspects
// ------------------------------------------

// -----------------------------------------
// 
// SIDEBARDRAWER CLASS
//
// ------------------------------------------

class SidebarDrawer {

    // -----------------------------------------
    // 
    // PUBLIC METHODS
    //
    // ------------------------------------------
    
    // -----------------------------------------
    // MANAGE GRAPHICAL ASPECT (EMPTY ITEM)
    // ------------------------------------------

    // Append the empty list template to sidebar
    static addSidebarEmpty(emptyItem) {
        this.addSidebarObject(emptyItem);
    }

    // Clean the previous attached element and append the item
    static cleanAddEmptyList(emptyItem) {
        this.removeSidebarElements();
        this.addSidebarEmpty(emptyItem);
    }

    // -----------------------------------------
    // MANAGE GRAPHICAL ASPECT (LIST ITEM)
    // ------------------------------------------

    // Append all items to sidebar
    static addSidebarItemList(listItems) {
        for(var i = 0; i < listItems.length; i++)
            this.addSidebarObject(listItems[i]);
    }

    // Clean the previous attached element and append the item
    static cleanAddItemList(listItems) {
        this.removeSidebarElements();
        this.addSidebarItemList(listItems);
    }

    // Get location item hidden field id
    static getItemId(item) {
        return this.getText(item, '.location-list-id');
    }

    // Set location item hidden field id
    static setItemId(item, id) {
        this.setText(item, '.location-list-id', id);
    }

    // Set location item component title
    static setItemTitle(item, title) {
        this.setText(item, '.location-list-title > h5', title);
    }

    // Set location item component price
    static setItemPrice (item, price) {
        this.setText(item, '.location-list-price-text', price.toLocaleString('it-IT', {style: 'currency', currency: 'EUR'}));
    }

    // Set location item component spot number
    static setItemSpotNumber(item, free, total) {
        this.setText(item, '.location-list-park-text', free + '/' + total);
    }

    // Set location item component status
    static setItemStatus(item, status) {
        var onlineHTML = '<i class="fa fa-check green"></i> Online';
        var offlineHTML = '<i class="fa fa-times red"></i> Offline';

        if(status == true)
            this.setHTML(item, '.location-list-footer-text', onlineHTML);
        else
            this.setHTML(item, '.location-list-footer-text', offlineHTML);
    }

    // Set a callback function for the open action of details item
    static setItemOpenAction(item, id, callback) {
        this.setOnClickEvent(item, '.location-container-link', callback, id);
    }

    // -----------------------------------------
    // MANAGE GRAPHICAL ASPECT (DETAILS)
    // ------------------------------------------

    // Append all items to sidebar
    static addSidebarDetails(listDetails, listIndex) {
        this.addSidebarObject(listDetails[listIndex]);
    }

    // Clean the previous attached element and append the item
    static cleanAddSidebarDetails(listDetails, listIndex) {
        this.removeSidebarElements();
        this.addSidebarDetails(listDetails, listIndex);
    }

    // Get location item hidden field id
    static getDetailsId(item) {
        return this.getText(item, '.location-details-id');
    }

    // Get location item on page hidden field id
    static getOnPageDetailsId() {
        return this.getText(document, '.location-details-id');
    }

    // Set location details hidden field id
    static setDetailsId(item, id) {
        this.setText(item, '.location-details-id', id);
    }

    // Set location details component title
    static setDetailsTitle(item, title) {
        this.setText(item, '.location-details-title > h4', title);
    }

    // Set location details component price
    static setDetailsPrice(item, price) {
        this.setText(item, '.location-details-price-text', price.toLocaleString('it-IT', {style: 'currency', currency: 'EUR'}));
    }

    // Set location details component spot number
    static setDetailsSpotNumber(item, free, total) {
        this.setText(item, '.location-details-park-text', free + '/' + total);
    }

    static setDetailsDirections(item, link) {
        this.setHref(item, '.btn-directions', link);
    }

    // Set location details component address information
    static setDetailsAddress(item, address) {
        var addressHTML = '<span>'+address+'</span>';
        this.setHTML(item, '.location-details-address-text', addressHTML);
    }

    // Set location details component opening information
    static setDetailsOpening(item, opening, closing) {
        var openingHTML = '<span>'+ opening + ' - ' + closing + '</span>';
        this.setHTML(item, '.location-details-clock-text', openingHTML);
    }

    // Set location details component status text
    static setDetailsStatusText(item, status) {
        if(status == true)
            this.setText(item, '.location-details-state-text', "Online");
        else
            this.setText(item, '.location-details-state-text', "Offline");
    }

    // Set location details component status icon
    static setDetailsIcon(item, status) {
        var onlineHTML = '<i class="fa fa-check green"></i>';
        var offlineHTML = '<i class="fa fa-times red"></i>';

        if(status == true)
            this.setHTML(item, '.location-details-state-icon', onlineHTML);
        else
            this.setHTML(item, '.location-details-state-icon', offlineHTML);
    }

    // Set a callback function for the close icon of details item
    static setDetailsCloseAction(item, callback) {
        this.setOnClickEvent(item, '.location-details-close', callback);
    }

    // -----------------------------------------
    // 
    // PRIVATE METHODS
    //
    // ------------------------------------------

    // -----------------------------------------
    // MANAGE GRAPHICAL ASPECT (UTILITY)
    // ------------------------------------------

    // Clear all and append template object to sidebar
    static addSidebarObject(object) {
        if(object != undefined)
            $('.list-sidebar').append($(object).clone(true));
    }

    // Remove current visible objects on sidebar
    static removeSidebarElements() {
        $('.list-sidebar').empty();
    }

    // Find text node in an item and set it
    static setText(item, selector, text) {
        $(item).find(selector).text(text);
    }

    // Find text node in an item and return it
    static getText(item, selector) {
        return $(item).find(selector).text();
    }

    // Find HTML node in an item and set it
    static setHTML(item, selector, html) {
        $(item).find(selector).html(html);
    }

    // Find href attribute in a node in an item and set it
    static setHref(item, selector, content) {
        $(item).find(selector).attr('href', content);
    }

    // Find onclick attribute in a node in an item and set it
    static setOnClick(item, selector, content) {
        $(item).find(selector).attr('onclick', content);
    }

    // Find onclick event in a node in an item and set it
    static setOnClickEvent(item, selector, callback, par) {
        //$(item).find(selector).click({param: par}, content);
        $(item).find(selector).click(function() {
            callback(par);
        });
    }

}