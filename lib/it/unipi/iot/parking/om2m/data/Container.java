package it.unipi.iot.parking.om2m.data;

import org.json.JSONObject;

import it.unipi.iot.parking.om2m.OM2M;

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
	
	public Container(JSONObject obj) {
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

	@Override
	public String[] getCopyOptions() {
		return new String[] { getResourceName() };
	}
	
	
}
