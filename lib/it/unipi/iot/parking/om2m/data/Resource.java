package it.unipi.iot.parking.om2m.data;

import org.json.JSONObject;

import it.unipi.iot.parking.om2m.OM2M;

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
	
	public Resource(JSONObject obj) {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((resourceID == null) ? 0 : resourceID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Resource other = (Resource) obj;
		if (resourceID == null) {
			if (other.resourceID != null)
				return false;
		} else if (!resourceID.equals(other.resourceID))
			return false;
		return true;
	}
	
	public String[] getCopyOptions() {
		return new String[0];
	}
}
