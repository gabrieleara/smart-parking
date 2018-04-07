package it.unipi.iot.parking.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.AsyncContext;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import it.unipi.iot.parking.data.ParkStatus;
import it.unipi.iot.parking.data.SpotStatus;
import it.unipi.iot.parking.util.OM2MObservable.Observer;
import it.unipi.iot.parking.web.ObservingClientsHolder;

public class SSEHandler {
    
    private static final long TIME_SECOND      = 1000;
    private static final long RETRY_INTERVAL   = 30 * TIME_SECOND;
    private static final long TIMEOUT_INTERVAL = 3 * RETRY_INTERVAL;
    
    private static final String EVENT_NEW_PARK_LIST = "newParkList";
    private static final String EVENT_PARK_UPDATE   = "parkUpdate";
    private static final String EVENT_SPOT_UPDATE   = "spotUpdate";
    
    private static final ObservingClientsHolder CLIENT_HANDLER = new ObservingClientsHolder();
    
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
            out = response.getWriter();
            out.write("event: " + event + "\n");
            out.write("data: " + data + "\n\n");
            if (out.checkError()) { // checkError calls flush, and flush() does not throw
                                    // IOException
                return false;
            }
            
            return true;
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
        // TODO: implement it
    }
    
    public static void sendSpotUpdate(final AsyncContext client, final SpotStatus spot) {
        // TODO: implement it
    }
    
    public static void init() {
        CLIENT_HANDLER.reset();
    }
    
    public static Observer getObserver() {
        return CLIENT_HANDLER;
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
