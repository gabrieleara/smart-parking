package it.unipi.iot.parking.om2m.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import it.unipi.iot.parking.om2m.OM2M;
import it.unipi.iot.parking.util.DateConverter;

/**
 * This class holds universal and most common attributes that will be shared by
 * each of its subclasses.
 * 
 * @author Gabriele Ara
 *
 */
public abstract class OM2MResource implements Comparable<OM2MResource> {
    final int      resourceType;
    final String   resourceID;
    final String   resourceName;
    final String   parentID;
    final Date     creationTime;
    final Date     lastModifiedTime;
    final String[] labels;
    
    public OM2MResource(JSONObject obj) {
        resourceType = obj.getInt(OM2M.ATTR_RESOURCE_TYPE);
        resourceID = obj.getString(OM2M.ATTR_RESOURCE_ID);
        resourceName = obj.getString(OM2M.ATTR_RESOURCE_NAME);
        parentID = obj.getString(OM2M.ATTR_PARENT_ID);
        
        String strdate = obj.getString(OM2M.ATTR_CREATION_TIME);
        creationTime = DateConverter.fromString(strdate);
        
        strdate = obj.getString(OM2M.ATTR_LAST_MOD_TIME);
        lastModifiedTime = DateConverter.fromString(strdate);
        
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
    
    public Date getCreationTime() {
        return creationTime;
    }
    
    public Date getLastModifiedTime() {
        return lastModifiedTime;
    }
    
    public String[] getLabels() {
        return Arrays.copyOf(labels, labels.length);
    }
    
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        
        String creationT = DateConverter.fromDate(creationTime);
        String lastModifiedT = DateConverter.fromDate(lastModifiedTime);
        
        obj.put(OM2M.ATTR_RESOURCE_TYPE, resourceType)
           .put(OM2M.ATTR_RESOURCE_ID, resourceID)
           .put(OM2M.ATTR_RESOURCE_NAME, resourceName)
           .put(OM2M.ATTR_PARENT_ID, parentID)
           .put(OM2M.ATTR_CREATION_TIME, creationT)
           .put(OM2M.ATTR_LAST_MOD_TIME, lastModifiedT)
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
        OM2MResource other = (OM2MResource) obj;
        if (resourceID == null) {
            if (other.resourceID != null)
                return false;
        } else if (!resourceID.equals(other.resourceID))
            return false;
        return true;
    }
    
    // TODO: change copy options to something better
    public String[] getCopyOptions() {
        return new String[0];
    }
    
    public static OM2MResource fromJSONObject(JSONObject obj) {
        OM2MResource res = null;
        
        switch (obj.getInt(OM2M.ATTR_RESOURCE_TYPE)) {
        case OM2M.RESOURCE_TYPE_APPLICATION_ENTITY:
            res = new ApplicationEntity(obj);
            break;
        case OM2M.RESOURCE_TYPE_CONTAINER:
            res = new Container(obj);
            break;
        case OM2M.RESOURCE_TYPE_CONTENT_INSTANCE:
            res = new ContentInstance(obj);
            break;
        case OM2M.RESOURCE_TYPE_REMOTE_CSE:
            res = new RemoteCSE(obj);
            break;
        case OM2M.RESOURCE_TYPE_SUBSCRIPTION:
            res = new Subscription(obj);
            break;
        default:
            // Other types are not implemented
        }
        return res;
    }

    @Override
    public int compareTo(OM2MResource other) {
        Date d0 = this.getCreationTime();
        Date d1 = other.getCreationTime();
        
        int res = d0.compareTo(d1);

        if (res == 0 && !this.equals(other))
            res = -1;
        
        return res;
    }
    
    
}
