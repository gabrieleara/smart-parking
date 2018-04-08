package it.unipi.iot.parking.data;

import org.json.JSONObject;

public class SpotStatus {
    private final static String STRING_ID        = "id";
    private final static String STRING_PARK_ID   = "parkID";
    private final static String STRING_LATITUDE  = "lat";
    private final static String STRING_LONGITUDE = "lon";
    private final static String STRING_FREE      = "free";
    private final static String STRING_USER      = "user";
    
    private final String  id;
    private final String  parkID;
    private final double  latitude;
    private final double  longitude;
    private boolean free;
    private String  user;
    
    public SpotStatus(String parkID, String id, JSONObject data) {
        this.id = id;
        this.parkID = parkID;
        this.latitude = data.getDouble(STRING_LATITUDE);
        this.longitude = data.getDouble(STRING_LONGITUDE);
        this.free = data.getBoolean(STRING_FREE);
        this.user = data.optString(STRING_USER, null);
    }
    
    public String getId() {
        return id;
    }
    
    public String getParkID() {
        return parkID;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public boolean isFree() {
        return free;
    }
    
    public String getUser() {
        return user;
    }
    
    public void setOccupied(String user) {
        this.free = false;
        this.user = user;
    }
    
    public void setFree() {
        this.free = true;
        this.user = null;
    }
    
    public JSONObject toJSONObject() {
        final JSONObject obj;
        
        obj = new JSONObject().put(STRING_ID, this.id)
                              .put(STRING_PARK_ID, this.parkID)
                              .put(STRING_LATITUDE, this.latitude)
                              .put(STRING_LONGITUDE, this.longitude)
                              .put(STRING_FREE, this.free)
                              .put(STRING_USER, this.user);
        return obj;
    }
    
    public String toString() {
        return toJSONObject().toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SpotStatus other = (SpotStatus) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
