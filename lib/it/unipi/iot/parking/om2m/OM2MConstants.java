package it.unipi.iot.parking.om2m;

public class OM2MConstants {
    
    static final int OPTION_CREDENTIALS   = 256;
    static final int OPTION_RESPONSE_CODE = 265;
    static final int OPTION_RESOURCE_TYPE = 267;
    static final int RESPONSE_OK          = 2000;
    static final int RESPONSE_CREATED     = 2001;
    static final int RESPONSE_ACCEPTED    = 2002;
    static final int RESPONSE_NO_CONTENT  = 2004;
    
    public static final int RESOURCE_TYPE_APPLICATION_ENTITY = 2;
    public static final int RESOURCE_TYPE_CONTAINER          = 3;
    public static final int RESOURCE_TYPE_CONTENT_INSTANCE   = 4;
    public static final int RESOURCE_TYPE_CSE_BASE           = 5;
    public static final int RESOURCE_TYPE_REMOTE_CSE         = 16;
    public static final int RESOURCE_TYPE_SUBSCRIPTION       = 23;
    public static final int RESOURCE_TYPE_URIL               = 999;
    
    private static final String RESOURCE_TYPE_STR_AE   = "ae";
    private static final String RESOURCE_TYPE_STR_CNT  = "cnt";
    private static final String RESOURCE_TYPE_STR_CIN  = "cin";
    private static final String RESOURCE_TYPE_STR_CB   = "cb";
    private static final String RESOURCE_TYPE_STR_CSR  = "csr";
    private static final String RESOURCE_TYPE_STR_SUB  = "sub";
    private static final String RESOURCE_TYPE_STR_URIL = "uril";
    
    public static final String ATTR_RESOURCE_TYPE             = "ty";
    public static final String ATTR_RESOURCE_ID               = "ri";
    public static final String ATTR_RESOURCE_NAME             = "rn";
    public static final String ATTR_PARENT_ID                 = "pi";
    public static final String ATTR_CREATION_TIME             = "ct";
    public static final String ATTR_LAST_MOD_TIME             = "lt";
    public static final String ATTR_OLDEST                    = "ol";
    public static final String ATTR_LATEST                    = "la";
    public static final String ATTR_STATE_TAG                 = "st";
    public static final String ATTR_APPLICATION_ID            = "api";
    public static final String ATTR_CONTENT                   = "con";
    public static final String ATTR_CONTENT_INFO              = "cnf";
    public static final String ATTR_CONTENT_SIZE              = "cs";
    public static final String ATTR_REQUEST_REACHABILITY      = "rr";
    public static final String ATTR_CSE_ID                    = "csi";
    public static final String ATTR_NOTIFICATION_URI          = "nu";
    public static final String ATTR_NOTIFICATION_CONTENT_TYPE = "nct";
    public static final String ATTR_LABELS                    = "lbl";
    public static final String ATTR_VERIFICATION_REQUEST      = "vrq";
    public static final String ATTR_SUBSCRIPTION_REFERENCE    = "sur";
    public static final String ATTR_SUBSCRIPTION_DELETION     = "sud";
    public static final String ATTR_NOTIFICATION_OBJECT       = "sgn";
    public static final String ATTR_NOTIFICATION_EVENT        = "nev";
    public static final String ATTR_REPRESENTATION            = "rep";
    
    static final int NCT_WHOLE_RESOURCE      = 1;
    static final int NCT_MODIFIED_ATTRIBUTES = 2;
    static final int NCT_REFERENCE_ONLY      = 3;
    
    static final boolean ATTR_REQUEST_REACHABILITY_VALUE = true;
    static final String  ATTR_CONTENT_INFO_VALUE         = "message";
    static final int     ATTR_NCT_VALUE                  = NCT_MODIFIED_ATTRIBUTES;
    static final String  ATTR_RESOURCE_NAME_SUBSCRIBER   = "subscriber";
    
    private OM2MConstants() {
    }
    
    public static String getResourceString(int resourceType) {
        switch (resourceType) {
        case RESOURCE_TYPE_APPLICATION_ENTITY:
            return RESOURCE_TYPE_STR_AE;
        case RESOURCE_TYPE_CONTAINER:
            return RESOURCE_TYPE_STR_CNT;
        case RESOURCE_TYPE_CONTENT_INSTANCE:
            return RESOURCE_TYPE_STR_CIN;
        case RESOURCE_TYPE_URIL:
            return RESOURCE_TYPE_STR_URIL;
        case RESOURCE_TYPE_SUBSCRIPTION:
            return RESOURCE_TYPE_STR_SUB;
        case RESOURCE_TYPE_CSE_BASE:
            return RESOURCE_TYPE_STR_CB;
        case RESOURCE_TYPE_REMOTE_CSE:
            return RESOURCE_TYPE_STR_CSR;
        default:
            throw new IllegalArgumentException("Unsupported resource type " + resourceType);
        }
    }
    
    public static String getFullResourceString(int resourceType) {
        return "m2m:" + getResourceString(resourceType);
    }
    
    public static String getFullAttribute(String attribute) {
        return "m2m:" + attribute;
    }
    
}
