package it.unipi.iot.parking.data;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class ParkStatus {
    private final static String STRING_ID           = "id";
    private final static String STRING_NAME         = "name";
    private final static String STRING_LATITUDE     = "lat";
    private final static String STRING_LONGITUDE    = "lon";
    private final static String STRING_ADDRESS      = "address";
    private final static String STRING_PRICE        = "price";
    private final static String STRING_OPENING_TIME = "openT";
    private final static String STRING_CLOSING_TIME = "closeT";
    private final static String STRING_SPOTS        = "spots";
    private final static String STRING_AVAILABLE    = "available";
    
    private final String          id;
    private final String          name;
    private final double          latitude;
    private final double          longitude;
    private final String          address;
    private final double          price;
    private final String          openingTime;
    private final String          closingTime;
    private final Set<SpotStatus> spots = new HashSet<>();
    
    private boolean available = true;
    
    public ParkStatus(String id, JSONObject data) {
        this.id = id;
        this.name = data.getString(STRING_NAME);
        this.latitude = data.getDouble(STRING_LATITUDE);
        this.longitude = data.getDouble(STRING_LONGITUDE);
        this.address = data.getString(STRING_ADDRESS);
        this.price = data.getDouble(STRING_PRICE);
        this.openingTime = data.getString(STRING_OPENING_TIME);
        this.closingTime = data.getString(STRING_CLOSING_TIME);
    }
    
    public String getParkID() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public String getAddress() {
        return address;
    }
    
    public double getPrice() {
        return price;
    }
    
    public String getOpeningTime() {
        return openingTime;
    }
    
    public String getClosingTime() {
        return closingTime;
    }
    
    public boolean isAvailable() {
        return available;
    }
    
    public void setAvailable(boolean available) {
        this.available = available;
    }
    
    public SpotStatus[] getSpots() {
        return spots.toArray(new SpotStatus[spots.size()]);
    }
    
    public void addOrReplaceSpot(SpotStatus spot) {
        if (!spots.add(spot)) {
            spots.remove(spot);
            spots.add(spot);
        }
    }
    
    public JSONObject toJSONObject() {
        final JSONArray spotsArray;
        final JSONObject obj;
        
        spotsArray = new JSONArray();
        
        for (SpotStatus s : spots) {
            spotsArray.put(s.toJSONObject());
        }
        
        obj = new JSONObject().put(STRING_ID, this.id)
                              .put(STRING_NAME, this.name)
                              .put(STRING_LATITUDE, this.latitude)
                              .put(STRING_LONGITUDE, this.longitude)
                              .put(STRING_ADDRESS, this.address)
                              .put(STRING_PRICE, this.price)
                              .put(STRING_OPENING_TIME, this.openingTime)
                              .put(STRING_CLOSING_TIME, this.closingTime)
                              .put(STRING_AVAILABLE, this.available)
                              .put(STRING_SPOTS, spotsArray);
        
        return obj;
    }
    
    public String toString() {
        return toJSONObject().toString();
    }
    
}
