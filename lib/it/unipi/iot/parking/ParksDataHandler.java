package it.unipi.iot.parking;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import it.unipi.iot.parking.om2m.ErrorCode;
import it.unipi.iot.parking.om2m.OM2M;
import it.unipi.iot.parking.om2m.OM2MConstants;
import it.unipi.iot.parking.om2m.OM2MException;
import it.unipi.iot.parking.om2m.OM2MSession;
import it.unipi.iot.parking.om2m.data.ApplicationEntity;
import it.unipi.iot.parking.om2m.data.Container;
import it.unipi.iot.parking.om2m.data.ContentInstance;
import it.unipi.iot.parking.om2m.data.OM2MResource;
import it.unipi.iot.parking.om2m.data.Subscription;

// NOTICE: these methods should be synchronized properly!
public class ParksDataHandler {
    @SuppressWarnings("unused")
    private static final Logger LOGGER = Logger.getLogger(ParksDataHandler.class.getName());
    
    private static final String      CONTAINER_BASE_NAME = "park-";
    private static final OM2MSession SESSION_DATA;
    private static final OM2M        OM2M_NODE;
    
    static {
        SESSION_DATA = new OM2MSession(AppConfig.HOST_ADDRESS, AppConfig.PORT_NUMBER,
                AppConfig.CREDENTIALS);
        
        OM2M_NODE = new OM2M(SESSION_DATA);
    }
    
    public static ApplicationEntity createPark(ParkConfig conf)
            throws OM2MException, TimeoutException {
        final String parentID;
        final String[] labels;
        final ApplicationEntity park;
        
        parentID = getResourceIDFromPath(AppConfig.CSE_ID);
        
        labels = new LabelsFactory().setParentID(parentID)
                                    .setResourceName(conf.name) // TODO: probably unused
                                    .setType("park")
                                    .getLabels();
        
        park = OM2M_NODE.createApplicationEntity(parentID, conf.appID, conf.name, labels);
        
        for (int index = 0; index < conf.spots.length; ++index) {
            createSpot(park.getResourceID(), conf.spots[index], index);
        }
        
        return park;
    }
    
    public static Container createSpot(final String parentID, final String data, final int index)
            throws OM2MException, TimeoutException {
        
        String[] labels;
        
        final String containerName;
        final Container container;
        final LabelsFactory factory;
        
        containerName = assignSpotName(index);
        
        factory = new LabelsFactory().setParentID(parentID)
                                     .setResourceName(containerName) // TODO: probably unused
                                     .setSpotName(containerName)
                                     .setParkID(parentID);
        
        if (index == 0)
            factory.setType("manifest");
        else
            factory.setType("spot");
        
        labels = factory.getLabels();
        
        container = OM2M_NODE.createContainer(parentID, containerName, labels);
        
        factory.reset()
               .setParentID(container.getResourceID())
               .setParkID(parentID)
               .setSpotName(containerName);
        
        if (index == 0)
            factory.setType("manifest-data");
        else
            factory.setType("instance");
        
        labels = factory.getLabels();
        
        OM2M_NODE.createContentInstance(container.getResourceID(), data, labels);
        
        return container;
        
    }
    
    public static void deleteAllSubscriptions(String startingNodePath)
            throws OM2MException, TimeoutException {
        final String[] filters;
        
        filters = new LabelsFactory().setType("subscription")
                                     .getFilters();
        
        String[] subscriptions = OM2M_NODE.discovery(startingNodePath, filters);
        
        for (String s : subscriptions) {
            OM2M_NODE.delete(s);
        }
    }
    
    public static String[] getAllParksList() throws OM2MException, TimeoutException {
        final String[] filters;
        final String[] uril;
        final List<String> parkList;
        final String[] parkIDs;
        
        filters = new LabelsFactory().setType("park")
                                     .getFilters();
        
        uril = OM2M_NODE.discovery(AppConfig.CSE_ID, filters);
        parkList = new ArrayList<>();
        
        for (String uri : uril) {
            parkList.add(getResourceIDFromPath(uri));
        }
        
        parkIDs = parkList.toArray(new String[parkList.size()]);
        
        return parkIDs;
    }
    
