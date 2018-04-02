package it.unipi.iot.parking.web;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.unipi.iot.parking.ParksDataHandler;
import it.unipi.iot.parking.om2m.data.RemoteNode;
import it.unipi.iot.parking.om2m.subscriber.DuplicatorThread;
import it.unipi.iot.parking.om2m.subscriber.NodeSubscriber;
import it.unipi.iot.parking.om2m.subscriber.SubscriptionServer;

/**
 * Servlet implementation class BackgroundServlet
 */
@WebServlet(name = "parks-servlet", urlPatterns = { "/servlets/parks" }, loadOnStartup = 1)
public class ParkingServlet extends HttpServlet {
    // Used for the Serialization, let us just leave it here
    private static final long serialVersionUID = 1L;
    
    private SubscriptionServer server  = null;
    private DuplicatorThread   dthread = null;
    
    /**
     * @see Servlet#init()“
     */
    public void init() throws ServletException {
        // TODO: switch to a LOGGER
        System.out.println(ParkingServlet.class.getName() + " has been initialized!");
        
        if (server == null)
            server = new SubscriptionServer(8023);
        
        if (dthread == null)
            dthread = DuplicatorThread.init();
        
        server.start();
        dthread.start();
        
        // Following line is used to invalidate code and simulate a reboot of the system
        // when in debug mode, modify that line so that the code needs to be rebooted
        System.out.println("Prova1");
        
        NodeSubscriber nodeSubscriber = new NodeSubscriber(server, "in-subscriber");
        
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        
        // DELETE ALL PREVIOUS SUBSCRIPTIONS
        try {
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
    
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        /*
         * PrintWriter writer = response.getWriter();
         * 
         * JSONObject responseObj = new JSONObject();
         * 
         * OM2M conn = OM2M.getInstance();
         * 
         * String[] uril = conn.getAllChildrenIDOfType("parking",
         * OM2MConstants.RESOURCE_TYPE_CONTENT_INSTANCE, new String[] {
         * "lbl=type/manifest" });
         * 
         * List<ContentInstance> parks = new ArrayList<>();
         * 
         * for (String uri : uril) { ContentInstance ci = (ContentInstance)
         * conn.getResourceFromID(uri, OM2MConstants.RESOURCE_TYPE_CONTENT_INSTANCE);
         * 
         * if (ci != null) parks.add(ci); }
         * 
         * // ContentInstance[] parksInstances = parks.toArray(new //
         * ContentInstance[parks.size()]); JSONArray arr = new JSONArray();
         * 
         * for (ContentInstance in : parks) { arr.put(in.getContentValue()); }
         * 
         * responseObj.put("parks", arr);
         * 
         * writer.append(responseObj.toString(2));
         * 
         * //
         * response.getWriter().append("Served at: ").append(request.getContextPath());
         * 
         */
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
        dthread.interrupt();
        
        try {
            dthread.join();
        } catch (InterruptedException e) {
            System.out.println(
                    "Server was terminated before the termination of the Duplicator Thread.");
        }
        
        dthread = null;
        
        // TODO: check if this is needed (shouldn't be)
        // server.destroy();
        // server = null;
        
        System.out.println(ParkingServlet.class.getName() + " has been destroyed!");
    }
    
}
