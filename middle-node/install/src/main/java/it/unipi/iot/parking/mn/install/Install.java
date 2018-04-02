package it.unipi.iot.parking.mn.install;

import java.util.concurrent.TimeoutException;

import it.unipi.iot.parking.AppConfig;
import it.unipi.iot.parking.ParkConfig;
import it.unipi.iot.parking.ParksDataHandler;

public class Install {
    // TODO: in the end it will be `throws Exception!` However, some exceptions
    // shall be caught, like when we try to double create something
    public static void main(String[] args) throws TimeoutException {
        for (final ParkConfig p : AppConfig.PARKS) {
            ParksDataHandler.createPark(p);
        }
        
        /*
        // TODO: delete the following code, needed only to test discoveries of data
        String[] parks = ParksDataHandler.getAllParksList();
        
        System.out.println(Arrays.toString(parks));
        
        for (String p : parks) {
            System.out.println(ParksDataHandler.getParkData(p));
        }
        */
        /*
        // TODO: delete the following code, it was only needed to test the delete operation
         
        System.out.println("Waiting for user input, then we will delete an object...");
        
        try {
            System.in.read();
        } catch (IOException e) {
        }
        
        String[] parks = ParksDataHandler.getDirectChildrenList("/" + AppConfig.CSE_ID);
        
        // I want to delete the first spot
        OM2M node = new OM2M(new OM2MSession(AppConfig.HOST_ADDRESS, AppConfig.PORT_NUMBER,
                AppConfig.CREDENTIALS));
        
        OM2MResource res;
        try {
            res = node.get(parks[0]);
        } catch (OM2MException e1) {
            throw new RuntimeException("Crap...");
        }
        
        String[] spots = ParksDataHandler.getDirectChildrenList(res.getResourceID());
        
        
        
        // TODO: add more TimeoutExceptions
        try {
            node.delete(spots[0]);
        } catch (OM2MException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        */
    }
    
}