    public static JSONObject getParkData(String parkID) throws OM2MException, TimeoutException {
        String[] filters;
        String[] uril;
        final JSONObject parkStatus;
        final JSONArray spots;
        
        filters = new LabelsFactory().setType("manifest")
                                     .setParkID(parkID)
                                     .getFilters();
        
        uril = OM2M_NODE.discovery(AppConfig.CSE_ID, filters);
        
        if (uril.length > 1 || uril.length < 1)
            throw new OM2MException("Bad discovery request generated!", ErrorCode.OTHER);
        
        parkStatus = ((ContentInstance) OM2M_NODE.get(uril[0] + "/la")).getContentValue();
        
        parkStatus.put("id", parkID);
        parkStatus.put("available", true); // TODO: implement it
        
        // Get list of spots and add each one to a JSONArray
        spots = new JSONArray();
        
        filters = new LabelsFactory().setType("spot")
                                     .setParkID(parkID)
                                     .getFilters();
        
        uril = OM2M_NODE.discovery(AppConfig.CSE_ID, filters);
        
        for (String uri : uril) {
            String spotID = getResourceIDFromPath(uri);
            ContentInstance value = (ContentInstance) OM2M_NODE.get(uri + "/la");
            JSONObject spotStatus = value.getContentValue();
            spotStatus.put("id",spotID);
            spots.put(spotStatus);
        }
        
        parkStatus.put("spots", spots);
        
        return parkStatus;
    }
    
    // TODO: add more data here if necessary
    public static JSONObject getParkDataFromURI(String parkPath)
            throws OM2MException, TimeoutException {
        final String parkID;
        
        parkID = getResourceIDFromPath(parkPath);
        
        return getParkData(parkID);
    }
    
    public static String[] getAllRemoteNodes() throws OM2MException, TimeoutException {
        final String[] filters;
        
        filters = new String[] { "rty=" + OM2MConstants.RESOURCE_TYPE_REMOTE_CSE };
        
        return OM2M_NODE.discovery(AppConfig.CSE_ID, filters);
    }
    
    public static boolean freeSpot(String parkID, int index)
            throws OM2MException, TimeoutException {
        return setSpot(parkID, index, true, null);
    }
    
    public static boolean occupySpot(String parkID, int index, String user)
            throws OM2MException, TimeoutException {
        return setSpot(parkID, index, false, user);
    }
    
    /**
     * 
     * @param parkID
     * @param index
     * @param free
     * @param user
     * @return
     * @throws TimeoutException
     */
    private static boolean setSpot(String parkID, int index, boolean free, String user)
            throws OM2MException, TimeoutException {
        final String containerName;
        final ContentInstance value;
        
        containerName = assignSpotName(index);
        
        // NOTICE: I used parentID to avoid matching copies of the spot in the IN node
        String[] filters = new LabelsFactory().setParentID(parkID)
                                              .setResourceName(containerName)
                                              .getFilters();
        
        String[] uri = OM2M_NODE.discovery(parkID, filters);
        
        if (uri.length > 0)
            throw new OM2MException(
                    "Too many spots associated with the same parkID and resourceName!",
                    ErrorCode.OTHER);
        
        String spotID = getResourceIDFromPath(uri[0]);
        
        value = (ContentInstance) OM2M_NODE.get(spotID + "/la");
        
        JSONObject contentValue = new JSONObject(value.getContentValue());
        
        // TODO: move constants to other file
        if (contentValue.getBoolean("free") == free) {
            return false;
        }
        
        // Change the spot value
        // TODO: add all necessary values
        contentValue = new JSONObject().put("free", free)
                                       .put("user", user);
        
        final String[] labels = new LabelsFactory().setParentID(spotID)
                                                   .setParkID(parkID)
                                                   .setSpotName(containerName)
                                                   .setType("instance")
                                                   .getLabels();
        
        OM2M_NODE.createContentInstance(spotID, contentValue.toString(), labels);
        
        return true;
    }
    
    public static String[] getDirectChildrenList(final String resourcePath)
            throws OM2MException, TimeoutException {
        
        final String parentID = getResourceIDFromPath(resourcePath);
        
        String[] uriQueries = new LabelsFactory().setParentID(parentID)
                                                 .getFilters();
        
        return OM2M_NODE.discovery(resourcePath, uriQueries);
    }
    
    public static Subscription subscribe(final String remoteID, final String subscriberURI)
            throws OM2MException, TimeoutException {
        final String[] labels;
        final String resourceID = getResourceIDFromPath(remoteID);
        
        labels = new LabelsFactory().setParentID(resourceID)
                                    .setType("subscription")
                                    .getLabels();
        
        return OM2M_NODE.createSubscription(remoteID, subscriberURI, labels);
    }
    
