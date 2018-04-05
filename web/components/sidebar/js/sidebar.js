// -----------------------------------------
// SIDEBAR.JS
// Manage sidebar components
// ------------------------------------------

// -----------------------------------------
// 
// SIDEBAR CLASS
//
// ------------------------------------------

class Sidebar {

    // -----------------------------------------
    // 
    // PUBLIC METHODS
    //
    // ------------------------------------------

    constructor() {
        // templates objects
        this.templateEmpty;
        this.templateItems;
        this.templateDetails;

        // list objects (clones)
        this.listEmpty;
        this.listItems = [];
        this.listDetails = [];

        // import templates
        this.importEmptyListTemplate();
        this.importItemTemplate();
        this.importDetailsTemplate();

        // by default append the empty template
        this.appendEmptyList();
        this.sidebarState = STATE.EMPTY;
    }

    // ---------------------------------------------
    // GETTER
    // ---------------------------------------------
    
    // Return the sidebar state (EMPTY, LIST, DETAILS)
    getState() {
        return this.sidebarState;
    }

    // Return true if the sidebar has no element to be displayed
    isListEmpty() {
        return (this.listItems.length == 0) ? true : false;
    }

    // Return the id of an element if it is currently displayed, 0 otherwise
    getDisplayedId() {
        if(this.getState() != STATE.DETAILS)
            return 0;
        else
            return SidebarDrawer.getOnPageDetailsId();
    }

    // Return the index list that correspond to the element id
    idToListIndex(id) {
        for(var i = 0; i < this.listDetails.length; i++)
            if(SidebarDrawer.getDetailsId(this.listDetails[i]) == id)
                return i;
        return -1;
    }

    // ---------------------------------------------
    // EMPTY LIST ITEM
    // ---------------------------------------------

    // Append in clean way the empty list to the page
    appendEmptyList() {
        SidebarDrawer.cleanAddEmptyList(this.listEmpty);
        this.sidebarState = STATE.EMPTY;
    }

    // Reset empty list and build new from scratch
    buildEmptyList() {
        this.copyTemplateToEmptyList();
    }

    // ---------------------------------------------
    // SINGLE LIST ITEM
    // ---------------------------------------------

    // Append in clean way the single item list to the page
    appendItemsList() {
        if(!this.isListEmpty()) {
            SidebarDrawer.cleanAddItemList(this.listItems);
            this.sidebarState = STATE.LIST;
        } else {
            SidebarDrawer.cleanAddEmptyList(this.listEmpty);
            this.sidebarState = STATE.EMPTY;
        }
    }

    // Reset items list and build them with new values
    buildItemsList(parkInfo, openCallback) {
        for(var i = 0; i < parkInfo.length; i++) {
            this.copyTemplateToItemList(i);
            SidebarDrawer.setItemId(this.listItems[i], parkInfo[i].id);
            SidebarDrawer.setItemTitle(this.listItems[i], parkInfo[i].name);
            SidebarDrawer.setItemPrice(this.listItems[i], parkInfo[i].price);
            SidebarDrawer.setItemSpotNumber(this.listItems[i], parkInfo[i].free, parkInfo[i].total);
            SidebarDrawer.setItemStatus(this.listItems[i], parkInfo[i].available);
            SidebarDrawer.setItemOpenAction(this.listItems[i], i, openCallback);
        }
    }

    // ---------------------------------------------
    // DETAILS ITEM
    // ---------------------------------------------

    // Append in clean way the single item list to the page
    appendDetailsList(listIndex) {
        SidebarDrawer.cleanAddSidebarDetails(this.listDetails, listIndex);
        this.sidebarState = STATE.DETAILS;
    }

    // Reset details list and build them with new values
    buildDetailsList(parkInfo, closeCallback) {
        for(var i = 0; i < parkInfo.length; i++) {
            this.copyTemplateToDetailsList(i);
            SidebarDrawer.setDetailsId(this.listDetails[i], parkInfo[i].id);
            SidebarDrawer.setDetailsTitle(this.listDetails[i], parkInfo[i].name);
            SidebarDrawer.setDetailsPrice(this.listDetails[i], parkInfo[i].price);
            SidebarDrawer.setDetailsSpotNumber(this.listDetails[i], parkInfo[i].free, parkInfo[i].total);
            SidebarDrawer.setDetailsDirections(this.listDetails[i], parkInfo[i].link);
            SidebarDrawer.setDetailsAddress(this.listDetails[i], parkInfo[i].address);
            SidebarDrawer.setDetailsOpening(this.listDetails[i], parkInfo[i].openT, parkInfo[i].closeT)
            SidebarDrawer.setDetailsStatusText(this.listDetails[i], parkInfo[i].available);
            SidebarDrawer.setDetailsIcon(this.listDetails[i], parkInfo[i].available);
            SidebarDrawer.setDetailsCloseAction(this.listDetails[i], i, closeCallback);
        }
    }
        
    // -----------------------------------------
    // 
    // PRIVATE ATTRIBUTES AND METHODS
    //
    // ------------------------------------------

    // -----------------------------------------
    // IMPORT TEMPLATES
    // ------------------------------------------

    // Import a template using HTML imports
    importTemplate(templateId) {
        var link, template;

        link = $('link[rel="import"]')[templateId];
        template = link.import.querySelector('template');
        return document.importNode(template.content, true);
    }

    // Import the template of an empty list
    importEmptyListTemplate() {
        this.templateEmpty = this.importTemplate(TMPL_EMPTY);
    }

    // Import the template of each single item
    importItemTemplate() {
        this.templateItems = this.importTemplate(TMPL_ITEMS);
    }

    // Import the template of details view
    importDetailsTemplate() {
        this.templateDetails = this.importTemplate(TMPL_DETAILS);
    }

    // -----------------------------------------
    // COPY ORIGINAL TEMPLATES
    // ------------------------------------------

    // Copy the original template to the specified item
    copyTemplateToEmptyList() {
        this.listEmpty = this.templateEmpty.cloneNode(true);
    }

    // Copy the original template to the specified item
    copyTemplateToItemList(listIndex) {
        this.listItems[listIndex] = this.templateItems.cloneNode(true);
    }

    // Copy the original template to the specified details item
    copyTemplateToDetailsList(listIndex) {
        this.listDetails[listIndex] = this.templateDetails.cloneNode(true);
    }
}

