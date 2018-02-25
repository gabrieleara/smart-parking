package it.unipi.iot.parking.om2m.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
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
	final int		resourceType;
	final String	resourceID;
	final String	resourceName;
	final String	parentID;
	final String	creationTime;
	final String	lastModifiedTime;
	final String[]	labels;
	
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
		
		List<String> labelsList = new ArrayList<>();
		
		if (obj.has(OM2M.ATTR_LABELS)) {
			JSONArray labelsArray = obj.getJSONArray(OM2M.ATTR_LABELS);
			for (Object s : labelsArray) {
				
				labelsList.add((String) s);
			}
		}
		
		labels = labelsList.toArray(new String[labelsList.size()]);
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
	
	public String[] getLabels() {
		return Arrays.copyOf(labels, labels.length);
	}
	
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		
		obj	.put(OM2M.ATTR_RESOURCE_TYPE, resourceType)
			.put(OM2M.ATTR_RESOURCE_ID, resourceID)
			.put(OM2M.ATTR_RESOURCE_NAME, resourceName)
			.put(OM2M.ATTR_PARENT_ID, parentID)
			.put(OM2M.ATTR_CREATION_TIME, creationTime)
			.put(OM2M.ATTR_LAST_MOD_TIME, lastModifiedTime)
			.put(OM2M.ATTR_LABELS, new JSONArray(labels));
		
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
