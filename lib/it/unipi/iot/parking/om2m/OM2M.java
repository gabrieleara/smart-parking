package it.unipi.iot.parking.om2m;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Request;
import org.json.JSONArray;
import org.json.JSONObject;

import it.unipi.iot.parking.om2m.data.ApplicationEntity;
import it.unipi.iot.parking.om2m.data.Container;
import it.unipi.iot.parking.om2m.data.ContentInstance;
import it.unipi.iot.parking.om2m.data.OM2MResource;
import it.unipi.iot.parking.om2m.data.Subscription;

public class OM2M {
    
    private static final Logger LOGGER = Logger.getLogger(OM2M.class.getSimpleName());
    
    private static final int OPTION_CREDENTIALS   = 256;
    private static final int OPTION_RESPONSE_CODE = 265;
    private static final int OPTION_RESOURCE_TYPE = 267;
    
    private static final int RESPONSE_CREATED = 2001;
    private static final int RESPONSE_CONTENT = 2000;
    
    public static final int RESOURCE_TYPE_APPLICATION_ENTITY = 2;
    public static final int RESOURCE_TYPE_CONTAINER          = 3;
    public static final int RESOURCE_TYPE_CONTENT_INSTANCE   = 4;
    public static final int RESOURCE_TYPE_CSE_BASE           = 5;
    public static final int RESOURCE_TYPE_REMOTE_CSE         = 16;
    public static final int RESOURCE_TYPE_SUBSCRIPTION       = 23;
    public static final int RESOURCE_TYPE_URIL               = 999;
    
    private static final String RESOURCE_TYPE_STR_AE           = "ae";
    private static final String RESOURCE_TYPE_STR_CNT          = "cnt";
    private static final String RESOURCE_TYPE_STR_CIN          = "cin";
    private static final String RESOURCE_TYPE_STR_CB           = "cb";
    private static final String RESOURCE_TYPE_STR_CSR          = "csr";
    private static final String RESOURCE_TYPE_STR_SUB          = "sub";
    private static final String RESOURCE_TYPE_STR_URIL         = "uril";
    public static final String  ATTR_RESOURCE_TYPE             = "ty";
    public static final String  ATTR_RESOURCE_ID               = "ri";
    public static final String  ATTR_RESOURCE_NAME             = "rn";
    public static final String  ATTR_PARENT_ID                 = "pi";
    public static final String  ATTR_CREATION_TIME             = "ct";
    public static final String  ATTR_LAST_MOD_TIME             = "lt";
    public static final String  ATTR_OLDEST                    = "ol";
    public static final String  ATTR_LATEST                    = "la";
    public static final String  ATTR_STATE_TAG                 = "st";
    public static final String  ATTR_APPLICATION_ID            = "api";
    public static final String  ATTR_CONTENT                   = "con";
    public static final String  ATTR_CONTENT_INFO              = "cnf";
    public static final String  ATTR_CONTENT_SIZE              = "cs";
    public static final String  ATTR_REQUEST_REACHABILITY      = "rr";
    public static final String  ATTR_CSE_ID                    = "csi";
    public static final String  ATTR_NOTIFICATION_URI          = "nu";
    public static final String  ATTR_NOTIFICATION_CONTENT_TYPE = "nct";
    public static final String  ATTR_LABELS                    = "lbl";
    public static final String  ATTR_VERIFICATION_REQUEST      = "vrq";
    public static final String  ATTR_SUBSCRIPTION_REFERENCE    = "sur";
    public static final String  ATTR_SUBSCRIPTION_DELETION     = "sud";
    public static final String  ATTR_NOTIFICATION_OBJECT       = "sgn";
    public static final String  ATTR_NOTIFICATION_EVENT        = "nev";
    public static final String  ATTR_REPRESENTATION            = "rep";
    
    // private static final int NCT_WHOLE_RESOURCE = 1;
    private static final int NCT_MODIFIED_ATTRIBUTES = 2;
    // private static final int NCT_REFERENCE_ONLY = 3;
    
