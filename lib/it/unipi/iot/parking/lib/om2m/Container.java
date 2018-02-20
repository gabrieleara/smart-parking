package it.unipi.iot.parking.lib.om2m;

import org.json.JSONObject;

/**
 * This class represents a OneM2M Container.
 * 
 * @author Gabriele Ara
 *
 */
public class Container extends Resource {
	private final String	oldest;
	private final String	latest;
	private final int		stateTag;
	
	protected Container(JSONObject obj) {
		super(obj);
		oldest = obj.getString(OM2M.ATTR_OLDEST);
		latest = obj.getString(OM2M.ATTR_LATEST);
		stateTag = obj.getInt(OM2M.ATTR_STATE_TAG);
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
		
		obj	.put(OM2M.ATTR_OLDEST, oldest)
			.put(OM2M.ATTR_LATEST, latest)
			.put(OM2M.ATTR_STATE_TAG, stateTag);
		
		return obj;
	}
}
