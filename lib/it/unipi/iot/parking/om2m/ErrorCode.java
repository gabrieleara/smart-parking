package it.unipi.iot.parking.om2m;

public enum ErrorCode {
    OTHER(""),
    MISSING_RESPONSE_OPT("Missing RESPONSE option in reply."),
    NOT_FOUND("4.04 Requested object was not found!"),
    INTERNAL_SERVER_ERROR("5.00 Internal server error!"),
    NAME_ALREADY_PRESENT("4.15 Name already present in parent!");
    
    public static final ErrorCode fromWebCode(int webCode) {
        switch (webCode) {
        case -1:
            return OTHER;
        case 4004:
            return NOT_FOUND;
        case 4105:
            return NAME_ALREADY_PRESENT;
        case 5000:
            return INTERNAL_SERVER_ERROR;
        case 0:
            return MISSING_RESPONSE_OPT;
        default:
            return OTHER;//throw new RuntimeException("Missing errorcode for the following webcode: " + webCode);
        }
        
    }
    
    private String text;
    
    private ErrorCode(String text) {
        this.text = text;
    }
    
    @Override
    public String toString() {
        return this.text;
    }
    
}
