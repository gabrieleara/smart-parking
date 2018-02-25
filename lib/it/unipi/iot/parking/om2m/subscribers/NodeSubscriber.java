package it.unipi.iot.parking.om2m.subscribers;

import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.JSONObject;

import it.unipi.iot.parking.om2m.OM2M;
import it.unipi.iot.parking.om2m.SubscriberResource;
import it.unipi.iot.parking.om2m.SubscriptionServer;
import it.unipi.iot.parking.om2m.data.RemoteCSE;

/**
 * This class tracks changes inside a Node, either a InfrastructureNode or a
 * MiddleNode and copies Application entities in it in the local remote CSE
 * reference.
 * 
 * @author Gabriele Ara
 *
 */
public class NodeSubscriber extends SubscriberResource {
	
	public NodeSubscriber(SubscriptionServer parentResource) {
		super(parentResource);
	}
	
	@Override
	protected void handlePost(CoapExchange exchange) {
		JSONObject body = new JSONObject(exchange.getRequestText());
		
		if (isVerificationRequest(body)) {
			// First time, do nothing basically, unless we want to start a new CopyCreator
			// or something
			return;
		}
		
		if (isSubscriptionDeletion(body)) {
			// TODO: for now do nothing, maybe later interrupt copyCreator thread
			return;
		}
		
		// Not first time and got a new value
		
		JSONObject resource = getResource(body, OM2M.RESOURCE_TYPE_REMOTE_CSE);
		
		if (resource == null)
			return;
		
		RemoteCSE rcse = new RemoteCSE(resource);
		
		RemoteCSESubscriber sub = new RemoteCSESubscriber(this, rcse.getCSEID(),
				rcse.getResourceID());
	}
}
