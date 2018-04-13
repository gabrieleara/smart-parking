package it.unipi.iot.parking.mn.app;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.Request;
import org.json.JSONObject;

import it.unipi.iot.parking.AppConfig;
import it.unipi.iot.parking.ParkConfig;
import it.unipi.iot.parking.ParksDataHandler;
import it.unipi.iot.parking.om2m.ErrorCode;
import it.unipi.iot.parking.om2m.OM2MException;

public class Application {
    
    public static String getSpotIP(String netIP, int index) {
        return netIP + Integer.toHexString(index + 1);
    }
    
    static ExecutorService executor = Executors.newSingleThreadExecutor();
    
    static class OccupySpotRunnable implements Runnable {
        final ParkConfig park;
        final int        index;
        final String     user;
        final String     credit;
        
        public OccupySpotRunnable(ParkConfig park, int index, String user, String credit) {
            super();
            this.park = park;
            this.index = index;
            this.user = user;
            this.credit = credit;
        }
        
        @Override
        public void run() {
            boolean success;
            
            System.out.println("Trying to occupy spot " + index + " of park " + park.name + "...");
            
            try {
                success = ParksDataHandler.payForSpot(park.parkID, index, user, credit);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            
            if (!success) {
                new CoapClient().advanced(Request.newPost()
                                                 .setURI("coap://[" + getSpotIP(park.netIP, index)
                                                         + "]:5683/park_spot")
                                                 .setPayload("free=1"));
                System.out.println("Spot " + index + " of park " + park.name + " was already occupied!");
                return;
            }
            
            new CoapClient().advanced(Request.newPost()
                                             .setURI("coap://[" + getSpotIP(park.netIP, index)
                                                     + "]:5683/park_spot")
                                             .setPayload("free=0"));
            System.out.println("Spot " + index + " of park " + park.name + " occupied successfully!");
        }
        
    }
    
    static class FreeSpotRunnable implements Runnable {
        final ParkConfig park;
        final int    index;
        
        public FreeSpotRunnable(ParkConfig park, int index) {
            this.park = park;
            this.index = index;
        }
        
        @Override
        public void run() {
            boolean success;
            
            System.out.println("Trying to free spot " + index + " of park " + park.name + "...");
            
            try {
                success = ParksDataHandler.freeSpot(park.parkID, index);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            
            if (!success) {
            	System.out.println("Spot " + index + " of park " + park.name + " was already free!");
                return;
            }
            
            System.out.println("Trying to occupy spot " + index + " of park " + park.name + " freed successfully!");
            
            return;
        }
        
    }
    
    static class SpotCoapHandler implements CoapHandler {
        final int        index;
        final ParkConfig park;
        
        SpotCoapHandler(ParkConfig park, int index) {
            this.index = index;
            this.park = park;
        }
        
        public void onLoad(CoapResponse response) {
            System.out.println("Received " + response.getResponseText() + " from park " + park.name + " spot " + index + "!");
        	
            JSONObject content = new JSONObject(response.getResponseText());
            
            boolean free = content.getBoolean("free");
            String user = content.optString("user", null);
            String credit = content.optString("credit", null);
            
            if (free == false)
                return;
            
            if (user == null) {
                executor.submit(new FreeSpotRunnable(park, index));
                return;
            }
            
            executor.submit(new OccupySpotRunnable(park, index, user, credit));
        }
        
        public void onError() {
            // TODO: nothing
        }
        
    }
    
    public static void main(String[] args) throws OM2MException, TimeoutException {
        try {
            ParksDataHandler.initMN();
        } catch (OM2MException e) {
            if (e.getCode() != ErrorCode.NAME_ALREADY_PRESENT)
                throw e;
        }
        
        System.out.println("MN initialized!");
        
        for (final ParkConfig p : AppConfig.PARKS) {
            try {
                p.parkID = ParksDataHandler.createPark(p)
                                           .getResourceID();
            } catch (OM2MException e) {
                if (e.getCode() != ErrorCode.NAME_ALREADY_PRESENT)
                    throw e;
                else {
                    p.parkID = ParksDataHandler.getParkID(p.name);
                }
            }
        }
        
        System.out.println("Parks initialized!");
        
        // Now, for each park and for each spot we need to establish an Observing
        // relation
        for (final ParkConfig p : AppConfig.PARKS) {
            String netIP = p.netIP;
            
            for (int i = 1; i <= p.spotsNumber; ++i) {
                String spotIP = getSpotIP(netIP, i);
                
                CoapClient client = new CoapClient("coap://[" + spotIP + "]:5683/park_spot");
                
                /* CoapObserveRelation relation = */
                client.observe(new SpotCoapHandler(p, i));
            }
        }
        
        System.out.println("Clients observed!");
        
        while (true)
            ;
    }
}
