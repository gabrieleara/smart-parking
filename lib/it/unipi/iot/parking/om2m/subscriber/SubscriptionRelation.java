package it.unipi.iot.parking.om2m.subscriber;

import java.util.Date;

import it.unipi.iot.parking.om2m.data.OM2MResource;
import it.unipi.iot.parking.util.DateConverter;

public class SubscriptionRelation {
    private final String remoteResourceID;
    private final int    childTypeToCopy;
    private String       localResourceID;
    private Date         lastCreationTimeCopied;
    private boolean      acceptNotifications;
    
    public SubscriptionRelation(String remoteResourceID, int childTypeToCopy,
            String localResourceID) {
        this.remoteResourceID = remoteResourceID;
        this.childTypeToCopy = childTypeToCopy;
        this.localResourceID = localResourceID;
        
        lastCreationTimeCopied = DateConverter.fromString("19700101T000000");
        acceptNotifications = false;
    }
    
    public boolean shallCopy(OM2MResource res) {
        return res.getCreationTime().after(lastCreationTimeCopied);
    }
    
    public Date getLastCreationTimeCopied() {
        return lastCreationTimeCopied;
    }
    
    public synchronized void accept() {
        acceptNotifications = true;
        notifyAll();
    }
    
    public boolean setLastCreationTimeCopied(Date newTime) {
        if (newTime.compareTo(lastCreationTimeCopied) <= 0)
            return false;
        
        this.lastCreationTimeCopied = newTime;
        
        accept();
        
        return true;
    }
    
    public synchronized boolean acceptNotification(Date newTime) {
        while (!acceptNotifications) {
            try {
                wait();
            } catch (InterruptedException e) {
                // It's just a matter of time, given the implementation of the class
                // CopySubscriberResource
            }
        }
        
        return setLastCreationTimeCopied(newTime);
    }
    
    public String getRemoteResourceID() {
        return remoteResourceID;
    }
    
    public String getLocalResourceID() {
        return localResourceID;
    }
    
    public int getChildTypeToCopy() {
        return childTypeToCopy;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + childTypeToCopy;
        result = prime * result + ((remoteResourceID == null) ? 0 : remoteResourceID.hashCode());
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
        SubscriptionRelation other = (SubscriptionRelation) obj;
        if (childTypeToCopy != other.childTypeToCopy)
            return false;
        if (remoteResourceID == null) {
            if (other.remoteResourceID != null)
                return false;
        } else if (!remoteResourceID.equals(other.remoteResourceID))
            return false;
        return true;
    }
    
    
    
}
