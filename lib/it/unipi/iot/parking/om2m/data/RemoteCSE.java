package it.unipi.iot.parking.om2m.data;

import org.json.JSONObject;

import it.unipi.iot.parking.om2m.OM2M;

public class RemoteCSE extends OM2MResource {
	
	private final String cseID;

	public RemoteCSE(JSONObject obj) {
		super(obj);
		
		cseID = obj.getString(OM2M.ATTR_CSE_ID);
	}

	public String getCSEID() {
		return cseID;
	}

	@Override
	public JSONObject toJSONObject() {
		// TODO Auto-generated method stub
		JSONObject obj = super.toJSONObject();
		
		obj.put(OM2M.ATTR_CSE_ID, cseID);
		
		return obj;
	}
	
	
	
}
