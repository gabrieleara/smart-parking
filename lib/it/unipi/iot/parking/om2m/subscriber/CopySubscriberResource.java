package it.unipi.iot.parking.om2m.subscriber;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;

import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;
import org.json.JSONObject;

import it.unipi.iot.parking.om2m.OM2M;
import it.unipi.iot.parking.om2m.SubscriberResource;
import it.unipi.iot.parking.om2m.SubscriptionServer;
import it.unipi.iot.parking.om2m.data.OM2MResource;

public class CopySubscriberResource extends SubscriberResource {
    
    private final CopyCreator copyCreator = new CopyCreator();
    private final OM2M        conn        = OM2M.getInstance();
    
    private final Set<SubscriptionRelation> subscriptions = new HashSet<>();
    
    private final SortedSet<OM2MResource> copyset = new TreeSet<>();
    
    public CopySubscriberResource(SubscriberResource parentResource, String name) {
        super(parentResource, name);
        // TODO: shall restore the previous set of subscriptions
        
        copyCreator.start();
    }
    
    public CopySubscriberResource(SubscriberResource parentResource) {
        super(parentResource);
        copyCreator.start();
    }
    
    public CopySubscriberResource(SubscriptionServer server, String name) {
        super(server, name);
        // TODO: shall restore the previous set of subscriptions
        
        copyCreator.start();
    }
    
    public CopySubscriberResource(SubscriptionServer server) {
        super(server);
        copyCreator.start();
    }
    
    public boolean addNewSubscription(SubscriptionRelation sr) {
        return addSubscription(sr, true);
    }
    
    public boolean addOldSubscription(SubscriptionRelation sr) {
        return addSubscription(sr, false);
    }
    
    // TODO: synchronized?
    private boolean addSubscription(SubscriptionRelation sr, boolean shallSubscribe) {
        if (!subscriptions.add(sr)) {
            return false;
        }
        
        new SubscriptionThread(sr, shallSubscribe).start();
        
        return true;
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
            return;
        }
        
        if (isSubscriptionDeletion(body)) {
            // TODO: for now do nothing, maybe later interrupt copyCreator thread
            return;
        }
        
        // Not first time and got a new value
        OM2MResource resource = getResource(body);
        
        if (resource == null)
            return;
        
        SubscriptionRelation subscription = getAssociatedSubscription(resource);
        
        System.out.println("Invalidator!");
        if (subscription == null)
            return;
        
        addResourceToCopy(subscription, resource);
        
