package it.unipi.iot.parking.om2m.data;

import org.json.JSONObject;

import it.unipi.iot.parking.om2m.OM2MConstants;

public class RemoteNode extends OM2MResource {
    
    private final String cseID;
    
    public RemoteNode(JSONObject obj) {
        super(obj);
        
        cseID = obj.getString(OM2MConstants.ATTR_CSE_ID);
    }
    
    public String getCSEID() {
        return cseID;
    }
    
    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = super.toJSONObject();
        
        obj.put(OM2MConstants.ATTR_CSE_ID, cseID);
        
        return obj;
    }
    
}
