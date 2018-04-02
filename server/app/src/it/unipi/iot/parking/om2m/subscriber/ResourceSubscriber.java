package it.unipi.iot.parking.om2m.subscriber;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;
import org.json.JSONException;
import org.json.JSONObject;

import it.unipi.iot.parking.om2m.OM2MConstants;
import it.unipi.iot.parking.om2m.OM2MException;
import it.unipi.iot.parking.om2m.data.OM2MResource;
import it.unipi.iot.parking.util.FullURIResource;

public abstract class ResourceSubscriber extends CoapResource implements FullURIResource {
    
    private static final Logger LOGGER = Logger.getLogger(ResourceSubscriber.class.getName());
    
    private final FullURIResource parent;
    
    public ResourceSubscriber(SubscriptionServer server, String name) {
        super(name);
        parent = server;
        server.add(this);
    }
    
    public ResourceSubscriber(ResourceSubscriber parentResource, String name) {
        super(name);
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
    protected boolean checkBoolAttribute(JSONObject obj, final String attribute) {
        final String notificationObject;
        final String fullAttribute;
        
        notificationObject = OM2MConstants.getFullAttribute(OM2MConstants.ATTR_NOTIFICATION_OBJECT);
        fullAttribute = OM2MConstants.getFullAttribute(attribute);
        
        obj = obj.getJSONObject(notificationObject);
        
        return obj.optBoolean(fullAttribute);
    }
    
    /**
     * Checks if the given request has the Verification Request flag up.
     * 
     * @param obj
     * @return true if the flag is up.
     */
    protected boolean isVerificationRequest(final JSONObject obj) {
        return checkBoolAttribute(obj, OM2MConstants.ATTR_VERIFICATION_REQUEST);
    }
    
    /**
     * Checks if the given request has the Subscription Deletion flag up.
     * 
     * @param obj
     * @return true if the flag is up.
     */
    protected boolean isSubscriptionDeletion(final JSONObject obj) {
        return checkBoolAttribute(obj, OM2MConstants.ATTR_SUBSCRIPTION_DELETION);
    }
    
    /**
     * Gets the resource in the notification from the notification body.
     * 
     * @param obj
     * @return the resource if present, null otherwise.
     */
    protected OM2MResource getResource(JSONObject obj) {
        final String notificationObject;
        final String notificationEvent;
        final String representation;
        
        notificationObject = OM2MConstants.getFullAttribute(OM2MConstants.ATTR_NOTIFICATION_OBJECT);
        notificationEvent = OM2MConstants.getFullAttribute(OM2MConstants.ATTR_NOTIFICATION_EVENT);
        representation = OM2MConstants.getFullAttribute(OM2MConstants.ATTR_REPRESENTATION);
        
        try {
            obj = obj.getJSONObject(notificationObject)
                     .getJSONObject(notificationEvent)
                     .getJSONObject(representation);
        } catch (JSONException ex) {
            return null;
        }
        
        try {
            return OM2MResource.fromJSONObject(
                    (JSONObject) OM2MResource.fromEnclosedJSONObject(obj));
        } catch (OM2MException e) {
            return null;
        }
    }
    
    /**
     * This method calls the handlePost method. Any class extending this class can
     * handler CoAP requests in that method. The result of a POST is always
     * corresponding to a ResponseCode.CREATED message.
     */
    @Override
    public final void handlePOST(CoapExchange exchange) {
        LOGGER.log(Level.INFO, getName() + " received: `" + exchange.getRequestText() + "`.");
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
        Collection<Resource> children = getChildren();
        for (Resource child : children) {
            if (child instanceof ResourceSubscriber) {
                ((ResourceSubscriber) child).start();
            }
        }
    }
    
    public void stop() {
        Collection<Resource> children = getChildren();
        for (Resource child : children) {
            if (child instanceof ResourceSubscriber) {
                ((ResourceSubscriber) child).stop();
            }
        }
        
        delete();
    }
    
}
