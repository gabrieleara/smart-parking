package it.unipi.iot.parking.om2m.subscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.JSONObject;

import it.unipi.iot.parking.ParksDataHandler;
import it.unipi.iot.parking.om2m.OM2MConstants;
import it.unipi.iot.parking.om2m.OM2MException;
import it.unipi.iot.parking.om2m.data.OM2MResource;
import it.unipi.iot.parking.util.ConcurrentOM2MObservable;
import it.unipi.iot.parking.util.OM2MObservable;

public class CopyResourceSubscriber extends ResourceSubscriber implements OM2MObservable {
    private final String baseCopyURI;
    private final ConcurrentOM2MObservable observable = new ConcurrentOM2MObservable();
    
    public CopyResourceSubscriber(ResourceSubscriber parentResource, String name,
            String baseCopyURI) {
        super(parentResource, name);
        this.baseCopyURI = baseCopyURI;
    }
    
    public CopyResourceSubscriber(SubscriptionServer server, String name, String baseCopyURI) {
        super(server, name);
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
            // Do nothing, it's not that we can do something about it
            System.out.println("Received a Subscrption Deletion Request!");
            return;
        }
        
        // Not first time and got a new value
        OM2MResource resource = getResource(body);
        
        if (resource == null)
            return;
        
        DuplicatorThread.getInstance()
                        .requestCopy(resource, this);
        
    }
    
    protected void postProcess(OM2MResource resource, OM2MResource copy) {
        observable.notifyObservers(copy);
        
        postProcess(resource);
    }
    
    protected void postProcess(OM2MResource resource) {
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
            final String[] children;
            final List<OM2MResource> copyList;
            
            try {
                try {
                    ParksDataHandler.subscribe(remoteID, getFullURI());
                } catch(OM2MException e) {
                    System.out.println("There has been an error with the subscriptions, but it should all be fine: " + e.getMessage());
                    return;
                }
                
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
                
                DuplicatorThread.getInstance()
                                .requestAll(copyList, CopyResourceSubscriber.this);
                
            } catch (TimeoutException e) {
                // TODO: implement a keep-alive for nodes
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

    @Override
    public void registerObserver(Observer observer) {
        observable.registerObserver(observer);
    }

    @Override
    public boolean unregisterObserver(Observer observer) {
        return observable.unregisterObserver(observer);
    }
}
