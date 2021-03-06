package it.unipi.iot.parking.om2m.data;

import org.json.JSONObject;

import it.unipi.iot.parking.om2m.OM2MConstants;

public class ContentInstance extends OM2MResource {
    
    final int        stateTag;
    final String     contentInfo; // NOTICE: always equal to "message"
    final int        contentSize;
    final JSONObject content;
    
    public ContentInstance(JSONObject obj) {
        super(obj);
        
        stateTag = obj.getInt(OM2MConstants.ATTR_STATE_TAG);
        contentInfo = obj.getString(OM2MConstants.ATTR_CONTENT_INFO);
        contentSize = obj.getInt(OM2MConstants.ATTR_CONTENT_SIZE);
        content = new JSONObject(obj.getString(OM2MConstants.ATTR_CONTENT));
    }
    
    public int getStateTag() {
        return stateTag;
    }
    
    public String getContentInfo() {
        return contentInfo;
    }
    
    public int getContentSize() {
        return contentSize;
    }
    
    public JSONObject getContentValue() {
        return content;
    }
    
    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = super.toJSONObject();
        
        obj.put(OM2MConstants.ATTR_STATE_TAG, stateTag)
           .put(OM2MConstants.ATTR_CONTENT_INFO, contentInfo)
           .put(OM2MConstants.ATTR_CONTENT_SIZE, contentSize)
           .put(OM2MConstants.ATTR_CONTENT, content.toString());
        
        return obj;
    }
    
}
