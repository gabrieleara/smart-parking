package it.unipi.iot.parking.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.JSONArray;
import org.json.JSONObject;

import it.unipi.iot.parking.om2m.OM2M;
import it.unipi.iot.parking.om2m.SubscriberResource;
import it.unipi.iot.parking.om2m.SubscriptionServer;
import it.unipi.iot.parking.om2m.data.ContentInstance;
import it.unipi.iot.parking.om2m.data.OM2MResource;
import it.unipi.iot.parking.om2m.data.RemoteCSE;
import it.unipi.iot.parking.om2m.subscriber.CopySubscriberResource;
import it.unipi.iot.parking.om2m.subscriber.SubscriptionRelation;

/**
 * Servlet implementation class BackgroundServlet
 */
@WebServlet(name = "parks-servlet", urlPatterns = { "/servlets/parks" }, loadOnStartup = 1)
public class ParkingServlet extends HttpServlet {
    // Used for the Serialization, let us just leave it here
    private static final long serialVersionUID = 1L;
    
    private SubscriptionServer server = null;
    
    /**
     * @see Servlet#init()“
     */
    public void init() throws ServletException {
        System.out.println(ParkingServlet.class.getName() + " has been initialized!");
        
        // TODO: read from configuration file, one single configuration file for each
        // node kind
        OM2M om2m = OM2M.init("127.0.0.1", "5683", "admin:admin");
        
        if (server == null)
            server = new SubscriptionServer(8023);
        
        server.start();
        
        /*
         * String[] subscribers = om2m.getAllChildrenIDOfType("parking",
         * OM2M.RESOURCE_TYPE_SUBSCRIPTION);
         */
        /*
         * System.out.println(om2m.testQuery("parking", new String[] { "fu=1", "rty=" +
         * OM2M.RESOURCE_TYPE_SUBSCRIPTION, "pi=parking"}));
         */
        System.out.println("Prova");
        
        /*
         * String[] nodeSubscriptions = om2m.getAllFirstChildrenIDOfType("parking",
         * 
         * OM2M.RESOURCE_TYPE_SUBSCRIPTION);
         * 
         * if (nodeSubscriptions.length > 0) { Subscription s =
         * om2m.getResourceFromID(resourceID, resourceType)
         * 
         * Subscription s = new Subscription(
         * om2m.getResourceObjectFromID(nodeSubscriptions[0],
         * OM2M.RESOURCE_TYPE_SUBSCRIPTION));
         * 
         * String[] path = s.getNotificationURIs()[0].split("[\\\\/]"); String name =
         * path[path.length - 1];
         * 
         * new NodeSubscriber(server, name); } else { NodeSubscriber ns = new
         * NodeSubscriber(server); om2m.createSubscription("parking", ns.getFullURI());
         * }
         */
        
        SubscriberResource nodeSubscriber = new SubscriberResource(server) {
            
            @Override
            protected void handlePost(CoapExchange exchange) {
                JSONObject body = new JSONObject(exchange.getRequestText());
                
                if (isVerificationRequest(body)) {
                    return;
                }
                
                if (isSubscriptionDeletion(body)) {
                    return;
                }
                
                // Not first time and got a new value
                OM2MResource resource = getResource(body);
                
                if (resource == null || !(resource instanceof RemoteCSE))
                    return;
                
                RemoteCSE rcse = (RemoteCSE) resource;
                
                CopySubscriberResource csr = new CopySubscriberResource(this);
                
                SubscriptionRelation newSub = new SubscriptionRelation(rcse.getCSEID(),
                        OM2M.RESOURCE_TYPE_APPLICATION_ENTITY, rcse.getResourceID());
                
                csr.addNewSubscription(newSub);
            }
        };
        
        om2m.createSubscription("parking", nodeSubscriber.getFullURI());
        
    }
    
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        PrintWriter writer = response.getWriter();
        
        JSONObject responseObj = new JSONObject();
        
        OM2M conn = OM2M.getInstance();
        
        String[] uril = conn.getAllChildrenIDOfType("parking", OM2M.RESOURCE_TYPE_CONTENT_INSTANCE,
                new String[] { "lbl=type/manifest" });
        
        List<ContentInstance> parks = new ArrayList<>();
        
        for (String uri : uril) {
            ContentInstance ci = (ContentInstance) conn.getResourceFromID(uri,
                    OM2M.RESOURCE_TYPE_CONTENT_INSTANCE);
            
            if (ci != null)
                parks.add(ci);
        }
        
        // ContentInstance[] parksInstances = parks.toArray(new
        // ContentInstance[parks.size()]);
        JSONArray arr = new JSONArray();
        
        for (ContentInstance in : parks) {
            arr.put(in.getContentValue());
        }
        
        responseObj.put("parks", arr);
        
        writer.append(responseObj.toString(2));
        
        // response.getWriter().append("Served at: ").append(request.getContextPath());
    }
    
    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        // doGet(request, response);
    }
    
    @Override
    public void destroy() {
        super.destroy(); // I don't know if it is needed
        
        server.stop();
        /*
         * server.destroy(); server = null;
         */
        
        System.out.println(ParkingServlet.class.getName() + " has been destroyed!");
    }
    
}