        /*
         * // TODO: Blocking call, even if not for too long "usually"
         * relation.acceptNotification();
         * 
         * if (relation.getLastCreationTimeCopied()) {
         * 
         * }
         * 
         * OM2MResource copy = fromJSONObject(resource);
         * 
         * addCopy(copy);
         */
    }
    
    private SubscriptionRelation getAssociatedSubscription(OM2MResource res) {
        String parentID = res.getParentID();
        int resourceType = res.getResourceType();
        
        SubscriptionRelation mockup = new SubscriptionRelation(parentID, resourceType, "");
        
        if (!subscriptions.contains(mockup))
            return null;
        
        Predicate<SubscriptionRelation> predicate = subR -> subR.equals(mockup);
        
        SubscriptionRelation subscription = subscriptions.stream()
                                                         .filter(predicate)
                                                         .findFirst()
                                                         .orElse(null);
        return subscription;
    }
    
    private void addResourceToCopy(SubscriptionRelation relation, OM2MResource resource) {
        if (relation.acceptNotification(resource.getCreationTime()))
            addResourceToCopy(resource);
    }
    
    private synchronized void addAllResourceToCopy(SubscriptionRelation relation,
            SortedSet<OM2MResource> resources) {
        if (resources.isEmpty()) {
            relation.accept();
            return;
        }
        
        OM2MResource[] content = resources.toArray(new OM2MResource[resources.size()]);
        String[] names = new String[content.length];
        
        for(int i = 0; i < names.length; ++i) {
            names[i] = content[i].getResourceName();
        }
        
        System.out.println("I added " + Arrays.toString(names) + " to the set.");
        
        copyset.addAll(resources);
        
        
        content = copyset.toArray(new OM2MResource[copyset.size()]);
        names = new String[content.length];
        
        for(int i = 0; i < names.length; ++i) {
            names[i] = content[i].getResourceName();
        }
        
        System.out.println("The set now contains: " + Arrays.toString(names));
        
        relation.setLastCreationTimeCopied(resources.last()
                .getCreationTime());
        
        
        
        notify();
    }
    
    private synchronized void addResourceToCopy(OM2MResource resource) {
        copyset.add(resource);
        
        OM2MResource[] content = copyset.toArray(new OM2MResource[copyset.size()]);
        String[] names = new String[content.length];
        
        for(int i = 0; i < names.length; ++i) {
            names[i] = content[i].getResourceName();
        }
        
        System.out.println("I added " + resource.getResourceName() + " to the set.");
        System.out.println("The set now contains: " + Arrays.toString(names));
        
        
        notify();
    }
    
    private synchronized OM2MResource extractResourceToCopy() throws InterruptedException {
        while (copyset.isEmpty()) {
            this.wait();
        }
        
        OM2MResource res = copyset.first();
        copyset.remove(res);
        
        OM2MResource[] remaining = copyset.toArray(new OM2MResource[copyset.size()]);
        String[] names = new String[remaining.length];
        
        for(int i = 0; i < names.length; ++i) {
            names[i] = remaining[i].getResourceName();
        }
        
        System.out.println("I removed " + res.getResourceName() + " from the set.");
        System.out.println("Remaining elements are: " + Arrays.toString(names));
        
        return res;
    }
    
    @Override
    public void start() {
        // TODO: review later
        
        if (!copyCreator.isAlive()) {
            copyCreator.start();
        }
        
        Collection<Resource> children = getChildren();
        
        for (Resource child : children) {
            if (child instanceof SubscriberResource) {
                ((SubscriberResource) child).start();
            }
        }
    }
    
    @Override
    public void stop() {
        // TODO: review later
        
        if (copyCreator.isAlive()) {
            copyCreator.interrupt();
        }
        
        try {
            copyCreator.join();
        } catch (InterruptedException e) {
            // TODO: should never happen!
            throw new RuntimeException("Servlet thread interrupted before ending the stop!");
        }
        
        Collection<Resource> children = getChildren();
        
        for (Resource child : children) {
            if (child instanceof SubscriberResource) {
                ((SubscriberResource) child).stop();
            }
        }
    }
    
    private void postProcess(OM2MResource original, OM2MResource copy) {
        int resourceType = -1;
        String remoteID = original.getResourceID();
        String localID = copy.getResourceID();
        
        switch (original.getResourceType()) {
        case OM2M.RESOURCE_TYPE_APPLICATION_ENTITY:
            // Since I copied an application entity, I shall copy the container in it
            resourceType = OM2M.RESOURCE_TYPE_CONTAINER;
            break;
        case OM2M.RESOURCE_TYPE_CONTAINER:
            // I shall copy all content instances in it
            resourceType = OM2M.RESOURCE_TYPE_CONTENT_INSTANCE;
            break;
        case OM2M.RESOURCE_TYPE_CONTENT_INSTANCE:
            // I shall do nothing
            break;
        case OM2M.RESOURCE_TYPE_REMOTE_CSE:
            // I cannot copy these elements
            break;
        case OM2M.RESOURCE_TYPE_SUBSCRIPTION:
            // I cannot copy these elements
            break;
        default:
            // Other types are not implemented
        }
        
        if (resourceType < 1)
            return;
        
        SubscriptionRelation sr = new SubscriptionRelation(remoteID, resourceType, localID);
        
        addNewSubscription(sr);
    }
    
    private class SubscriptionThread extends Thread {
        
        SubscriptionRelation subscription;
        boolean              shallSubscribe;
        
        public SubscriptionThread(SubscriptionRelation subscription, boolean shallSubscribe) {
            this.subscription = subscription;
            this.shallSubscribe = shallSubscribe;
        }
        
        @Override
        public void run() {
            /*
             * try { sleep(5000); } catch (InterruptedException e) { // Do nothing }
             */
            
            if (shallSubscribe) {
                conn.createSubscription(subscription.getRemoteResourceID(), getFullURI());
                
                // The local container shall already be created
            }
            
            // TODO: switch to first children only
            // TODO: add also a filter for creationTime
            String[] uril = conn.getAllChildrenIDOfType(subscription.getRemoteResourceID(),
                    subscription.getChildTypeToCopy());
            
            SortedSet<OM2MResource> copySet = new TreeSet<>();
            
            // TODO: remove
            System.out.println(Arrays.toString(uril));
            
            for (String uri : uril) {
                OM2MResource child = conn.getResourceFromID(uri, subscription.getChildTypeToCopy());
                
                if (subscription.shallCopy(child)) {
                    copySet.add(child);
                }
            }
            
            addAllResourceToCopy(subscription, copySet);
        }
    }
    
    // TODO: some copies may be scheduled to be written, but never are if the thread
    // is interrupted
    private class CopyCreator extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    OM2MResource original = extractResourceToCopy();
                    
                    SubscriptionRelation relation = getAssociatedSubscription(original);
                    
                    OM2MResource copy = conn.create(relation.getLocalResourceID(), original);
                    
                    postProcess(original, copy);
                }
            } catch (InterruptedException ex) {
                // Thread terminates, as requested
            }
        }
    }
    
}
