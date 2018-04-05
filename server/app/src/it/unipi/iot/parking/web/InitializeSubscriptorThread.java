package it.unipi.iot.parking.web;

import java.util.concurrent.TimeoutException;

import it.unipi.iot.parking.ParksDataHandler;
import it.unipi.iot.parking.om2m.data.RemoteNode;
import it.unipi.iot.parking.om2m.subscriber.NodeSubscriber;
import it.unipi.iot.parking.om2m.subscriber.SubscriptionServer;

public class InitializeSubscriptorThread extends Thread {
    private final SubscriptionServer server;
    
    public InitializeSubscriptorThread(SubscriptionServer server) {
        super();
        this.server = server;
    }
    
    @Override
    public void run() {
        try {
            NodeSubscriber nodeSubscriber = new NodeSubscriber(server, "in-subscriber");
            
            // DELETE ALL PREVIOUS SUBSCRIPTIONS
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
