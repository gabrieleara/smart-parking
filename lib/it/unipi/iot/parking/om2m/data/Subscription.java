package it.unipi.iot.parking.om2m.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import it.unipi.iot.parking.om2m.OM2M;

public class Subscription extends OM2MResource {
    private final String[] notificationURIs;
    
    public Subscription(JSONObject obj) {
        super(obj);
        
        JSONArray arr = obj.getJSONArray(OM2M.ATTR_NOTIFICATION_URI);
        List<String> list = new ArrayList<String>();
        
        for (Object o : arr) {
            list.add((String) o);
        }
        
        notificationURIs = list.toArray(new String[list.size()]);
    }
    
    public String[] getNotificationURIs() {
        return notificationURIs;
    }
    
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        
        // TODO: other attributes if necessary
        obj.put(OM2M.ATTR_NOTIFICATION_URI, notificationURIs);
        
        return obj;
    }
    
    /*
     * public String[] getCopyOptions() { return new String[0]; }
     */
    
}
