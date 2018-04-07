package it.unipi.iot.parking.web;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.unipi.iot.parking.om2m.subscriber.DuplicatorThread;
import it.unipi.iot.parking.om2m.subscriber.SubscriptionServer;
import it.unipi.iot.parking.util.Bounds;
import it.unipi.iot.parking.util.SSEHandler;

/**
 * Servlet implementation class BackgroundServlet
 */
// TODO: LOGGER
@WebServlet(name = "parks-servlet", urlPatterns = {
        "/servlets/parks" }, loadOnStartup = 1, asyncSupported = true)
public class ParkingServlet extends HttpServlet {
    // Used for the Serialization, let us just leave it here
    private static final long serialVersionUID = 1L;
    
    private SubscriptionServer     server       = null;
    private DuplicatorThread       dthread      = null;
    
    private ExecutorService requestsExecutor = new ThreadPoolExecutor(10, 20, 0L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    
    /**
     * @see Servlet#init()“
     */
    public void init() throws ServletException {
        if (server == null)
            server = new SubscriptionServer(8023);
        
        if (dthread == null)
            dthread = DuplicatorThread.init();
        
        SSEHandler.init();
        
        server.start();
        dthread.start();
        
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        
        InitializeSubscriptorThread t = new InitializeSubscriptorThread(server, SSEHandler.getObserver());
        t.start();
    }
    
    private double parseParameterOrDefault(HttpServletRequest request, String param,
            double defaultv) {
        try {
            return Double.parseDouble(request.getParameter(param));
        } catch (NullPointerException | NumberFormatException e) {
            return defaultv;
        }
    }
    
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        final double minLat;
        final double minLon;
        final double maxLat;
        final double maxLon;
        final Bounds bounds;
        final AsyncContext client;
        
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        
        client = SSEHandler.createSSEStream(request);
        
        minLat = parseParameterOrDefault(request, "minLat", Double.NEGATIVE_INFINITY);
        minLon = parseParameterOrDefault(request, "minLon", Double.NEGATIVE_INFINITY);
        maxLat = parseParameterOrDefault(request, "maxLat", Double.POSITIVE_INFINITY);
        maxLon = parseParameterOrDefault(request, "maxLon", Double.POSITIVE_INFINITY);
        
        bounds = new Bounds(minLat, minLon, maxLat, maxLon);
        
        GetParksRunnable task = new GetParksRunnable(client, bounds);
        requestsExecutor.execute(task);
    }
    
    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
    
    @Override
    public void destroy() {
        super.destroy(); // I don't know if it is needed
        
        server.stop();
        dthread.interrupt();
        // TODO: Destroy all clients
        // TODO: Stop pool of executors and create later a new one
        
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
