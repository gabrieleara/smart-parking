package it.unipi.iot.parking;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONObject;

public class AppConfig {
    public static final String HOST_ADDRESS;
    public static final String PORT_NUMBER;
    public static final String CREDENTIALS;
    public static final String CSE_ID;
    public static final String CSE_NAME;
    public static final String NODE_TYPE;   // Can be either "IN" or "MN"
    // public static final String IN_ID;
    
    /**
     * Only for MN, null for IN
     */
    public final static ParkConfig[] PARKS;
    
    static {
        Charset encoding = StandardCharsets.UTF_8;
        String fname = "./config.json";
        
        String content;
        JSONObject obj;
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(fname));
            content = new String(encoded, encoding);
            obj = new JSONObject(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        HOST_ADDRESS = obj.getString("host_address");
        PORT_NUMBER = obj.getString("port_number");
        CREDENTIALS = obj.getString("credentials");
        CSE_ID = obj.getString("cse_id");
        CSE_NAME = obj.getString("cse_name");
        NODE_TYPE = obj.getString("node_type");
        
        if (NODE_TYPE.equalsIgnoreCase("MN")) {
            final JSONArray parksData = obj.getJSONArray("parks_data");
            
            PARKS = new ParkConfig[parksData.length()];
            
            for (int i = 0; i < parksData.length(); ++i) {
                PARKS[i] = new ParkConfig(parksData.getJSONObject(i));
            }
        } else {
            PARKS = null;
        }
        
        if (HOST_ADDRESS == null)
            throw new RuntimeException("Configuration constant HOST_ADDRESS was not initialized!");
        if (PORT_NUMBER == null)
            throw new RuntimeException("Configuration constant PORT_NUMBER was not initialized!");
        if (CREDENTIALS == null)
            throw new RuntimeException("Configuration constant CREDENTIALS was not initialized!");
        if (CSE_ID == null)
            throw new RuntimeException("Configuration constant CSE_ID was not initialized!");
        if (CSE_NAME == null)
            throw new RuntimeException("Configuration constant CSE_NAME was not initialized!");
        if (NODE_TYPE == null)
            throw new RuntimeException("Configuration constant NODE_TYPE was not initialized!");
        
    }
    
    private AppConfig() {
    }
    
}
