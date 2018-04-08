package it.unipi.iot.parking.web;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;

import it.unipi.iot.parking.util.SimpleMultiMap;

public class ObservingClientsHolder {
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
            // Do nothing
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
    
    public Set<AsyncContext> getAssociatedClients(String parkID) {
        final Set<AsyncContext> clientsReference;
        final Set<AsyncContext> clients;
        
        synchronized (COLLECTIONS_MONITOR) {
            clientsReference = servedClients.get(parkID);
            
            if(clientsReference == null)
                clients = new HashSet<>();
            else
                clients = new HashSet<>(clientsReference);
        }
        
        return clients;
    }
    
    public AsyncListener getClientEventListener() {
        return clientEventListener;
    }
    
    public void reset() {
        synchronized (COLLECTIONS_MONITOR) {
            waitingClients.clear();
            servedClients.clear();
        }
    }
}