package it.unipi.iot.parking.om2m.data;

import org.json.JSONObject;

import it.unipi.iot.parking.om2m.OM2MConstants;

/**
 * This class represents a OneM2M Application Entity.
 * 
 * @author Gabriele Ara
 *
 */
public class ApplicationEntity extends OM2MResource {
    private final String applicationID;
    
    public ApplicationEntity(JSONObject obj) {
        super(obj);
        
        applicationID = obj.getString(OM2MConstants.ATTR_APPLICATION_ID);
    }
    
    public String getApplicationID() {
        return applicationID;
    }
    
    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = super.toJSONObject();
        
        obj.put(OM2MConstants.ATTR_APPLICATION_ID, applicationID);
        
        return obj;
    }
    
}