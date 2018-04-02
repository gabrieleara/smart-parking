package it.unipi.iot.parking.om2m.subscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.JSONObject;

import it.unipi.iot.parking.ParksDataHandler;
import it.unipi.iot.parking.om2m.OM2MConstants;
import it.unipi.iot.parking.om2m.data.OM2MResource;

public class CopyResourceSubscriber extends ResourceSubscriber {
    private final String baseCopyURI;
    
    // TODO:
    // Si assume che non arrivino richieste di cambio di stato eccessivamente rapide
    // da parte delle CI
    public CopyResourceSubscriber(ResourceSubscriber parentResource, String name,
            String baseCopyURI) {
        super(parentResource, name);
        // TODO: shall restore the previous set of subscriptions: NO, SEPARATE METHOD
        
        this.baseCopyURI = baseCopyURI;
    }
    
    public CopyResourceSubscriber(SubscriptionServer server, String name, String baseCopyURI) {
        super(server, name);
        // TODO: shall restore the previous set of subscriptions
        
        this.baseCopyURI = baseCopyURI;
    }
    
    public void addSubscription(String remoteID) {
        new SubscriptionThread(remoteID).start();
    }
    
    /**
     * A new attribute has been added to the monitored remote resource. It shall be
     * added to the local copy too.
     */
    @Override
    protected void handlePost(CoapExchange exchange) {
        JSONObject body = new JSONObject(exchange.getRequestText());
        
        if (isVerificationRequest(body)) {
            // First time, do nothing basically
            System.out.println("Received a Verification Request!");
            return;
        }
        
        if (isSubscriptionDeletion(body)) {
            // TODO: for now do nothing, maybe later interrupt copyCreator thread
            System.out.println("Received a Subscrption Deletion Request!");
            return;
        }
        
        // Not first time and got a new value
        OM2MResource resource = getResource(body);
        
        if (resource == null)
            return;
            
        // TODO: this is a blocking call and it does not ensure the actual order when
        // subscription takes too long
        DuplicatorThread.getInstance()
                        .requestCopy(resource, this);
        
    }
    
    protected void postProcess(OM2MResource resource) {
        // TODO: notify someone that something has changed maybe?
        
        if (OM2MResource.shouldBeSubscribed(resource))
            addSubscription(resource.getResourceID());
    }
    
    private class SubscriptionThread extends Thread {
        String remoteID;
        
        public SubscriptionThread(String remoteID) {
            this.remoteID = remoteID;
        }
        
        @Override
        public void run() {
            /*
             * try { sleep(5000); } catch (InterruptedException e) { // Do nothing }
             */
            
            final String[] children;
            final List<OM2MResource> copyList;
            
            try {
                ParksDataHandler.subscribe(remoteID, getFullURI());
                
                children = ParksDataHandler.getDirectChildrenList(remoteID);
                copyList = new ArrayList<>();
                
                for (String childURI : children) {
                    OM2MResource child;
                    child = ParksDataHandler.get(childURI);
                    
                    if (!OM2MResource.shouldBeCopied(child))
                        continue;
                    
                    if (child.getResourceType() == OM2MConstants.RESOURCE_TYPE_SUBSCRIPTION
                            || child.getResourceType() == OM2MConstants.RESOURCE_TYPE_REMOTE_CSE)
                        continue;
                    
                    copyList.add(child);
                }
                
                DuplicatorThread.getInstance().requestAll(copyList, CopyResourceSubscriber.this);
                
            } catch (TimeoutException e) {
                // TODO: keepalive for nodes
                throw new RuntimeException("The remote node did not reply! Re-subscribe later?", e);
            }
            
        }
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((baseCopyURI == null) ? 0 : baseCopyURI.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CopyResourceSubscriber other = (CopyResourceSubscriber) obj;
        if (baseCopyURI == null) {
            if (other.baseCopyURI != null)
                return false;
        } else if (!baseCopyURI.equals(other.baseCopyURI))
            return false;
        return true;
    }
    
    public String getBaseCopyURI() {
        return this.baseCopyURI;
    }
}