    private static final boolean ATTR_REQUEST_REACHABILITY_CONTENT = true;
    private static final String  ATTR_CONTENT_INFO_CONTENT         = "message";
    private static final int     ATTR_NCT_CONTENT                  = NCT_MODIFIED_ATTRIBUTES;
    
    private static final String URI_PROTOCOL = "coap";
    
    private final String baseURI;
    private final String credentials;
    
    private static OM2M instance = null;
    
    public static OM2M init(String host, String portNumber, String credentials) {
        if (instance != null)
            throw new IllegalStateException("The init method can be called only once!");
        
        instance = new OM2M(host, portNumber, credentials);
        
        return instance;
    }
    
    public static String fullAttribute(String attribute) {
        return "m2m:" + attribute;
    }
    
    /**
     * 
     * @return null if {@link #init(String, String, String)} has not been called.
     *         The singleton instance otherwise.
     */
    public static OM2M getInstance() {
        return instance;
    }
    
    public ApplicationEntity createApplicationEntity(String parentID, String appID, String name) {
        JSONObject object = new JSONObject();
        
        object.put(ATTR_APPLICATION_ID, appID)
              .put(ATTR_RESOURCE_NAME, name)
              .put(ATTR_REQUEST_REACHABILITY, ATTR_REQUEST_REACHABILITY_CONTENT);
        
        JSONObject body = new JSONObject();
        body.put("m2m:" + RESOURCE_TYPE_STR_AE, object);
        
        CoapResponse response = performPost(parentID, body, RESOURCE_TYPE_APPLICATION_ENTITY);
        
        object = (JSONObject) getObject(response, RESOURCE_TYPE_APPLICATION_ENTITY,
                RESPONSE_CREATED);
        
        return new ApplicationEntity(object);
    }
    
    public OM2MResource create(String parentID, int resourceType, String... options) {
        switch (resourceType) {
        case RESOURCE_TYPE_APPLICATION_ENTITY:
            if (options.length < 2)
                throw new IllegalArgumentException(
                        "To create an Application Entity two options are required!");
            return createApplicationEntity(parentID, options[0], options[1]);
        case RESOURCE_TYPE_CONTAINER:
            return createContainer(parentID, options[0]);
        case RESOURCE_TYPE_CONTENT_INSTANCE:
            if (options.length > 1)
                return createContentInstance(parentID, options[0],
                        Arrays.copyOfRange(options, 1, options.length));
            else
                return createContentInstance(parentID, options[0]);
        case RESOURCE_TYPE_SUBSCRIPTION:
            return createSubscription(parentID, options[0]);
        default:
            throw new IllegalArgumentException("Resource Type not supported!");
        }
    }
    
    public Container createContainer(String parentID, String name) {
        JSONObject object = new JSONObject();
        
        object.put(ATTR_RESOURCE_NAME, name);
        
        JSONObject body = new JSONObject();
        body.put("m2m:" + RESOURCE_TYPE_STR_CNT, object);
        
        CoapResponse response = performPost(parentID, body, RESOURCE_TYPE_CONTAINER);
        
        object = (JSONObject) getObject(response, RESOURCE_TYPE_CONTAINER, RESPONSE_CREATED);
        
        return new Container(object);
    }
    
    /*
     * public ContentInstance createContentInstance(String parentID, String value) {
     * return createContentInstance(parentID, value); }
     */
    
    public ContentInstance createContentInstance(String parentID, String value, String... labels) {
        JSONObject object = new JSONObject();
        
        JSONArray arr = new JSONArray(labels);
        
        System.out.println(arr);
        /*
         * for(String label : labels) {
         * 
         * }
         */
        object.put(ATTR_CONTENT, value)
              .put(ATTR_CONTENT_INFO, ATTR_CONTENT_INFO_CONTENT)
              .put(ATTR_LABELS, arr);
        
        JSONObject body = new JSONObject();
        body.put("m2m:" + RESOURCE_TYPE_STR_CIN, object);
        
        CoapResponse response = performPost(parentID, body, RESOURCE_TYPE_CONTENT_INSTANCE);
        
        object = (JSONObject) getObject(response, RESOURCE_TYPE_CONTENT_INSTANCE, RESPONSE_CREATED);
        
        return new ContentInstance(object);
    }
    
