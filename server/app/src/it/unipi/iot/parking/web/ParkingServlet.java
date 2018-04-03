package it.unipi.iot.parking.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

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
        System.out.println("Prova");
        
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
    
    private class Bounds {
        final double minLatitude, minLongitude, maxLatitude, maxLongitude;
        
        public Bounds(double minLatitude, double minLongitude, double maxLatitude,
                double maxLongitude) {
            super();
            this.minLatitude = minLatitude;
            this.minLongitude = minLongitude;
            this.maxLatitude = maxLatitude;
            this.maxLongitude = maxLongitude;
        }
        
        public boolean acceptPark(JSONObject parkDescriptor) {
            final double latitude;
            final double longitude;
            
            latitude = parkDescriptor.getDouble("lat");
            longitude = parkDescriptor.getDouble("lon");
            
            if (latitude < minLatitude || latitude > maxLatitude)
                return false;
            if (longitude < minLongitude || longitude > maxLongitude)
                return false;
            
            return true;
        }
        
    }
    
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        final PrintWriter writer;
        final JSONObject responseObj;
        final String[] parks;
        final List<JSONObject> parkList;
        final JSONArray parksArray;
        final Bounds bounds;
        double minLat, minLon, maxLat, maxLon;
        
        try {
            minLat = Double.parseDouble(request.getParameter("minLat"));
        } catch (NullPointerException | NumberFormatException e) {
            minLat = Double.NEGATIVE_INFINITY;
        }
        try {
            minLon = Double.parseDouble(request.getParameter("minLon"));
        } catch (NullPointerException | NumberFormatException e) {
            minLon = Double.NEGATIVE_INFINITY;
        }
        try {
            maxLat = Double.parseDouble(request.getParameter("maxLat"));
        } catch (NullPointerException | NumberFormatException e) {
            maxLat = Double.POSITIVE_INFINITY;
        }
        try {
            maxLon = Double.parseDouble(request.getParameter("maxLon"));
        } catch (NullPointerException | NumberFormatException e) {
            maxLon = Double.POSITIVE_INFINITY;
        }
        
        bounds = new Bounds(minLat, minLon, maxLat, maxLon);
        
        // TODO: switch to application/javascript in deploy
        //response.setContentType("application/javascript");
        response.setContentType("text/json");
        writer = response.getWriter();
        responseObj = new JSONObject();
        
        try {
            parks = ParksDataHandler.getAllParksList();
            
            parkList = new ArrayList<>();
            
            for (String park : parks) {
                JSONObject parkData = ParksDataHandler.getParkData(park);
                
                if (!bounds.acceptPark(parkData))
                    continue;
                
                parkList.add(parkData);
            }
            
            parksArray = new JSONArray(parkList);
            
            responseObj.put("parks", parksArray);
            
            writer.append(responseObj.toString(2));
            /*
             * response.getWriter() .append("Served at: ")
             * .append(request.getContextPath());
             */
        } catch (TimeoutException e) {
            // What to do if IN is unavailable?
            throw new RuntimeException(
                    "IN node unreachable! Contact system administrator as soon as possible!", e);
        }
        
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
