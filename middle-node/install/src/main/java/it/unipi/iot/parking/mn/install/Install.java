package it.unipi.iot.parking.mn.install;

import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import it.unipi.iot.parking.AppConfig;
import it.unipi.iot.parking.ParkConfig;
import it.unipi.iot.parking.ParksDataHandler;
import it.unipi.iot.parking.om2m.ErrorCode;
import it.unipi.iot.parking.om2m.OM2MException;

public class Install {
    public static void main(String[] args) throws TimeoutException {
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
        
        
        // TODO: remove, this is a test of the actual behavior of the parks and spots
        
        @SuppressWarnings("resource")
        Scanner in = new Scanner(System.in);
        
        while (true) {
            System.out.print("Insert the parkID you want to change: ");
            System.out.flush();
            
            String parkID = in.next();
            
            System.out.print("Insert the index of the spot you want to change: ");
            System.out.flush();
            
            int index = in.nextInt();
            
            if (ParksDataHandler.freeSpot(parkID, index)) {
                System.out.println("Spot has been freed!");
            } else {
                if (ParksDataHandler.payForSpot(parkID, index, "gabriele", "12345")) {
                    System.out.println("Spot has been occupied!");
                } else {
                    System.out.println("Unable to occupy spot!");
                }
            }
        }
        
        /*
         * // TODO: delete the following code, needed only to test discoveries of data
         * String[] parks = ParksDataHandler.getAllParksList();
         * 
         * System.out.println(Arrays.toString(parks));
         * 
         * for (String p : parks) { System.out.println(ParksDataHandler.getParkData(p));
         * }
         */
        /*
         * // TODO: delete the following code, it was only needed to test the delete
         * operation
         * 
         * System.out.println("Waiting for user input, then we will delete an object..."
         * );
         * 
         * try { System.in.read(); } catch (IOException e) { }
         * 
         * String[] parks = ParksDataHandler.getDirectChildrenList("/" +
         * AppConfig.CSE_ID);
         * 
         * // I want to delete the first spot OM2M node = new OM2M(new
         * OM2MSession(AppConfig.HOST_ADDRESS, AppConfig.PORT_NUMBER,
         * AppConfig.CREDENTIALS));
         * 
         * OM2MResource res; try { res = node.get(parks[0]); } catch (OM2MException e1)
         * { throw new RuntimeException("Crap..."); }
         * 
         * String[] spots = ParksDataHandler.getDirectChildrenList(res.getResourceID());
         * 
         * 
         * 
         * // TODO: add more TimeoutExceptions try { node.delete(spots[0]); } catch
         * (OM2MException e) { // TODO Auto-generated catch block e.printStackTrace(); }
         */
    }
    
}