    private static String findLocalParentID(final String remoteParentID, final String copyBasePath)
            throws OM2MException, TimeoutException {
        final String[] uril;
        
        // Find the local copy with remoteID = remoteParentID
        String[] filters = new LabelsFactory().setRemoteID(remoteParentID)
                                              .getFilters();
        
        uril = OM2M_NODE.discovery(copyBasePath, filters);
        
        if (uril.length > 1)
            throw new OM2MException("Bad discovery request generated!", ErrorCode.OTHER);
        
        if (uril.length < 1) {
            // Oops, it was a RemoteCSE then, let us just use the copyBasePath then
            return copyBasePath;
        }
        
        return getResourceIDFromPath(uril[0]);
        
        /*
         * filters = new LabelsFactory().setParentID(AppConfig.CSE_ID) .getFilters();
         * 
         * uril = OM2M_NODE.discovery(copyBasePath, new String[] { "pi=" +
         * AppConfig.CSE_ID });
         * 
         * if (uril.length < 1) throw new
         * OM2MException("Bad discovery request generated!", ErrorCode.OTHER);
         * 
         * for (String uri : uril) { OM2MResource res = OM2M_NODE.get(uri);
         * 
         * if (res instanceof RemoteNode) { if (((RemoteNode) res).getCSEID()
         * .equals(remoteID)) { uril = new String[] { uri }; break; } } }
         * 
         * if (uril.length > 1) throw new
         * OM2MException("Bad discovery request generated!", ErrorCode.OTHER); }
         */
    }
    
    private static ApplicationEntity createAECopy(final ApplicationEntity original,
            final String localParentID) throws OM2MException, TimeoutException {
        final String remoteAppID;
        final String remoteName;
        final String remoteID;
        final String parkID;
        final String[] labels;
        
        remoteAppID = original.getApplicationID();
        remoteName = original.getResourceName();
        remoteID = original.getResourceID();
        parkID = original.getResourceID();
        
        labels = new LabelsFactory().setParentID(localParentID)
                                    .setParkID(parkID)
                                    .setRemoteID(remoteID)
                                    .setResourceName(remoteName)
                                    .setType("park")
                                    .getLabels();
        
        return OM2M_NODE.createApplicationEntity(localParentID, remoteAppID, remoteName, labels);
    }
    
    private static Container createCNTCopy(final Container original, final String localParentID)
            throws OM2MException, TimeoutException {
        final String remoteName;
        final String remoteID;
        final String parkID;
        final String spotName;
        final String type;
        final String[] remoteLabels;
        final String[] labels;
        
        remoteName = original.getResourceName();
        remoteID = original.getResourceID();
        spotName = remoteName;
        remoteLabels = original.getLabels();
        parkID = localParentID;
        type = LabelsFactory.getType(remoteLabels);
        
        labels = new LabelsFactory().setParentID(localParentID)
                                    .setParkID(parkID)
                                    .setRemoteID(remoteID)
                                    .setResourceName(remoteName)
                                    .setSpotName(spotName)
                                    .setType(type)
                                    .getLabels();
        
        return OM2M_NODE.createContainer(localParentID, remoteName, labels);
    }
    
    private static ContentInstance createCICopy(final ContentInstance original,
            final String localParentID) throws OM2MException, TimeoutException {
        final String remoteName;
        final String remoteID;
        final String parkID;
        final String spotName;
        final String value;
        final String type;
        final String[] remoteLabels;
        final String[] labels;
        
        remoteName = original.getResourceName();
        remoteID = original.getResourceID();
        value = original.getContentValue()
                        .toString();
        
        remoteLabels = original.getLabels();
        
        spotName = LabelsFactory.getSpotName(remoteLabels);
        parkID = LabelsFactory.getParkID(get(localParentID).getLabels());
        type = LabelsFactory.getType(remoteLabels);
        
        labels = new LabelsFactory().setParentID(localParentID)
                                    .setParkID(parkID)
                                    .setRemoteID(remoteID)
                                    .setResourceName(remoteName)
                                    .setSpotName(spotName)
                                    .setType(type)
                                    .getLabels();
        
        return OM2M_NODE.createContentInstance(localParentID, remoteName, value, labels);
    }
    
