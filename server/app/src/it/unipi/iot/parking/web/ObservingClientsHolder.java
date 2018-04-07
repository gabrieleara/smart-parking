package it.unipi.iot.parking.web;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;

import it.unipi.iot.parking.om2m.data.OM2MResource;
import it.unipi.iot.parking.util.OM2MObservable;
import it.unipi.iot.parking.util.SimpleMultiMap;

public class ObservingClientsHolder implements OM2MObservable.Observer {
    private final Object COLLECTIONS_MONITOR = new Object();
    
    /**
     * A set of clients that asked for a parking spot but did not receive first
     * reply yet.
     */
    private final Set<AsyncContext> waitingClients = new HashSet<>();
    
    /**
     * An association between a parking and its set of clients that observe its
     * status.
     */
    private final SimpleMultiMap<String, AsyncContext> servedClients = new SimpleMultiMap<>();
    
    /**
     * A listener used to remove clients from any of the previous sets when the
     * stream request is canceled.
     */
    private final ClientEventListener clientEventListener = new ClientEventListener();
    
    private class ClientEventListener implements AsyncListener {
        @Override
        public void onComplete(AsyncEvent event) throws IOException {
            removeClient(event.getAsyncContext());
        }
        
        @Override
        public void onError(AsyncEvent event) throws IOException {
            removeClient(event.getAsyncContext());
        }
        
        @Override
        public void onStartAsync(AsyncEvent event) throws IOException {
            // TODO: do nothing?
        }
        
        @Override
        public void onTimeout(AsyncEvent event) throws IOException {
            removeClient(event.getAsyncContext());
        }
    }
    
    public boolean isClientWaiting(AsyncContext client) {
        synchronized (COLLECTIONS_MONITOR) {
            return waitingClients.contains(client);
        }
    }
    
    public boolean isClientListening(AsyncContext client) {
        synchronized (COLLECTIONS_MONITOR) {
            return servedClients.contains(client);
        }
    }
    
    public boolean isClientThere(AsyncContext client) {
        synchronized (COLLECTIONS_MONITOR) {
            return isClientWaiting(client) || isClientListening(client);
        }
    }
    
    public boolean isClientGone(AsyncContext client) {
        synchronized (COLLECTIONS_MONITOR) {
            return !isClientThere(client);
        }
    }
    
    public boolean addWaitingClient(AsyncContext client) {
        synchronized (COLLECTIONS_MONITOR) {
            return waitingClients.add(client);
        }
    }
    
    public boolean serveClient(AsyncContext client, String[] keys) {
        synchronized (COLLECTIONS_MONITOR) {
            if (!waitingClients.remove(client))
                return false;
            
            for (String k : keys) {
                servedClients.put(k, client);
            }
            
            return true;
        }
    }
    
    public boolean removeClient(AsyncContext client) {
        synchronized (COLLECTIONS_MONITOR) {
            boolean wasWaiting = waitingClients.remove(client);
            boolean wasServed = servedClients.removeAll(client);
            
            return wasWaiting || wasServed;
        }
    }
    
    public AsyncListener getClientEventListener() {
        return clientEventListener;
    }
    
    // TODO: hide the resource behind this, keep only the data inferred from the new
    // value after implementing the classes associated to a park manifest and a park
    // spot and their statuses
    @Override
    public void onObservableChanged(OM2MObservable observable, OM2MResource newResource) {
        // TODO: implement this
        System.out.println("FUNZIONA!");
    }
    
    public void reset() {
        // TODO: implement it
    }
}