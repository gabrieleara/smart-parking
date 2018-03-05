package it.unipi.iot.parking.om2m;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.JSONException;
import org.json.JSONObject;

import it.unipi.iot.parking.om2m.data.OM2MResource;
import it.unipi.iot.parking.util.UniqueAssigner;

public abstract class SubscriberResource extends CoapResource implements FullURIResource {
    
    private static final Logger LOGGER = Logger.getLogger(SubscriberResource.class.getName());
    
    private static final UniqueAssigner ASSIGNER = new UniqueAssigner();
    
    private final FullURIResource parent;
    
    public SubscriberResource(SubscriptionServer server) {
        super("default");
        
        setName(getBaseName() + ASSIGNER.assignName());
        
        parent = server;
        
        server.add(this);
    }
    
    public SubscriberResource(SubscriptionServer server, String name) {
        super("default");
        
        ASSIGNER.reserveName(name);
        setName(name);
        
        parent = server;
        
        server.add(this);
    }
    
    public SubscriberResource(SubscriberResource parentResource) {
        super("default");
        
        setName(getBaseName() + ASSIGNER.assignName());
        
        parent = parentResource;
        
        parentResource.add(this);
    }
    
    public SubscriberResource(SubscriberResource parentResource, String name) {
        super("default");
        
        ASSIGNER.reserveName(name);
        setName(name);
        
        parent = parentResource;
        
        parentResource.add(this);
    }
    
    /**
     * Checks if an attribute is present in the specified JSONObject and if it is
     * checks its boolean value.
     * 
     * @param obj
     * @param attribute
     * @return true if the attribute exists and it can be read as a boolean value
     *         true, false otherwise.
     */
    protected boolean checkBoolAttribute(JSONObject obj, String attribute) {
        String fullAttribute = OM2M.fullAttribute(attribute);
        
        return obj.has(fullAttribute) && obj.getBoolean(fullAttribute);
    }
    
    /**
     * Checks if the given request has the Verification Request flag up.
     * 
     * @param obj
     * @return true if the flag is up.
     */
    protected boolean isVerificationRequest(JSONObject obj) {
        String notificationObject = OM2M.fullAttribute(OM2M.ATTR_NOTIFICATION_OBJECT);
        obj = obj.getJSONObject(notificationObject);
        return checkBoolAttribute(obj, OM2M.ATTR_VERIFICATION_REQUEST);
    }
    
    /**
     * Checks if the given request has the Subscription Deletion flag up.
     * 
     * @param obj
     * @return true if the flag is up.
     */
    protected boolean isSubscriptionDeletion(JSONObject obj) {
        String notificationObject = OM2M.fullAttribute(OM2M.ATTR_NOTIFICATION_OBJECT);
        obj = obj.getJSONObject(notificationObject);
        return checkBoolAttribute(obj, OM2M.ATTR_SUBSCRIPTION_DELETION);
    }
    
    /**
     * Override this method to define different base names when implementing a
     * subclass.
     * 
     * @return the base identifier for this class
     */
    protected String getBaseName() {
        return "res-";
    }
    
    private OM2MResource fromJSONObject(JSONObject obj, String fullname) {
        if(obj.has(fullname)) {
            obj = obj.getJSONObject(fullname);
            return OM2MResource.fromJSONObject(obj);
        }
        return null;
    }
    
    /**
     * Gets the resource in the notification from the notification body.
     * 
     * @param obj
     * @return the resource if present, null otherwise.
     */
    protected OM2MResource getResource(JSONObject obj) {
        String notificationObject = OM2M.fullAttribute(OM2M.ATTR_NOTIFICATION_OBJECT);
        String notificationEvent = OM2M.fullAttribute(OM2M.ATTR_NOTIFICATION_EVENT);
        String representation = OM2M.fullAttribute(OM2M.ATTR_REPRESENTATION);
        
        try {
            obj = obj.getJSONObject(notificationObject)
                    .getJSONObject(notificationEvent)
                    .getJSONObject(representation);
        } catch (JSONException ex) {
            return null;
        }
        
        OM2MResource res;
        String fullname;
        
        fullname = OM2M.fullAttribute(OM2M.getResourceString(OM2M.RESOURCE_TYPE_APPLICATION_ENTITY));
        res = fromJSONObject(obj, fullname);
        if(res != null)
            return res;
        
        fullname = OM2M.fullAttribute(OM2M.getResourceString(OM2M.RESOURCE_TYPE_CONTAINER));
        res = fromJSONObject(obj, fullname);
        if(res != null)
            return res;
        
        fullname = OM2M.fullAttribute(OM2M.getResourceString(OM2M.RESOURCE_TYPE_CONTENT_INSTANCE));
        res = fromJSONObject(obj, fullname);
        if(res != null)
            return res;
        
        fullname = OM2M.fullAttribute(OM2M.getResourceString(OM2M.RESOURCE_TYPE_REMOTE_CSE));
        res = fromJSONObject(obj, fullname);
        if(res != null)
            return res;
        
        fullname = OM2M.fullAttribute(OM2M.getResourceString(OM2M.RESOURCE_TYPE_SUBSCRIPTION));
        res = fromJSONObject(obj, fullname);
        if(res != null)
            return res;
        
        return null;
    }
    
    /**
     * This method calls the handlePost method. Any class extending this class can
     * handler CoAP requests in that method. The result of a POST is always
     * corresponding to a ResponseCode.CREATED message.
     */
    @Override
    public final void handlePOST(CoapExchange exchange) {
        LOGGER.log(Level.INFO, getName() + " received" + exchange.getRequestText());
        handlePost(exchange);
        exchange.respond(ResponseCode.CREATED);
    }
    
    @Override
    public final String getFullURI() {
        return parent.getFullURI() + "/" + getName();
    }
    
    /**
     * 
     * @param exchange
     *            the parameter of the handlePOST method
     */
    protected abstract void handlePost(CoapExchange exchange);
    
    public void start() {
    }
    
    public void stop() {
        delete();
    }
    
}
