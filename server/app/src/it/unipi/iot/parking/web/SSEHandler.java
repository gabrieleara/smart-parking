package it.unipi.iot.parking.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.servlet.AsyncContext;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import it.unipi.iot.parking.ParksDataHandler;
import it.unipi.iot.parking.data.ParkStatus;
import it.unipi.iot.parking.data.SpotStatus;
import it.unipi.iot.parking.om2m.OM2MException;
import it.unipi.iot.parking.om2m.data.OM2MResource;
import it.unipi.iot.parking.util.OM2MObservable;
import it.unipi.iot.parking.util.OM2MObservable.Observer;

public class SSEHandler {
    
    private static final long TIME_SECOND      = 1000;
    private static final long RETRY_INTERVAL   = 30 * TIME_SECOND;
    private static final long TIMEOUT_INTERVAL = 3 * RETRY_INTERVAL;
    
    private static final String EVENT_NEW_PARK_LIST = "newParkList";
    private static final String EVENT_PARK_UPDATE   = "parkUpdate";
    private static final String EVENT_SPOT_UPDATE   = "spotUpdate";
    
    private static ExecutorService requestsExecutor;
    
    private static final ObservingClientsHolder CLIENT_HANDLER = new ObservingClientsHolder();
    
    private static class StatusChangedRunnable implements Runnable {
        final OM2MResource resource;
        
        public StatusChangedRunnable(OM2MResource resource) {
            this.resource = resource;
        }
        
        public void deliverParkUpdate() {
            // The price of a park has been updated
            final ParkStatus park = ParksDataHandler.getParkStatus(resource);
            final String parkID = park.getParkID();
            final Set<AsyncContext> clients = CLIENT_HANDLER.getAssociatedClients(parkID);
            
            for (AsyncContext client : clients) {
                sendParkUpdate(client, park);
            }
        }
        
        public void deliverSpotUpdate() {
            // The status of a spot has been updated
            final SpotStatus spot = ParksDataHandler.getSpotStatus(resource);
            final String parkID = spot.getParkID();
            final Set<AsyncContext> clients = CLIENT_HANDLER.getAssociatedClients(parkID);
            
            for (AsyncContext client : clients) {
                sendSpotUpdate(client, spot);
            }
            
        }
        
        @Override
        public void run() {
            if (ParksDataHandler.isParkStatusUpdate(resource))
                deliverParkUpdate();
            else if (ParksDataHandler.isSpotStatusUpdate(resource)) {
                deliverSpotUpdate();
                
                SpotStatus spot = ParksDataHandler.getSpotStatus(resource);
                try {
                    int sign = spot.isFree() ? -1 : +1;
                    ParksDataHandler.updatePrice(spot.getParkID(), sign);
                } catch (OM2MException | TimeoutException e) {
                    // TODO: do something?
                }       
            }
            
        }
        
    }
    
    private static final OM2MObservable.Observer UPDATE_OBSERVER = new OM2MObservable.Observer() {
        @Override
        public void onObservableChanged(OM2MObservable observable, OM2MResource newResource) {
            
            StatusChangedRunnable task = new StatusChangedRunnable(newResource);
            requestsExecutor.execute(task);
            
        }
        
    };
    
    private SSEHandler() {
    }
    
    public static AsyncContext createSSEStream(HttpServletRequest request) {
        final AsyncContext client;
        
        client = request.startAsync();
        setClientRetryInterval(client);
        client.setTimeout(TIMEOUT_INTERVAL);
        CLIENT_HANDLER.addWaitingClient(client);
        client.addListener(CLIENT_HANDLER.getClientEventListener());
        
        return client;
    }
    
    private static void setClientRetryInterval(AsyncContext client) {
        final ServletResponse response;
        final PrintWriter out;
        
        try {
            response = client.getResponse();
            out = response.getWriter();
            out.write("retry: " + RETRY_INTERVAL + "\n");
            if (out.checkError()) { // checkError calls flush, and flush() does not throw
                                    // IOException
            }
        } catch (IllegalStateException | IOException ignored) {
        }
    }
    
    private static boolean sendEvent(final AsyncContext client, String event, final String data) {
        final ServletResponse response;
        final PrintWriter out;
        
        try {
            response = client.getResponse();
            
            synchronized (response) {
                out = response.getWriter();
                out.write("event: " + event + "\n");
                out.write("data: " + data + "\n\n");
                if (out.checkError()) { // checkError calls flush, and flush() does not throw
                                        // IOException
                    return false;
                }
                
                return true;
            }
        } catch (IllegalStateException | IOException ignored) {
            return false;
        }
    }
    
    public static void sendNewParkList(final AsyncContext client, final List<ParkStatus> parkList) {
        final String message;
        final JSONObject messageObject;
        final List<JSONObject> parksJSONList;
        final JSONArray parksArray;
        
        parksJSONList = parkList.stream()
                                .map(p -> p.toJSONObject())
                                .collect(Collectors.toList());
        
        parksArray = new JSONArray(parksJSONList);
        messageObject = new JSONObject().put("parks", parksArray);
        
        String[] parkIDs = new String[parkList.size()];
        
        for (int i = 0; i < parkIDs.length; ++i) {
            parkIDs[i] = parkList.get(i)
                                 .getParkID();
        }
        
        if (!serveClient(client, parkIDs))
            return;
        
        message = messageObject.toString();
        
        if (!sendEvent(client, EVENT_NEW_PARK_LIST, message)) {
            System.out.println("Client was already gone before first reply!");
            removeClient(client);
        } else {
            System.out.println("Sent response to client!");
        }
    }
    
    public static void sendParkUpdate(final AsyncContext client, final ParkStatus park) {
        final JSONObject data = park.toJSONObject();
        
        if (!sendEvent(client, EVENT_PARK_UPDATE, data.toString())) {
            removeClient(client);
        }
    }
    
    public static void sendSpotUpdate(final AsyncContext client, final SpotStatus spot) {
        final JSONObject data = spot.toJSONObject();
        
        if (!sendEvent(client, EVENT_SPOT_UPDATE, data.toString())) {
            removeClient(client);
        }
    }
    
    public static void init() {
        CLIENT_HANDLER.reset();
        requestsExecutor = new ThreadPoolExecutor(10, 20, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());
    }
    
    public static void clear() {
        CLIENT_HANDLER.reset();
        requestsExecutor.shutdown();
        requestsExecutor = null;
    }
    
    public static Observer getObserver() {
        return UPDATE_OBSERVER;
    }
    
    public static boolean isClientWaiting(AsyncContext client) {
        return CLIENT_HANDLER.isClientWaiting(client);
    }
    
    public static boolean isClientListening(AsyncContext client) {
        return CLIENT_HANDLER.isClientListening(client);
    }
    
    public static boolean isClientThere(AsyncContext client) {
        return CLIENT_HANDLER.isClientThere(client);
    }
    
    public static boolean isClientGone(AsyncContext client) {
        return CLIENT_HANDLER.isClientGone(client);
    }
    
    public static boolean addWaitingClient(AsyncContext client) {
        return CLIENT_HANDLER.addWaitingClient(client);
    }
    
    public static boolean serveClient(AsyncContext client, String[] keys) {
        return CLIENT_HANDLER.serveClient(client, keys);
    }
    
    public static boolean removeClient(AsyncContext client) {
        return CLIENT_HANDLER.removeClient(client);
    }
}
