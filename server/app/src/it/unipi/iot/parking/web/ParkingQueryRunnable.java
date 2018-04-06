package it.unipi.iot.parking.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.servlet.AsyncContext;
import javax.servlet.ServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import it.unipi.iot.parking.ParksDataHandler;
import it.unipi.iot.parking.util.Bounds;

class GetParksRunnable implements Runnable {
    private final AsyncContext           client;
    private final Bounds                 bounds;
    private final ObservingClientsHolder clientHolder;
    
    public GetParksRunnable(AsyncContext client, ObservingClientsHolder clientHolder,
            Bounds bounds) {
        this.bounds = bounds;
        this.client = client;
        this.clientHolder = clientHolder;
    }
    
    @Override
    public void run() {
        
        final JSONObject responseObj;
        final String[] parks;
        final List<JSONObject> parkList;
        final JSONArray parksArray;
        
        try {
            if (!clientHolder.isClientWaiting(client))
                return;
            
            parks = ParksDataHandler.getAllParksList();
            
            if (!clientHolder.isClientWaiting(client))
                return;
            
            parkList = new ArrayList<>();
            
            for (String park : parks) {
                JSONObject parkData = ParksDataHandler.getParkData(park);
                
                if (!bounds.acceptPark(parkData))
                    continue;
                
                parkList.add(parkData);
                
                if (!clientHolder.isClientWaiting(client))
                    return;
            }
            
            parksArray = new JSONArray(parkList);
            responseObj = new JSONObject().put("parks", parksArray);
            
            String[] parkIDs = new String[parkList.size()];
            
            for (int i = 0; i < parkIDs.length; ++i) {
                parkIDs[i] = parkList.get(i)
                                     .getString("id");
            }
            
            if (!clientHolder.serveClient(client, parkIDs))
                return;
            
            String message = responseObj.toString();
            
            System.out.println("DATA: " + message);
            
            if (!writeToStream(client, message)) {
                System.out.println("Client was already gone before first reply!");
                clientHolder.removeClient(client);
            } else {
                System.out.println("Sent response to client!");
            }
            
        } catch (TimeoutException e) { // What to do if IN is unavailable?
            throw new RuntimeException(
                    "IN node unreachable! " + "Contact system administrator as soon as possible!",
                    e);
        }
    }
    
    private boolean writeToStream(AsyncContext context, String content) {
        final ServletResponse response;
        final PrintWriter out;
        
        try {
            response = context.getResponse();
            out = response.getWriter();
            // out.write("event: message\n"); // TODO: different events!
            out.write("data: " + content + "\n\n");
            if (out.checkError()) { // checkError calls flush, and flush() does not throw
                                    // IOException
                return false;
            }
            
            return true;
        } catch (IllegalStateException | IOException ignored) {
            return false;
        }
    }
    
}
