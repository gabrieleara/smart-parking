package it.unipi.iot.parking.web;

import java.util.concurrent.TimeoutException;

import it.unipi.iot.parking.ParksDataHandler;
import it.unipi.iot.parking.om2m.data.RemoteNode;
import it.unipi.iot.parking.om2m.subscriber.NodeSubscriber;
import it.unipi.iot.parking.om2m.subscriber.SubscriptionServer;
import it.unipi.iot.parking.util.OM2MObservable;

public class InitializeSubscriptorThread extends Thread {
    private final SubscriptionServer server;
    private final OM2MObservable.Observer observer;
    
    public InitializeSubscriptorThread(SubscriptionServer server, OM2MObservable.Observer observer) {
        super();
        this.server = server;
        this.observer = observer;
    }
    
    @Override
    public void run() {
        try {
            NodeSubscriber nodeSubscriber = new NodeSubscriber(server, "in-subscriber", observer);
            
            // Deleting any previous subscription
            ParksDataHandler.deleteAllSubscriptions("parking");
            
            ParksDataHandler.subscribe("parking", nodeSubscriber.getFullURI());
            
            String[] uril = ParksDataHandler.getAllRemoteNodes();
            
            for (String uri : uril) {
                RemoteNode rcse = (RemoteNode) ParksDataHandler.get(uri);
                
                nodeSubscriber.subscribeToNode(rcse);
            }
            
        } catch (TimeoutException e) {
            throw new RuntimeException("IN node is not responding to requests!");
        }
    }
}