    public Subscription createSubscription(String parentID, String subscriberURI) {
        return createSubscription(parentID, subscriberURI, new String[0]);
    }
    
    public Subscription createSubscription(String parentID, String subscriberURI, String[] labels) {
        JSONObject object = new JSONObject();
        
        object.put(ATTR_RESOURCE_NAME, "subscriber")
              .put(ATTR_NOTIFICATION_URI, subscriberURI)
              .put(ATTR_NOTIFICATION_CONTENT_TYPE, ATTR_NCT_CONTENT)
              .put(ATTR_LABELS, labels);
        
        JSONObject body = new JSONObject();
        body.put("m2m:" + getResourceString(RESOURCE_TYPE_SUBSCRIPTION), object);
        
        CoapResponse response = performPost(parentID, body, RESOURCE_TYPE_SUBSCRIPTION);
        
        object = (JSONObject) getObject(response, RESOURCE_TYPE_SUBSCRIPTION, RESPONSE_CREATED);
        
        return new Subscription(object);
    }
    
    // TODO: not working
    public String[] getAllFirstChildrenIDOfType(String parentID, int resourceType) {
        return getAllChildrenIDOfType(parentID, resourceType, new String[] { "pi=" + parentID });
    }
    
    public String[] getAllChildrenIDOfType(String parentID, int resourceType) {
        return getAllChildrenIDOfType(parentID, resourceType, new String[0]);
    }
    
    // TODO: remove public after tests are done
    public String[] getAllChildrenIDOfType(String parentID, int resourceType, String[] filters) {
        String[] uriQueries = { "fu=1", "rty=" + resourceType };
        
        uriQueries = concatArrays(uriQueries, filters);
        
        CoapResponse response = performGet(parentID, uriQueries);
        
        JSONArray array = (JSONArray) getObject(response, RESOURCE_TYPE_URIL, RESPONSE_CONTENT);
        
        List<String> list = new ArrayList<String>();
        
        for (Object o : array) {
            list.add(o.toString());
        }
        
        return list.toArray(new String[list.size()]);
    }
    
    public JSONObject getResourceObjectFromID(String resourceID, int resourceType) {
        CoapResponse response = performGet(concatURIs(resourceID));
        return (JSONObject) getObject(response, resourceType, RESPONSE_CONTENT);
        
    }
    
    public OM2MResource getResourceFromID(String resourceID, int resourceType) {
        JSONObject obj = getResourceObjectFromID(resourceID, resourceType);
        
        return OM2MResource.fromJSONObject(obj);
        
    }
    
    // TODO: remove after tests are done!
    public String testQuery(String resourceID, String[] uriQueries) {
        return performGet(resourceID, uriQueries).getResponseText();
    }
    
    // ------------------------------------------------------------------------------------------
    // -------------------------------- PRIVATE AREA
    // ------------------------------------------------------------------------------------------
    
    private OM2M(String host, String portNumber, String credentials) {
        this.baseURI = URI_PROTOCOL + "://" + host + ":" + portNumber + "/~";
        this.credentials = credentials;
    }
    
    private Object getObject(CoapResponse response, int resourceType, int successCode) {
        String responseBody = response.getResponseText();
        
        List<Option> list = response.getOptions()
                                    .asSortedList();
        
        Predicate<Option> predicate = opt -> opt.getNumber() == OPTION_RESPONSE_CODE;
        Option responseOption = list.stream()
                                    .filter(predicate)
                                    .findFirst()
                                    .orElse(null);
        
        if (responseOption == null) {
            String message = "Option RESPONSE couldn't be found! Response body was: "
                    + responseBody;
            throw new RuntimeException(message);
        }
        
        if (responseOption.getIntegerValue() != successCode) {
            String message = "Option RESPONSE status code was " + responseOption.getIntegerValue()
                    + ".\nResponse body was: `" + responseBody + "`";
            throw new RuntimeException(message);
        }
        
        LOGGER.log(Level.INFO, "Request has been accepted!");
        
        return new JSONObject(responseBody).get("m2m:" + getResourceString(resourceType));
    }
    
