package it.unipi.iot.parking.web;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.servlet.AsyncContext;

import it.unipi.iot.parking.ParksDataHandler;
import it.unipi.iot.parking.data.ParkStatus;
import it.unipi.iot.parking.util.Bounds;
import it.unipi.iot.parking.util.SSEHandler;

class GetParksRunnable implements Runnable {
    private final AsyncContext client;
    private final Bounds       bounds;
    
    public GetParksRunnable(AsyncContext client, Bounds bounds) {
        this.bounds = bounds;
        this.client = client;
    }
    
    @Override
    public void run() {
        final String[] parks;
        final List<ParkStatus> parkList;
        
        try {
            if (!SSEHandler.isClientWaiting(client))
                return;
            
            parks = ParksDataHandler.getAllParksList();
            
            if (!SSEHandler.isClientWaiting(client))
                return;
            
            parkList = new ArrayList<>();
            
            for (String park : parks) {
                ParkStatus parkData = ParksDataHandler.getParkStatus(park);
                
                if (!bounds.acceptPark(parkData))
                    continue;
                
                parkList.add(parkData);
                
                if (!SSEHandler.isClientWaiting(client))
                    return;
            }
            
            SSEHandler.sendNewParkList(client, parkList);
            
        } catch (TimeoutException e) { // What to do if IN is unavailable?
            throw new RuntimeException(
                    "IN node unreachable! " + "Contact system administrator as soon as possible!",
                    e);
        }
    }    
}
