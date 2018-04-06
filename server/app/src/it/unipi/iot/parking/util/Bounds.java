package it.unipi.iot.parking.util;

import org.json.JSONObject;

public class Bounds {
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