    private String concatURIs(String... strings) {
        StringBuffer buffer = new StringBuffer();
        int oldSize;
        
        for (String s : strings) {
            if (s == null || s.length() < 1)
                continue;
            
            oldSize = buffer.length();
            
            buffer.append(s);
            
            while (oldSize < buffer.length() && buffer.charAt(oldSize) == '/')
                buffer.deleteCharAt(oldSize);
            
            buffer.append('/');
        }
        
        while (buffer.charAt(buffer.length() - 1) == '/')
            buffer.deleteCharAt(buffer.length() - 1);
        
        return buffer.toString();
    }
    
    private URI getURIFromID(String resourceID) {
        String containerLocation = concatURIs(baseURI, resourceID);
        URI uri = null;
        
        try {
            uri = new URI(containerLocation);
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
        
        return uri;
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
    
    /**
     * @return null if timed out - CoapResponse otherwise
     */
    private CoapResponse performRequest(Request request) {
        CoapClient client = new CoapClient();
        
        return client.advanced(request);
    }
    
    /**
     * @return null if timed out - CoapResponse otherwise
     */
    private CoapResponse performPost(String parentID, JSONObject body, int resourceType) {
        Request request = Request.newPost();
        
        request.setURI(getURIFromID(parentID));
        
        OptionSet options = request.getOptions();
        
        options.addOption(new Option(OPTION_RESOURCE_TYPE, resourceType))
               .addOption(new Option(OPTION_CREDENTIALS, credentials))
               .setContentFormat(MediaTypeRegistry.APPLICATION_JSON)
               .setAccept(MediaTypeRegistry.APPLICATION_JSON);
        
        request.setPayload(body.toString());
        
        LOGGER.log(Level.INFO, "Sending following payload: `" + body + "`");
        LOGGER.log(Level.INFO, "Sending POST request to URI `" + request.getURI() + "`");
        
        return performRequest(request);
    }
    
    private CoapResponse performGet(String resourceID) {
        return performGet(resourceID, new String[0]);
    }
    
    private CoapResponse performGet(String resourceID, String[] uriQueries) {
        Request request = Request.newGet();
        
        request.setURI(getURIFromID(resourceID));
        
        OptionSet options = request.getOptions();
        
        options.addOption(new Option(OPTION_CREDENTIALS, credentials))
               .setContentFormat(MediaTypeRegistry.APPLICATION_JSON)
               .setAccept(MediaTypeRegistry.APPLICATION_JSON);
        
        for (String s : uriQueries)
            options.addUriQuery(s);
        
        LOGGER.log(Level.INFO, "Sending GET request to URI `" + request.getURI() + "`");
        
        return performRequest(request);
    }
    
    // This method probably leads to errors!
    /*
     * private JSONObject[] getAllChildrenOfType(String parentID, int resourceType,
     * boolean last) { return getAllChildrenOfType(parentID, resourceType, last, new
     * String[0]); }
     */
    /*
     * private JSONObject[] getAllChildrenOfType(String parentID, int resourceType)
     * { return getAllChildrenOfType(parentID, resourceType, false, new String[0]);
     * }
     */
    
    private String[] concatArrays(String[] first, String[] second) {
        return Stream.concat(Arrays.stream(first), Arrays.stream(second))
                     .toArray(String[]::new);
    }
    /*
     * private JSONObject[] getAllChildrenOfType(String parentID, int resourceType,
     * boolean last, String[] filters) { String[] uril =
     * getAllChildrenIDOfType(parentID, resourceType, filters);
     * 
     * CoapResponse response;
     * 
     * List<JSONObject> values = new ArrayList<>();
     * 
     * // TODO: if "la" is put, resourceType changes! for (String uri : uril) { if
     * (last) uri = concatURIs(uri, "la");
     * 
     * response = performGet(uri, new String[0]);
     * 
     * values.add((JSONObject) getObject(response, resourceType, RESPONSE_CONTENT));
     * }
     * 
     * return values.toArray(new JSONObject[values.size()]); }
     */
    /*
     * public ApplicationEntity[] getAllAE(String resourceID) { JSONObject[] objs =
     * getAllChildrenOfType(resourceID, RESOURCE_TYPE_APPLICATION_ENTITY);
     * 
     * List<RemoteCSE> list = new ArrayList<>();
     * 
     * for (JSONObject obj : objs) { list.add(new RemoteCSE(obj)); }
     * 
     * return list.toArray(new ApplicationEntity[list.size()]); }
     * 
     * public Container[] getAllContainers(String resourceID) { JSONObject[] objs =
     * getAllChildrenOfType(resourceID, RESOURCE_TYPE_CONTAINER);
     * 
     * List<RemoteCSE> list = new ArrayList<>();
     * 
     * for (JSONObject obj : objs) { list.add(new RemoteCSE(obj)); }
     * 
     * return list.toArray(new Container[list.size()]); }
     */
    
    public OM2MResource create(String parentID, OM2MResource original) {
        return create(parentID, original.getResourceType(), original.getCopyOptions());
    }
    
    /*
     * public RemoteCSE[] getAllCSEBase(String resourceID) { JSONObject[] objs =
     * getAllChildrenOfType(resourceID, RESOURCE_TYPE_CSE_BASE);
     * 
     * List<RemoteCSE> list = new ArrayList<>();
     * 
     * for (JSONObject obj : objs) { list.add(new RemoteCSE(obj)); }
     * 
     * return list.toArray(new RemoteCSE[list.size()]); }
     */
    
    /*
     * public ContentInstance getContainerValue(String resourceID) { CoapResponse
     * response = performGet(concatURIs(resourceID, "la"));
     * 
     * JSONObject object = (JSONObject) getObject(response,
     * RESOURCE_TYPE_CONTENT_INSTANCE, RESPONSE_CONTENT);
     * 
     * return new ContentInstance(object); }
     */
    
    // TODO: this method is almost certainly WRONG!
    /*
     * private ContentInstance[] getAllLastValues(String resourceID) { JSONObject[]
     * objs = getAllChildrenOfType(resourceID, RESOURCE_TYPE_CONTENT_INSTANCE,
     * true);
     * 
     * List<ContentInstance> list = new ArrayList<>();
     * 
     * for (JSONObject obj : objs) { list.add(new ContentInstance(obj)); }
     * 
     * return list.toArray(new ContentInstance[list.size()]); }
     */
    /*
     * // This is ok
     * 
     * public ContentInstance[] getAllValues(String resourceID) { JSONObject[] objs
     * = getAllChildrenOfType(resourceID, RESOURCE_TYPE_CONTENT_INSTANCE);
     * 
     * List<ContentInstance> list = new ArrayList<>();
     * 
     * for (JSONObject obj : objs) { list.add(new ContentInstance(obj)); }
     * 
     * return list.toArray(new ContentInstance[list.size()]); }
     * 
     * public ContentInstance[] getAllLastValuesGreater(String resourceID, int
     * stateTag) { JSONObject[] objs = getAllChildrenOfType(resourceID,
     * RESOURCE_TYPE_CONTENT_INSTANCE);
     * 
     * List<ContentInstance> list = new ArrayList<>();
     * 
     * for (JSONObject obj : objs) { list.add(new ContentInstance(obj)); }
     * 
     * return list.toArray(new ContentInstance[list.size()]); }
     * 
     */
    
}
