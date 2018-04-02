package it.unipi.iot.parking.om2m.subscriber;

import java.util.concurrent.TimeoutException;

import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.JSONObject;

import it.unipi.iot.parking.ParksDataHandler;
import it.unipi.iot.parking.om2m.data.OM2MResource;
import it.unipi.iot.parking.om2m.data.RemoteNode;

public class NodeSubscriber extends ResourceSubscriber {
    
    public NodeSubscriber(ResourceSubscriber parentResource, String name) {
        super(parentResource, name);
    }
    
    public NodeSubscriber(SubscriptionServer server, String name) {
        super(server, name);
    }
    
    @Override
    protected void handlePost(CoapExchange exchange) {
        final JSONObject body;
        final OM2MResource resource;
        
        body = new JSONObject(exchange.getRequestText());
        
        if (isVerificationRequest(body)) {
            return;
        }
        
        if (isSubscriptionDeletion(body)) {
            return;
        }
        
        // Not first time and got a new value
        resource = getResource(body);
        
        if (resource == null || !(resource instanceof RemoteNode))
            return;
        
        subscribeToNode((RemoteNode) resource);
    }
    
    public void subscribeToNode(RemoteNode rcse) {
        String resourceID = rcse.getResourceID();
        
        resourceID = resourceID.substring(resourceID.lastIndexOf('/') + 1);
        
        // NOTICE: this gives no error if we create two times the same resource, but the
        // previous one gets substituted with the new one
        // TODO: check if this behavior is bad or not
        CopyResourceSubscriber csr = new CopyResourceSubscriber(this, resourceID,
                rcse.getResourceID());
        
        // TODO: move on the CopyResourceSubscriber, because if the node goes down
        // you'll want to re-subscribe to every node
        try {
            ParksDataHandler.deleteAllSubscriptions(rcse.getCSEID());
        } catch (TimeoutException e) {
            return; // TODO: what to do when this does not reply?
        }
        
        csr.addSubscription(rcse.getCSEID());
    }
    
}
