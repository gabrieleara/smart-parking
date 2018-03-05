package it.unipi.iot.parking.om2m.data;

import org.json.JSONObject;

import it.unipi.iot.parking.om2m.OM2M;

public class ContentInstance extends OM2MResource {
	
	final int			stateTag;
	final String		contentInfo;	// NOTICE: always equal to "message"
	final int			contentSize;
	final JSONObject	content;
	
	public ContentInstance(JSONObject obj) {
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
	
	@Override
	public String[] getCopyOptions() {
		// TODO: should add time information to the value!
		
		String[] options = new String[labels.length + 1];
		
		options[0] = content.toString();
		
		int i = 1;
		for(String label : labels) {
			options[i++] = label;
		}
		
		//System.arraycopy(labels, 0, content, 1, labels.length);
		
		return options;
	}
	
}