    // I don't need to check if it already exists, because an error will be thrown
    // for the duplicate resource name, however, outside it much be known that the
    // exception will be thrown
    public static OM2MResource createCopy(final OM2MResource original, final String copyBasePath)
            throws OM2MException, TimeoutException {
        final String localParentID;
        OM2MResource copy;
        
        localParentID = findLocalParentID(original.getParentID(), copyBasePath);
        
        switch (original.getResourceType()) {
        case OM2MConstants.RESOURCE_TYPE_APPLICATION_ENTITY:
            copy = createAECopy((ApplicationEntity) original, localParentID);
            break;
        case OM2MConstants.RESOURCE_TYPE_CONTAINER:
            copy = createCNTCopy((Container) original, localParentID);
            break;
        case OM2MConstants.RESOURCE_TYPE_CONTENT_INSTANCE:
            copy = createCICopy((ContentInstance) original, localParentID);
            break;
        default:
            throw new IllegalArgumentException("Unsupported resource to be copied!");
        }
        
        return copy;
    }
    
    public static OM2MResource get(String resourcePath) throws OM2MException, TimeoutException {
        return OM2M_NODE.get(resourcePath);
    }
    
    private static String getResourceIDFromPath(String resourcePath)
            throws OM2MException, TimeoutException {
        return OM2M_NODE.get(resourcePath)
                        .getResourceID();
    }
    
    private static String assignSpotName(final int index) {
        return CONTAINER_BASE_NAME
                + ((index > 99) ? "" + index : (index > 9) ? "0" + index : "00" + index);
    }
    
    private static class LabelsFactory {
        private static final String SEP = ":";
        
        private String parentID;
        private String resourceName;
        private String type;
        private String parkID;
        private String spotName;
        private String remoteID;
        
        public static String getParentFilter(String parentID) {
            return "pi" + SEP + parentID;
        }
        
        public static String getNameFilter(String resourceName) {
            return "rn" + SEP + resourceName;
        }
        
        public static String getTypeFilter(String type) {
            return "ty" + SEP + type;
        }
        
        public static String getParkFilter(String parkID) {
            return "pkid" + SEP + parkID;
        }
        
        public static String getSpotFilter(String spotName) {
            return "sn" + SEP + spotName;
        }
        
        public static String getRemoteFilter(String remoteID) {
            return "rmtid" + SEP + remoteID;
        }
        
        private static String getValue(String[] labels, String prefix) {
            for (String s : labels) {
                if (s.startsWith(prefix)) {
                    return s.substring(prefix.length());
                }
            }
            
            throw new RuntimeException(
                    "The requested label couldn't be found! Prefix was `" + prefix + "`");
        }
        
        public static String getParkID(String[] labels) {
            final String prefix = "pkid" + SEP;
            return getValue(labels, prefix);
        }
        
        public static String getSpotName(String[] labels) {
            final String prefix = "sn" + SEP;
            return getValue(labels, prefix);
        }
        
        public static String getType(String[] labels) {
            final String prefix = "ty" + SEP;
            return getValue(labels, prefix);
        }
        
        public LabelsFactory() {
            reset();
        }
        
        public LabelsFactory reset() {
            parentID = null;
            resourceName = null;
            type = null;
            parkID = null;
            spotName = null;
            remoteID = null;
            
            return this;
        }
        
        public LabelsFactory setParentID(String parentID) {
            this.parentID = parentID;
            return this;
        }
        
        public LabelsFactory setResourceName(String resourceName) {
            this.resourceName = resourceName;
            return this;
        }
        
        public LabelsFactory setType(String type) {
            this.type = type;
            return this;
        }
        
        public LabelsFactory setParkID(String parkID) {
            this.parkID = parkID;
            return this;
        }
        
        public LabelsFactory setSpotName(String spotName) {
            this.spotName = spotName;
            return this;
        }
        
        public LabelsFactory setRemoteID(String remoteID) {
            this.remoteID = remoteID;
            return this;
        }
        
        public String[] getLabels() {
            ArrayList<String> labels = new ArrayList<>();
            
            if (parentID != null)
                labels.add(getParentFilter(parentID));
            
            if (resourceName != null)
                labels.add(getNameFilter(resourceName));
            
            if (type != null) {
                labels.add(getTypeFilter(type));
            }
            
            if (parkID != null) {
                labels.add(getParkFilter(parkID));
            }
            
            if (spotName != null) {
                labels.add(getSpotFilter(spotName));
            }
            
            if (remoteID != null) {
                labels.add(getRemoteFilter(remoteID));
            }
            
            return labels.toArray(new String[labels.size()]);
        }
        
        public String[] getFilters() {
            final String[] labels = getLabels();
            
            for (int i = 0; i < labels.length; ++i) {
                labels[i] = "lbl=" + labels[i];
            }
            
            return labels;
        }
        
    }
}
