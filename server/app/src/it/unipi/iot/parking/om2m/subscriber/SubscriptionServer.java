package it.unipi.iot.parking.om2m.subscriber;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Collection;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.server.resources.Resource;

import it.unipi.iot.parking.util.FullURIResource;

public class SubscriptionServer extends CoapServer implements FullURIResource {
    
    /**
     * Machine IP address, base for all servers
     */
    static final String IPAddress;
    
    static {
        try {
            IPAddress = Inet4Address.getLocalHost()
                                    .getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Server full URI, without '/' at the end.
     */
    private final String serverURI;
    
    // TODO: change to package
    public SubscriptionServer(int port) {
        super(port);
        serverURI = "coap://" + IPAddress + ":" + port;
    }
    
    @Override
    public String getFullURI() {
        return serverURI;
    }
    
    @Override
    public synchronized void start() {
        super.start();
        
        Resource root = getRoot();
        
        Collection<Resource> children = root.getChildren();
        
        for (Resource child : children) {
            if (child instanceof ResourceSubscriber) {
                ((ResourceSubscriber) child).start();
            }
        }
    }
    
    @Override
    public synchronized void stop() {
        super.stop();
        
        Resource root = getRoot();
        
        Collection<Resource> children = root.getChildren();
        
        for (Resource child : children) {
            if (child instanceof ResourceSubscriber) {
                ((ResourceSubscriber) child).stop();
            }
        }
    }
    
}