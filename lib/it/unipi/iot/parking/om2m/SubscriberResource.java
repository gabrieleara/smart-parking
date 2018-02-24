package it.unipi.iot.parking.om2m;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.JSONException;
import org.json.JSONObject;

import it.unipi.iot.parking.util.UniqueAssigner;

public abstract class SubscriberResource extends CoapResource implements FullURIResource {
	
	private static final Logger			LOGGER		= Logger.getLogger(
			SubscriberResource.class.getName());
	
	private static final UniqueAssigner	assigner	= new UniqueAssigner();
	
	private final FullURIResource parent;
	
	/**
	 * Default constructor, assigns a name based on the getBaseName method, followed
	 * by an at least three digit long number provided by an UniqueAssigner.
	 */
	public SubscriberResource(SubscriptionServer server) {
		super("default");
		setName(getBaseName() + assigner.assignName());
		
		parent = server;
		
		server.add(this);
	}
	
	public SubscriberResource(SubscriberResource parentResource) {
		super("default");
		setName(getBaseName() + assigner.assignName());
		
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
	
	/**
	 * Gets the resource in the notification from the notification body.
	 * 
	 * @param obj
	 * @return the resource if present, null otherwise.
	 */
	protected JSONObject getResource(JSONObject obj, int trackedResourceType) {
		String notificationObject = OM2M.fullAttribute(OM2M.ATTR_NOTIFICATION_OBJECT);
		String notificationEvent = OM2M.fullAttribute(OM2M.ATTR_NOTIFICATION_EVENT);
		String representation = OM2M.fullAttribute(OM2M.ATTR_REPRESENTATION);
		String resourceType = OM2M.fullAttribute(OM2M.getResourceString(trackedResourceType));
		
		JSONObject resource = null;
		
		try {
			resource = obj	.getJSONObject(notificationObject)
							.getJSONObject(notificationEvent)
							.getJSONObject(representation)
							.getJSONObject(resourceType);
		} catch (JSONException ex) {
			// Do nothing, return null for invalid update object
		}
		
		return resource;
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
	
}
