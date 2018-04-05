package it.unipi.iot.parking.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import it.unipi.iot.parking.ParksDataHandler;
import it.unipi.iot.parking.om2m.subscriber.DuplicatorThread;
import it.unipi.iot.parking.om2m.subscriber.SubscriptionServer;
import it.unipi.iot.parking.util.SimpleMultiMap;

/**
 * Servlet implementation class BackgroundServlet
 */
@WebServlet(name = "parks-servlet", urlPatterns = {
        "/servlets/parks" }, loadOnStartup = 1, asyncSupported = true)
public class ParkingServlet extends HttpServlet {
    // Used for the Serialization, let us just leave it here
    private static final long serialVersionUID = 1L;
    
    private SubscriptionServer server  = null;
    private DuplicatorThread   dthread = null;
    // private SSEventHandler sseHandler = null;
    
    private final Object                               STREAMS_MONITOR   = new Object();
    private final Set<AsyncContext>                    waitingStreams    = new HashSet<>();
    private final SimpleMultiMap<String, AsyncContext> parkStreams       = new SimpleMultiMap<>();
    private final ParkEventListener                    parkEventListener = new ParkEventListener();
    
    private class ParkEventListener implements AsyncListener {
        private void removeContext(AsyncContext context) {
            synchronized (STREAMS_MONITOR) {
                /* boolean changed = */
                parkStreams.removeAll(context);
                
                /* if (!changed) */
                waitingStreams.remove(context);
            }
        }
        
        @Override
        public void onComplete(AsyncEvent event) throws IOException {
            removeContext(event.getAsyncContext());
        }
        
        @Override
        public void onError(AsyncEvent event) throws IOException {
            removeContext(event.getAsyncContext());
        }
        
        @Override
        public void onStartAsync(AsyncEvent event) throws IOException {
            // TODO: do nothing?
        }
        
        @Override
        public void onTimeout(AsyncEvent event) throws IOException {
            removeContext(event.getAsyncContext());
        }
    }
    
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
        
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        
        InitializeSubscriptorThread t = new InitializeSubscriptorThread(server);
        t.start();
    }
    
    private static class Bounds {
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
        
        final Bounds bounds;
        double minLat, minLon, maxLat, maxLon;
        
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        
        // writer = response.getWriter();
        final AsyncContext enquiring;
        
        synchronized (STREAMS_MONITOR) {
            enquiring = request.startAsync();
            enquiring.setTimeout(60 * 1000);
            enquiring.addListener(parkEventListener);
            waitingStreams.add(enquiring);
        }
        
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
        
        Runnable query = new Runnable() {
            JSONObject       responseObj;
            String[]         parks;
            List<JSONObject> parkList;
            JSONArray        parksArray;
            
            @Override
            public void run() {
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
                    
                    synchronized (STREAMS_MONITOR) {
                        // Move the enquiring from the set to the map (multiple copies) and notify
                        // him
                        waitingStreams.remove(enquiring);
                        
                        for (JSONObject parkData : parkList) {
                            parkStreams.put(parkData.getString("id"), enquiring);
                        }
                    }
                    
                    // TODO: if it doesn't go well, remove the park
                    writeToStream(enquiring, responseObj.toString());
                    
                } catch (TimeoutException e) { // What to do if IN is unavailable?
                    throw new RuntimeException("IN node unreachable! "
                            + "Contact system administrator as soon as possible!", e);
                }
                
            }
            
        };
        
        new Thread(query).start();
        
        /*
         * try { parks = ParksDataHandler.getAllParksList();
         * 
         * parkList = new ArrayList<>();
         * 
         * for (String park : parks) { JSONObject parkData =
         * ParksDataHandler.getParkData(park);
         * 
         * if (!bounds.acceptPark(parkData)) continue;
         * 
         * parkList.add(parkData); }
         * 
         * parksArray = new JSONArray(parkList);
         * 
         * responseObj.put("parks", parksArray);
         * 
         * //writer.append(responseObj.toString(2));
         * 
         * } catch (TimeoutException e) { // What to do if IN is unavailable? throw new
         * RuntimeException(
         * "IN node unreachable! Contact system administrator as soon as possible!", e);
         * }
         */
    }
    
    private boolean writeToStream(AsyncContext context, String content) {
        final ServletResponse response = context.getResponse();
        final PrintWriter out;
        
        try {
            out = response.getWriter();
            out.write(content);
            if (out.checkError()) { // checkError calls flush, and flush() does not
                                    // throw IOException
                return false;
            }
            
            return true;
        } catch (IOException ignored) {
            return false;
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
        // Destroy the sseHandler
        
        try {
            dthread.join();
        } catch (InterruptedException e) {
            System.out.println(
                    "Server was terminated before the termination of the Duplicator Thread.");
        }
        
        dthread = null;
        // TODO: empty the streams list
        
        // TODO: check if this is needed (shouldn't be)
        // server.destroy();
        // server = null;
        
        System.out.println(ParkingServlet.class.getName() + " has been destroyed!");
    }
    
}
