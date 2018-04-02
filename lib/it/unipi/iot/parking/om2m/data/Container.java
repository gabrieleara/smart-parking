package it.unipi.iot.parking.om2m.data;

import org.json.JSONObject;

import it.unipi.iot.parking.om2m.OM2MConstants;

/**
 * This class represents a OneM2M Container.
 * 
 * @author Gabriele Ara
 *
 */
public class Container extends OM2MResource {
    private final String oldest;
    private final String latest;
    private final int    stateTag;
    
    public Container(JSONObject obj) {
        super(obj);
        oldest = obj.getString(OM2MConstants.ATTR_OLDEST);
        latest = obj.getString(OM2MConstants.ATTR_LATEST);
        stateTag = obj.getInt(OM2MConstants.ATTR_STATE_TAG);
    }
    
    public String getOldest() {
        return oldest;
    }
    
    public String getLatest() {
        return latest;
    }
    
    public int getStateTag() {
        return stateTag;
    }
    
    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = super.toJSONObject();
        
        obj.put(OM2MConstants.ATTR_OLDEST, oldest)
           .put(OM2MConstants.ATTR_LATEST, latest)
           .put(OM2MConstants.ATTR_STATE_TAG, stateTag);
        
        return obj;
    }
    
    @Override
    public String[] getCopyParameters() {
        return new String[] { getResourceName() };
    }
    
}
