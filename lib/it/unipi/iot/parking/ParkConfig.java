package it.unipi.iot.parking;

import org.json.JSONArray;
import org.json.JSONObject;

public class ParkConfig {
    
    public final String name;
    public final String appID;
    
    // This means also spot number zero, which actually is the manifest of the park
    public final String[] spots;
    
    public ParkConfig(JSONObject data) {
        appID = data.getString("app_id");
        name = data.getString("name");
        
        JSONArray spotsData = data.getJSONArray("spots");
        
        spots = new String[spotsData.length()];
        
        for (int i = 0; i < spotsData.length(); ++i) {
            spots[i] = spotsData.getJSONObject(i).toString();
        }
        
        if (appID == null)
            throw new RuntimeException(
                    "Configuration constant APP_ID for a park was not initialized!");
        
        if (name == null) {
            throw new RuntimeException(
                    "Configuration constant NAME for a park was not initialized!");
        }
        
        for (String s : spots) {
            if (s == null) {
                throw new RuntimeException(
                        "One of configuration constants spot in a park was not initialized!");
            }
        }
        
    }
    
}
