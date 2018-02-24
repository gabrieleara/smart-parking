package it.unipi.iot.parking.om2m.data;

import org.json.JSONObject;

import it.unipi.iot.parking.om2m.OM2M;

/**
 * This class represents a OneM2M Application Entity.
 * 
 * @author Gabriele Ara
 *
 */
public class ApplicationEntity extends Resource {
	private final String applicationID;
	
	public ApplicationEntity(JSONObject obj) {
		super(obj);
		
		applicationID = obj.getString(OM2M.ATTR_APPLICATION_ID);
	}
	
	public String getApplicationID() {
		return applicationID;
	}
	
	@Override
	public JSONObject toJSONObject() {
		JSONObject obj = super.toJSONObject();
		
		obj.put(OM2M.ATTR_APPLICATION_ID, applicationID);
		
		return obj;
	}
	
	@Override
	public String[] getCopyOptions() {
		return new String[] { getApplicationID(), getResourceName() };
	}
	
}