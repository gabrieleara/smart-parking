package it.unipi.iot.parking.lib.om2m;

import org.json.JSONObject;

/**
 * This class holds universal and most common attributes that will be shared by
 * each of its subclasses.
 * 
 * @author Gabriele Ara
 *
 */
public abstract class Resource {
	private final int		resourceType;
	private final String	resourceID;
	private final String	resourceName;
	private final String	parentID;
	private final String	creationTime;
	private final String	lastModifiedTime;
	
	// Common attributes, check if necessary here
	
	// private final List<String> labels;
	// private final List<String> accessControlPolicyIDs;
	// private final String expirationTime;
	// private final String api;
	// private final String aei;
	// private final boolean rr;
	
	protected Resource(JSONObject obj) {
		resourceType = obj.getInt(OM2M.ATTR_RESOURCE_TYPE);
		resourceID = obj.getString(OM2M.ATTR_RESOURCE_ID);
		resourceName = obj.getString(OM2M.ATTR_RESOURCE_NAME);
		parentID = obj.getString(OM2M.ATTR_PARENT_ID);
		creationTime = obj.getString(OM2M.ATTR_CREATION_TIME);
		lastModifiedTime = obj.getString(OM2M.ATTR_LAST_MOD_TIME);
	}
	
	public int getResourceType() {
		return resourceType;
	}
	
	public String getResourceID() {
		return resourceID;
	}
	
	public String getResourceName() {
		return resourceName;
	}
	
	public String getParentID() {
		return parentID;
	}
	
	public String getCreationTime() {
		return creationTime;
	}
	
	public String getLastModifiedTime() {
		return lastModifiedTime;
	}
	
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		
		obj	.put(OM2M.ATTR_RESOURCE_TYPE, resourceType)
			.put(OM2M.ATTR_RESOURCE_ID, resourceID)
			.put(OM2M.ATTR_RESOURCE_NAME, resourceName)
			.put(OM2M.ATTR_PARENT_ID, parentID)
			.put(OM2M.ATTR_CREATION_TIME, creationTime)
			.put(OM2M.ATTR_LAST_MOD_TIME, lastModifiedTime);
		
		return obj;
	}
}
