package it.unipi.iot.parking.lib.om2m;

import org.json.JSONObject;

// TODO: probably a check to an interface could be interesing
public class Content extends Resource {
	
	private final int			stateTag;
	private final String		contentInfo;	// NOTICE: always equal to "message"
	private final int			contentSize;
	private final JSONObject	content;
	
	protected Content(JSONObject obj) {
		super(obj);
		
		stateTag = obj.getInt(OM2M.ATTR_STATE_TAG);
		contentInfo = obj.getString(OM2M.ATTR_CONTENT_INFO);
		contentSize = obj.getInt(OM2M.ATTR_CONTENT_SIZE);
		content = new JSONObject(obj.getString(OM2M.ATTR_CONTENT));
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
		
		obj	.put(OM2M.ATTR_STATE_TAG, stateTag)
			.put(OM2M.ATTR_CONTENT_INFO, contentInfo)
			.put(OM2M.ATTR_CONTENT_SIZE, contentSize)
			.put(OM2M.ATTR_CONTENT, content.toString());
		
		return obj;
	}
	
}
